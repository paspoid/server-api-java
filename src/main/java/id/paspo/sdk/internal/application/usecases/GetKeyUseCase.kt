package id.paspo.sdk.internal.application.usecases

import id.paspo.sdk.internal.application.dto.GetKeyInput
import id.paspo.sdk.internal.application.dto.GetKeyOutput
import id.paspo.sdk.internal.application.ports.providers.TransactionServiceProvider
import id.paspo.sdk.internal.application.ports.providers.requests.GetKeyRequest

internal class GetKeyUseCase(
    private val provider: TransactionServiceProvider
) {
    suspend fun execute(input: GetKeyInput): GetKeyOutput {
        val response = provider.getKey(
            GetKeyRequest(
                servicePublicId = input.servicePublicId,
                transactionType = input.transactionType
            )
        )
        return GetKeyOutput(
            key = response.key,
            validationWindow = response.validationWindow
        )
    }
}
