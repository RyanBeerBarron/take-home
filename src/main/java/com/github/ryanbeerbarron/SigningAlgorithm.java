package com.github.ryanbeerbarron;

import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.lang.System.getenv;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public interface SigningAlgorithm {

    byte[] sign(byte[] payload);

    boolean verify(byte[] payload, byte[] signature);

    SigningAlgorithm hmac = new SigningAlgorithm() {
        static Mac hmac256;

        {
            try {
                hmac256 = Mac.getInstance("HmacSHA256");
                String key = Optional.ofNullable(getProperty("hmac.key"))
                        .or(() -> Optional.ofNullable(getenv("HMAC_KEY")))
                        .orElse(null);
                if (key == null) {
                    System.err.println(
                            "No hmac secret key found. Try adding '-Dhmac.key=<MY-SECRET-KEY>' as an argument to add a system property. Or use the environment variable 'HMAC_KEY'");
                    exit(1);
                }

                hmac256.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte[] sign(byte[] payload) {
            return hmac256.doFinal(payload);
        }

        @Override
        public boolean verify(byte[] payload, byte[] signature) {
            return Arrays.equals(hmac256.doFinal(payload), signature);
        }
    };
}
