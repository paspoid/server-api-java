# paspo.id Server API Android (Kotlin) SDK

The official Android / Kotlin SDK for integrating with the **paspo.id Server API** (Transaction and Authentication Service).

---

## 📦 Installation

Add the dependency to your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("id.paspo:sdk:0.1.0")
}
```

---

## 🚀 Quick Start

```kotlin
import id.paspo.sdk.Client
import id.paspo.sdk.exceptions.PaspoidException
import kotlinx.coroutines.launch

val client = Client(
    baseUrl = "https://paspo.id",
    apiKey = "YOUR_API_KEY",
    apiSecret = "YOUR_API_SECRET"
)

// Inside CoroutineScope (e.g. viewModelScope or lifecycleScope):
lifecycleScope.launch {
    try {
        // 1. Obtain a transaction key
        val keyResponse = client.getKey(
            servicePublicId = "YOUR_SERVICE_PUBLIC_ID",
            transactionType = "phones"
        )
        println("Key: ${keyResponse.key}")
        println("Validation Window: ${keyResponse.validationWindow}")

        // 2. Validate transaction status
        val valResponse = client.validate(keyResponse.key)
        println("Status: ${valResponse.status}")
    } catch (e: PaspoidException) {
        println("SDK Error: ${e.message}")
    }
}
```

---

## 🛠 API Methods

### `client.getKey(servicePublicId: String, transactionType: String): GetKeyResponse`
Requests a temporary transaction key.

### `client.validate(nonce: String): ValidateResponse`
Checks transaction status for the provided nonce.

---

## 🔄 Integration Flow

```mermaid
sequenceDiagram
    autonumber

    actor Backend as Integrator App / Backend
    participant SDK as paspo.id Android SDK
    participant API as paspo.id API

    Note over Backend: Keep API credentials secure

    Backend->>SDK: Client(baseUrl, apiKey, apiSecret)
    Backend->>SDK: getKey(servicePublicId, transactionType)
    SDK->>API: POST /v1/ext/get-key
    API-->>SDK: key + validation_window
    SDK-->>Backend: GetKeyResponse

    Backend->>SDK: validate(nonce)
    SDK->>API: POST /v1/ext/validate
    API-->>SDK: status + optional verified data
    SDK-->>Backend: ValidateResponse
```

---

## 📄 License

Distributed under the MIT License.
