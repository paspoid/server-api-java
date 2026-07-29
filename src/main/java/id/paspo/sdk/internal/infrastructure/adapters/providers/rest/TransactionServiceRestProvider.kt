package id.paspo.sdk.internal.infrastructure.adapters.providers.rest

import id.paspo.sdk.exceptions.ApiException
import id.paspo.sdk.exceptions.ConfigurationException
import id.paspo.sdk.exceptions.DecodeException
import id.paspo.sdk.exceptions.TransportException
import id.paspo.sdk.internal.application.ports.providers.TransactionServiceProvider
import id.paspo.sdk.internal.application.ports.providers.requests.GetKeyRequest
import id.paspo.sdk.internal.application.ports.providers.requests.ValidateRequest
import id.paspo.sdk.internal.application.ports.providers.responses.GetKeyProviderResponse
import id.paspo.sdk.internal.application.ports.providers.responses.ValidateProviderResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.Closeable
import java.io.IOException
import java.net.URI
import java.util.concurrent.TimeUnit

internal class TransactionServiceRestProvider(
    baseUrl: String,
    private val apiKey: String,
    private val apiSecret: String,
    timeoutSeconds: Long = 15,
    customClient: OkHttpClient? = null
) : TransactionServiceProvider, Closeable {

    private val normalizedBaseUrl = normalizeBaseUrl(baseUrl)
    private val client = customClient ?: OkHttpClient.Builder()
        .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override suspend fun getKey(request: GetKeyRequest): GetKeyProviderResponse = withContext(Dispatchers.IO) {
        val jsonPayload = JSONObject().apply {
            put("api_key", apiKey)
            put("api_secret", apiSecret)
            put("service_public_id", request.servicePublicId)
            put("transaction_type", request.transactionType)
        }

        val jsonObject = post("/v1/ext/get-key", jsonPayload)
        val key = jsonObject.optString("key")
        val validationWindow = jsonObject.optString("validation_window")

        if (key.isEmpty() || validationWindow.isEmpty()) {
            throw DecodeException("get-key response missing required 'key' or 'validation_window'")
        }

        GetKeyProviderResponse(key = key, validationWindow = validationWindow)
    }

    override suspend fun validate(request: ValidateRequest): ValidateProviderResponse = withContext(Dispatchers.IO) {
        val jsonPayload = JSONObject().apply {
            put("nonce", request.nonce)
        }

        val jsonObject = post("/v1/ext/validate", jsonPayload)
        val status = jsonObject.optString("status")
        if (status.isEmpty()) {
            throw DecodeException("validate response missing required 'status' field")
        }

        ValidateProviderResponse(
            status = status,
            dataType = if (jsonObject.has("data_type") && !jsonObject.isNull("data_type")) jsonObject.getString("data_type") else null,
            dataValue = if (jsonObject.has("data_value") && !jsonObject.isNull("data_value")) jsonObject.getString("data_value") else null,
            phoneData = jsonObject.optJSONObject("phone_data"),
            deviceData = jsonObject.optJSONObject("device_data")
        )
    }

    override fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }

    private fun post(endpoint: String, payload: JSONObject): JSONObject {
        val url = "$normalizedBaseUrl$endpoint"
        val requestBody = payload.toString().toRequestBody(jsonMediaType)

        val httpRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Accept", "application/json")
            .build()

        val response = try {
            client.newCall(httpRequest).execute()
        } catch (e: IOException) {
            throw TransportException("failed to call $endpoint: ${e.message}", e)
        }

        response.use { resp ->
            val bodyString = resp.body?.string() ?: ""

            if (!resp.isSuccessful) {
                throw ApiException(resp.code, bodyString, endpoint)
            }

            return try {
                JSONObject(bodyString)
            } catch (e: JSONException) {
                throw DecodeException("$endpoint returned invalid JSON", e)
            }
        }
    }

    private fun normalizeBaseUrl(url: String): String {
        val trimmed = url.trim().trimEnd('/')
        if (trimmed.isEmpty()) {
            throw ConfigurationException("base_url must be a non-empty string")
        }
        val uri = try {
            URI.create(trimmed)
        } catch (e: Exception) {
            throw ConfigurationException("base_url must be a valid HTTP or HTTPS URL")
        }
        if (uri.scheme !in listOf("http", "https") || uri.host.isNullOrEmpty()) {
            throw ConfigurationException("base_url must be an absolute http or https URL")
        }
        return trimmed
    }
}
