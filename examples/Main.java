package examples;

import id.paspo.sdk.Client;
import id.paspo.sdk.responses.GetKeyResponse;
import id.paspo.sdk.responses.ValidateResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Map<String, String> env = loadEnv(".env");

        String baseUrl = getRequired(env, "PASPOID_BASE_URL");
        String apiKey = getRequired(env, "PASPOID_API_KEY");
        String apiSecret = getRequired(env, "PASPOID_API_SECRET");
        String servicePublicId = getRequired(env, "PASPOID_SERVICE_PUBLIC_ID");
        String transactionType = env.getOrDefault("PASPOID_TRANSACTION_TYPE", "phones");

        try (Client client = new Client(baseUrl, apiKey, apiSecret)) {
            System.out.println("1. Requesting transaction key...");
            GetKeyResponse keyResp = client.getKeyAsync(servicePublicId, transactionType).get();
            System.out.println("   Key: " + keyResp.getKey());
            System.out.println("   Validation Window: " + keyResp.getValidationWindow());

            System.out.println("2. Validating transaction status...");
            ValidateResponse valResp = client.validateAsync(keyResp.getKey()).get();
            System.out.println("   Status: " + valResp.getStatus());
            System.out.println("   Data Type: " + valResp.getDataType());
            System.out.println("   Data Value: " + valResp.getDataValue());
        } catch (Exception e) {
            System.err.println("Error executing paspo.id SDK: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getRequired(Map<String, String> env, String name) {
        String val = env.get(name);
        if (val == null || val.trim().isEmpty()) {
            val = System.getenv(name);
        }
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException("Required environment variable " + name + " is missing");
        }
        return val.trim();
    }

    private static Map<String, String> loadEnv(String filename) {
        Map<String, String> map = new HashMap<>();
        File file = new File(filename);
        if (!file.exists()) {
            file = new File("../" + filename);
        }
        if (!file.exists()) {
            return map;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String val = line.substring(eqIdx + 1).trim();
                    map.put(key, val);
                }
            }
        } catch (IOException ignored) {
        }
        return map;
    }
}
