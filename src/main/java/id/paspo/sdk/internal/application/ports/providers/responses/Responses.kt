package id.paspo.sdk.internal.application.ports.providers.responses

import org.json.JSONObject

internal data class GetKeyProviderResponse(
    val key: String,
    val validationWindow: String
)

internal data class ValidateProviderResponse(
    val status: String,
    val dataType: String?,
    val dataValue: String?,
    val phoneData: JSONObject?,
    val deviceData: JSONObject?
)
