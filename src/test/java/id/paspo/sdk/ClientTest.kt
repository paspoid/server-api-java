package id.paspo.sdk

import id.paspo.sdk.exceptions.ApiException
import id.paspo.sdk.exceptions.ConfigurationException
import id.paspo.sdk.exceptions.RequestValidationException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: Client

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val baseUrl = mockWebServer.url("/").toString()
        client = Client(
            baseUrl = baseUrl,
            apiKey = "test-api-key",
            apiSecret = "test-api-secret"
        )
    }

    @After
    fun tearDown() {
        client.close()
        mockWebServer.shutdown()
    }

    @Test
    fun testGetKeySuccess() = runBlocking {
        val mockResponseBody = """
            {
                "key": "nonce-12345",
                "validation_window": "30s"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponseBody)
        )

        val response = client.getKey(
            servicePublicId = "service-id-1",
            transactionType = "phones"
        )

        assertEquals("nonce-12345", response.key)
        assertEquals("30s", response.validationWindow)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/ext/get-key", recordedRequest.path)
        assertEquals("POST", recordedRequest.method)
    }

    @Test
    fun testValidateSuccess() = runBlocking {
        val mockResponseBody = """
            {
                "status": "success",
                "data_type": "phone",
                "data_value": "+77000000000"
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(mockResponseBody)
        )

        val response = client.validate(nonce = "nonce-12345")

        assertEquals("success", response.status)
        assertEquals("phone", response.dataType)
        assertEquals("+77000000000", response.dataValue)

        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("/v1/ext/validate", recordedRequest.path)
    }

    @Test(expected = ApiException::class)
    fun testApiErrorHandled(): Unit = runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"message":"service not found"}""")
        )

        client.getKey("missing-srv", "phones")
    }

    @Test(expected = ConfigurationException::class)
    fun testInvalidConfiguration() {
        Client(
            baseUrl = "https://paspo.id",
            apiKey = "",
            apiSecret = "secret"
        )
    }

    @Test(expected = RequestValidationException::class)
    fun testEmptyNonceValidation(): Unit = runBlocking {
        client.validate("")
    }
}
