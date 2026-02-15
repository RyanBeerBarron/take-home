package com.github.ryanbeerbarron;

import java.util.Base64;

///   Usually an encoding would encode one string of bytes into another string of bytes, same for the decoding operation.
///   This allows chaining different encoding/decoding one after another.<p>
///
///   But for this assignment, since the goal is to produce a JSON, we need text.
///   Therefore, this interface returns/accepts `String` for convenience.
public interface Encoding {
    String encode(byte[] bytes);

    byte[] decode(String str) throws EncodingException;

    class EncodingException extends Exception {
        public EncodingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    Encoding base64 = new Encoding() {
        private final Base64.Encoder encoder = Base64.getEncoder();
        private final Base64.Decoder decoder = Base64.getDecoder();

        @Override
        public String encode(byte[] bytes) {
            return encoder.encodeToString(bytes);
        }

        @Override
        public byte[] decode(String str) throws EncodingException {
            try {
                return decoder.decode(str);
            } catch (RuntimeException ex) {
                throw new EncodingException(String.format("Invalid base64 string '%s'", str), ex);
            }
        }
    };
}
