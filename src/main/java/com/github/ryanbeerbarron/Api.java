package com.github.ryanbeerbarron;

import io.javalin.*;
import io.javalin.http.*;
import java.util.TreeMap;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
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
                .enable()
                .build();
    }

    public static class InvalidJsonException extends Exception {
        public InvalidJsonException(Throwable cause) {
            super(cause);
        }
    }

    public static JsonNode bodyAsJson(Context context) throws InvalidJsonException {
        try {
            return mapper.readTree(context.bodyAsBytes());
        } catch (RuntimeException e) {
            throw new InvalidJsonException(e);
        }
    }
}
