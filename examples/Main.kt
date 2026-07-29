package examples

import id.paspo.sdk.Client
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() = runBlocking {
    val env = loadEnv(".env")

    val baseUrl = env["PASPOID_BASE_URL"] ?: System.getenv("PASPOID_BASE_URL") ?: error("PASPOID_BASE_URL is required")
    val apiKey = env["PASPOID_API_KEY"] ?: System.getenv("PASPOID_API_KEY") ?: error("PASPOID_API_KEY is required")
    val apiSecret = env["PASPOID_API_SECRET"] ?: System.getenv("PASPOID_API_SECRET") ?: error("PASPOID_API_SECRET is required")
    val servicePublicId = env["PASPOID_SERVICE_PUBLIC_ID"] ?: System.getenv("PASPOID_SERVICE_PUBLIC_ID") ?: error("PASPOID_SERVICE_PUBLIC_ID is required")
    val transactionType = env["PASPOID_TRANSACTION_TYPE"] ?: "phones"

    Client(baseUrl, apiKey, apiSecret).use { client ->
        println("1. Requesting transaction key...")
        val keyResp = client.getKey(servicePublicId, transactionType)
        println("   Key: ${keyResp.key}")
        println("   Validation Window: ${keyResp.validationWindow}")

        println("2. Validating transaction status...")
        val valResp = client.validate(keyResp.key)
        println("   Status: ${valResp.status}")
        println("   Data Type: ${valResp.dataType}")
        println("   Data Value: ${valResp.dataValue}")
    }
}

private fun loadEnv(filename: String): Map<String, String> {
    var file = File(filename)
    if (!file.exists()) {
        file = File("../$filename")
    }
    if (!file.exists()) return emptyMap()

    return file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val idx = line.indexOf('=')
            if (idx > 0) line.substring(0, idx).trim() to line.substring(idx + 1).trim() else null
        }.toMap()
}
