package com.github.ryanbeerbarron;

import static com.github.ryanbeerbarron.Api.*;

import com.github.ryanbeerbarron.Encoding.EncodingException;
import io.javalin.http.*;
import tools.jackson.databind.JsonNode;

public class SigningEndpoints {
    record SignatureResponse(String signature) {}

    public static void sign(Context ctx) throws InvalidJsonException {
        JsonNode body = bodyAsCanonicalJson(ctx);
        String signature = encoding.encode(signer.sign(mapper.writeValueAsBytes(body)));
        ctx.status(HttpStatus.OK).result(mapper.writeValueAsString(new SignatureResponse(signature)));
    }

    record VerifyRequest(String signature, JsonNode data) {}

    public static void verify(Context ctx) throws EncodingException {
        VerifyRequest request = mapper.readValue(ctx.bodyAsBytes(), VerifyRequest.class);

        boolean signatureValid =
                signer.verify(mapper.writeValueAsBytes(request.data), encoding.decode(request.signature));
        ctx.status(signatureValid ? HttpStatus.NO_CONTENT : HttpStatus.BAD_REQUEST);
    }
}
