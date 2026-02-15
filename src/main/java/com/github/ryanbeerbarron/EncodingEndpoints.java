package com.github.ryanbeerbarron;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.BinaryNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.util.Optional;

import static com.github.ryanbeerbarron.Api.*;

public class EncodingEndpoints {

    public static void encrypt(Context ctx) throws InvalidJsonException {
        JsonNode json = bodyAsJson(ctx);
        if (json instanceof ObjectNode object) {
            object.forEachEntry((key, val) -> object.put(key, encoding.encode(mapper.writeValueAsBytes(val))));
        }

        ctx.status(HttpStatus.OK).result(mapper.writeValueAsString(json));

    }

    public static void decrypt(Context ctx) throws InvalidJsonException {
        JsonNode json = bodyAsJson(ctx);
        if (json instanceof ObjectNode object) {
            object.forEachEntry((key, val) -> {
                Optional<byte[]> decodedString =
                        switch (val) {
                            // `BinaryNode` is a helper from the Jackson library.
                            // If a string value inside the Json is detected to be base64, it automatically decodes it.
                            case BinaryNode binaryNode -> Optional.of(binaryNode.binaryValue());
                            case StringNode stringNode -> {
                                try {
                                    yield Optional.of(encoding.decode(stringNode.stringValue()));
                                }
                                // CodecException means the input string being invalid base64
                                catch (Encoding.EncodingException _) {
                                    yield Optional.empty();
                                }
                            }
                            default -> Optional.empty();
                        };
                decodedString.ifPresent(bytes -> {
                    try {
                        object.set(key, mapper.readTree(bytes));
                    }
                    // The decoded bytes may not represent valid json, in which case, don't override the original
                    // string.
                    catch (JacksonException _) {
                    }
                });
            });
        }

        ctx.status(HttpStatus.OK).result(mapper.writeValueAsBytes(json));
    }
}
