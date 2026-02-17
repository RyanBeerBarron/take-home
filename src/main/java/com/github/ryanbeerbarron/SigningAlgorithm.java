package com.github.ryanbeerbarron;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.jetbrains.annotations.Nullable;

public interface SigningAlgorithm {

    byte[] sign(byte[] payload);

    boolean verify(byte[] payload, byte[] signature);

    class HmacSHA256 implements SigningAlgorithm {

        private Mac hmac256;

        public HmacSHA256(@Nullable String secretKey) {
            try {
                this.hmac256 = Mac.getInstance("HmacSHA256");
                secretKey = Optional.ofNullable(secretKey)
                        .or(() -> Optional.ofNullable(getProperty("hmac.key")))
                        .or(() -> Optional.ofNullable(getenv("HMAC_KEY")))
                        .orElse(null);
                if (secretKey == null) {
                    String errorMsg =
                            "No hmac secret key found. Try adding '-Dhmac.key=<MY-SECRET-KEY>' as an argument to add a system property. Or use the environment variable 'HMAC_KEY'";
                    System.err.println(errorMsg);
                    throw new RuntimeException(errorMsg);
                }

                hmac256.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
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
    }
}
