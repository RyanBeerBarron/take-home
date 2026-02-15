package com.github.ryanbeerbarron;

import java.util.TreeMap;

import io.javalin.*;
import io.javalin.http.*;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public class Api {

    static void main() {
        Javalin.create()
                .post("/encrypt", EncodingEndpoints::encrypt)
                .post("/decrypt", EncodingEndpoints::decrypt)
                .post("/sign", SigningEndpoints::sign)
                .post("/verify", SigningEndpoints::verify)
                .exception(InvalidJsonException.class, (ex, ctx) -> ctx.status(HttpStatus.BAD_REQUEST).result("Could not parse body, reason: " + ex.getMessage()))
                .exception(JacksonException.class, (ex, ctx) -> ctx.status(HttpStatus.BAD_REQUEST).result("Could not parse body, reason: " + ex.getMessage()))
                .start(8000);
    }

    // Globals
    static final Encoding encoding;
    static final SigningAlgorithm signer;
    static final JsonMapper mapper;

    static {
        mapper = JsonMapper.builder()
                // Taken from stackoverflow
                //
                .nodeFactory(new JsonNodeFactory() {
                    @Override
                    public ObjectNode objectNode() {
                        return new ObjectNode(this, new TreeMap<>());
                    }
                })
                .build();
        encoding = Encoding.base64;
        signer = SigningAlgorithm.hmac;
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
