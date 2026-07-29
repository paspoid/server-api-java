package id.paspo.sdk.exceptions

/** Base exception for all paspo.id SDK errors. */
open class PaspoidException(message: String, cause: Throwable? = null) : Exception(message, cause)

/** Raised when client configuration is invalid (e.g. empty API key or invalid base URL). */
class ConfigurationException(message: String) : PaspoidException(message)

/** Raised before sending an invalid request. */
class RequestValidationException(message: String) : PaspoidException(message)

/** Raised when an HTTP request or connection fails. */
class TransportException(message: String, cause: Throwable? = null) : PaspoidException(message, cause)

/** Raised when API returns a non-2xx HTTP status. */
class ApiException(
    val statusCode: Int,
    val responseBody: String,
    val endpoint: String
) : PaspoidException("$endpoint returned status $statusCode: ${responseBody.ifEmpty { "<empty response>" }}")

/** Raised when response payload cannot be decoded. */
class DecodeException(message: String, cause: Throwable? = null) : PaspoidException(message, cause)
