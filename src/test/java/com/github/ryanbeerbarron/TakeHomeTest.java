package com.github.ryanbeerbarron;

import static org.junit.jupiter.api.Assertions.*;

import com.github.ryanbeerbarron.SigningEndpoints.SignatureResponse;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import java.util.Map;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class TakeHomeTest {

    static JsonMapper mapper = Api.createMapper();
    static String simpleJson = """
        {
            "foo": "hello",
            "bar": "world",
            "baz": 1
        }
        """;

    static String semanticallyEquivalentJson = """
        {"bar": "world",
                           "baz": 01, "foo": "hello"
        }
        """;
    static String goodbyeJson = """
        {
            "foo": "goodbye",
            "bar": "world",
            "baz": 1
        }
        """;

    static String encryptedJson = """
    {
      "bar": "IndvcmxkIg==",
      "foo": "ImhlbGxvIg==",
      "nonEncryptedString": "non encrypted value",
      "nonEncryptedObject": {
        "firstName": "john",
        "age": 30
      }
    }
    """;

    @Test
    public void encrypting_and_decrypting_should_yield_same_json() {
        JavalinTest.test(createApp(), (_, client) -> {
            Response response = client.post("/encrypt", simpleJson);
            assertEquals(200, response.code());

            String encryptedResponse = response.body().string();
            response = client.post("/decrypt", encryptedResponse);
            assertEquals(200, response.code());

            String decryptedResponse = response.body().string();
            JsonNode original = mapper.readTree(simpleJson);
            JsonNode output = mapper.readTree(decryptedResponse);
            assertEquals(original, output);
        });
    }

    @Test
    public void decrypting_should_leave_nonEncryptedValues_as_is() {
        JavalinTest.test(createApp(), (_, client) -> {
            Response response = client.post("/decrypt", encryptedJson);
            assertEquals(200, response.code());

            Map<String, Object> output = mapper.readValue(response.body().string(), new TypeReference<>() {});
            assertEquals("hello", output.get("foo"));
            assertEquals("world", output.get("bar"));
            assertEquals("non encrypted value", output.get("nonEncryptedString"));
            var expected = Map.<String, Object>of("firstName", "john", "age", 30);
            assertEquals(expected, output.get("nonEncryptedObject"));
        });
    }

    @Test
    public void semantically_equivalent_json_have_same_signature() {
        JavalinTest.test(createApp(), (_, client) -> {
            Response response = client.post("/sign", simpleJson);
            assertEquals(200, response.code());
            String body = response.body().string();
            SignatureResponse firstSignature = mapper.readValue(body, SignatureResponse.class);

            response = client.post("/sign", semanticallyEquivalentJson);
            assertEquals(200, response.code());
            body = response.body().string();
            SignatureResponse secondSignature = mapper.readValue(body, SignatureResponse.class);

            assertEquals(firstSignature.signature(), secondSignature.signature());
        });
    }

    @Test
    public void different_payloads_give_different_signature() {
        JavalinTest.test(createApp(), (_, client) -> {
            Response response = client.post("/sign", simpleJson);
            assertEquals(200, response.code());
            SignatureResponse firstSignature = mapper.readValue(response.body().bytes(), SignatureResponse.class);

            response = client.post("/sign", goodbyeJson);
            assertEquals(200, response.code());
            SignatureResponse secondSignature = mapper.readValue(response.body().bytes(), SignatureResponse.class);

            assertNotEquals(firstSignature.signature(), secondSignature.signature());
        });
    }

    private Javalin createApp() {
        return Api.createServer("very secret key");
    }
}
