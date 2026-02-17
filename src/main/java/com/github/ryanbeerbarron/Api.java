package com.github.ryanbeerbarron;

import io.javalin.*;
import io.javalin.http.*;
import java.math.BigDecimal;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.ObjectNode;

public class Api {

    static void main() {
        createServer().start(8000);
    }

    public static Javalin createServer() {
        return createServer(null);
    }

    public static Javalin createServer(String secretKey) {
        signer = new SigningAlgorithm.HmacSHA256(secretKey);
        return Javalin.create()
                .post("/encrypt", EncodingEndpoints::encrypt)
                .post("/decrypt", EncodingEndpoints::decrypt)
                .post("/sign", SigningEndpoints::sign)
                .post("/verify", SigningEndpoints::verify)
                .exception(InvalidJsonException.class, (ex, ctx) -> ctx.status(HttpStatus.BAD_REQUEST)
                        .result("Could not parse body, reason: " + ex.getMessage()))
                .exception(JacksonException.class, (ex, ctx) -> ctx.status(HttpStatus.BAD_REQUEST)
                        .result("Could not parse body, reason: " + ex.getMessage()));
    }

    // Globals
    static final Encoding encoding = Encoding.base64;
    static SigningAlgorithm signer;
    static final JsonMapper mapper = createMapper();

    public static JsonMapper createMapper() {
        return JsonMapper.builder()
                // To enforce keys being sorted inside a json object, override the default factory
                // Pass one that uses `TreeMap` which sorts its keys.
                .nodeFactory(new JsonNodeFactory() {
                    @Override
                    public ObjectNode objectNode() {
                        return new ObjectNode(this, new TreeMap<>());
                    }
                })
                .build();
    }

    public static class InvalidJsonException extends Exception {
        public InvalidJsonException(Throwable cause) {
            super(cause);
        }
    }

    ///  To ensure semantically equivalent JSON produce the same hash/signature/etc... We need to transform it into
    ///  a canonical form.
    ///
    /// Per the [JSON Canonicalization Scheme RFC](https://www.rfc-editor.org/rfc/rfc8785), numbers need to have
    // trailing zeroes removed.<p>
    /// ##### For example:
    /// * `-0` and `0` are the same.
    /// * `1` and `1.0` are the same.
    public static JsonNode bodyAsCanonicalJson(Context context) throws InvalidJsonException {
        JsonNode root;
        try {
            root = mapper.readTree(context.bodyAsBytes());
        } catch (RuntimeException e) {
            throw new InvalidJsonException(e);
        }

        return normalizeNumbers(root);
    }

    public static JsonNode normalizeNumbers(JsonNode json) {
        if (json instanceof ObjectNode object) {
            object.propertyStream().forEach(entry -> {
                object.set(entry.getKey(), normalizeNumbers(entry.getValue()));
            });
            return object;
        } else if (json instanceof ArrayNode array) {
            for (int i = 0; i < array.size(); i++) {
                array.set(i, normalizeNumbers(array.get(i)));
            }
            return array;
        } else if (json.isNumber()) {
            BigDecimal decimal = json.decimalValue().stripTrailingZeros();
            if (decimal.scale() <= 0) {
                return LongNode.valueOf(decimal.longValue());
            }
            return DecimalNode.valueOf(decimal);
        } else {
            return json;
        }
    }
}
