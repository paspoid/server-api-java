package id.paspo.sdk.internal.application.dto

import org.json.JSONObject

internal data class GetKeyInput(
    val servicePublicId: String,
    val transactionType: String
)

internal data class GetKeyOutput(
    val key: String,
    val validationWindow: String
)

internal data class ValidateInput(
    val nonce: String
)

internal data class ValidateOutput(
    val status: String,
    val dataType: String?,
    val dataValue: String?,
    val phoneData: JSONObject?,
    val deviceData: JSONObject?
)
