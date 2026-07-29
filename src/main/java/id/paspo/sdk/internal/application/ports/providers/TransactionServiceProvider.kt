package id.paspo.sdk.internal.application.ports.providers

import id.paspo.sdk.internal.application.ports.providers.requests.GetKeyRequest
import id.paspo.sdk.internal.application.ports.providers.requests.ValidateRequest
import id.paspo.sdk.internal.application.ports.providers.responses.GetKeyProviderResponse
import id.paspo.sdk.internal.application.ports.providers.responses.ValidateProviderResponse

internal interface TransactionServiceProvider {
    suspend fun getKey(request: GetKeyRequest): GetKeyProviderResponse
    suspend fun validate(request: ValidateRequest): ValidateProviderResponse
    fun close()
}
