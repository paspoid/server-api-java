package id.paspo.sdk

import id.paspo.sdk.exceptions.ConfigurationException
import id.paspo.sdk.exceptions.RequestValidationException
import id.paspo.sdk.internal.application.dto.GetKeyInput
import id.paspo.sdk.internal.application.dto.ValidateInput
import id.paspo.sdk.internal.application.usecases.GetKeyUseCase
import id.paspo.sdk.internal.application.usecases.ValidateUseCase
import id.paspo.sdk.internal.infrastructure.adapters.providers.rest.TransactionServiceRestProvider
import id.paspo.sdk.responses.GetKeyResponse
import id.paspo.sdk.responses.ValidateResponse
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import java.io.Closeable
import java.util.concurrent.CompletableFuture

/**
 * Android/Kotlin SDK Client for paspo.id Server API.
 *
 * API credentials must only be used in trusted backend or authorized applications.
 */
class Client @JvmOverloads constructor(
    baseUrl: String,
    apiKey: String,
    apiSecret: String,
    timeoutSeconds: Long = 15,
    customClient: OkHttpClient? = null
) : Closeable {

    private val provider: TransactionServiceRestProvider
    private val getKeyUseCase: GetKeyUseCase
    private val validateUseCase: ValidateUseCase

    init {
        if (apiKey.isBlank()) {
            throw ConfigurationException("api_key must be a non-empty string")
        }
        if (apiSecret.isBlank()) {
            throw ConfigurationException("api_secret must be a non-empty string")
        }

        provider = TransactionServiceRestProvider(
            baseUrl = baseUrl,
            apiKey = apiKey,
            apiSecret = apiSecret,
            timeoutSeconds = timeoutSeconds,
            customClient = customClient
        )
        getKeyUseCase = GetKeyUseCase(provider)
        validateUseCase = ValidateUseCase(provider)
    }

    /**
     * Request a one-time transaction key asynchronously (Kotlin Coroutines).
     */
    suspend fun getKey(
        servicePublicId: String,
        transactionType: String
    ): GetKeyResponse {
        requireNonEmpty("service_public_id", servicePublicId)
        requireNonEmpty("transaction_type", transactionType)

        val output = getKeyUseCase.execute(
            GetKeyInput(
                servicePublicId = servicePublicId,
                transactionType = transactionType
            )
        )
        return GetKeyResponse(
            key = output.key,
            validationWindow = output.validationWindow
        )
    }

    /**
     * Perform one transaction-status check for the supplied nonce asynchronously (Kotlin Coroutines).
     */
    suspend fun validate(nonce: String): ValidateResponse {
        requireNonEmpty("nonce", nonce)

        val output = validateUseCase.execute(ValidateInput(nonce = nonce))
        return ValidateResponse(
            status = output.status,
            dataType = output.dataType,
            dataValue = output.dataValue,
            phoneData = output.phoneData,
            deviceData = output.deviceData
        )
    }

    /**
     * Java-friendly async wrapper for [getKey].
     */
    fun getKeyAsync(
        servicePublicId: String,
        transactionType: String
    ): CompletableFuture<GetKeyResponse> = CompletableFuture.supplyAsync {
        runBlocking { getKey(servicePublicId, transactionType) }
    }

    /**
     * Java-friendly async wrapper for [validate].
     */
    fun validateAsync(nonce: String): CompletableFuture<ValidateResponse> = CompletableFuture.supplyAsync {
        runBlocking { validate(nonce) }
    }

    /**
     * Close internal HTTP resources.
     */
    override fun close() {
        provider.close()
    }

    private fun requireNonEmpty(name: String, value: String) {
        if (value.isBlank()) {
            throw RequestValidationException("$name must be a non-empty string")
        }
    }
}
