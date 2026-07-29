package id.paspo.sdk.responses

import org.json.JSONObject

/** One-time transaction key response. */
data class GetKeyResponse(
    val key: String,
    val validationWindow: String
)

/** Transaction validation status check response. */
data class ValidateResponse(
    val status: String,
    val dataType: String?,
    val dataValue: String?,
    val phoneData: JSONObject?,
    val deviceData: JSONObject?
)
