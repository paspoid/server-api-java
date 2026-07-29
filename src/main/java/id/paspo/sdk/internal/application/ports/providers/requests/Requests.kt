package id.paspo.sdk.internal.application.ports.providers.requests

internal data class GetKeyRequest(
    val servicePublicId: String,
    val transactionType: String
)

internal data class ValidateRequest(
    val nonce: String
)
