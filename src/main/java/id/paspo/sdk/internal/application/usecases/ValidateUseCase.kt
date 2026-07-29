package id.paspo.sdk.internal.application.usecases

import id.paspo.sdk.internal.application.dto.ValidateInput
import id.paspo.sdk.internal.application.dto.ValidateOutput
import id.paspo.sdk.internal.application.ports.providers.TransactionServiceProvider
import id.paspo.sdk.internal.application.ports.providers.requests.ValidateRequest

internal class ValidateUseCase(
    private val provider: TransactionServiceProvider
) {
    suspend fun execute(input: ValidateInput): ValidateOutput {
        val response = provider.validate(ValidateRequest(nonce = input.nonce))
        return ValidateOutput(
            status = response.status,
            dataType = response.dataType,
            dataValue = response.dataValue,
            phoneData = response.phoneData,
            deviceData = response.deviceData
        )
    }
}
