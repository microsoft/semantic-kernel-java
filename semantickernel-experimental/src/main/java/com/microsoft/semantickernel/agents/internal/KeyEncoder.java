// Copyright (c) Microsoft. All rights reserved.
package com.microsoft.semantickernel.agents.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.microsoft.semantickernel.exceptions.SKException;

import java.util.Base64;

/**
 * Utility to encode a list of string keys to a base-64 encoded hash.
 */
public class KeyEncoder {
        /**
         * Produces a base-64 encoded hash for a set of input strings.
         *
         * @param keys A set of input strings
         * @return A base-64 encoded hash
         */
        public static String generateHash(List<String> keys) {
                final MessageDigest digest;
                try {
                        digest = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                        // if this happens, something is very wrong with the JVM
                        throw new SKException("Failed to generate hash", e);
                }
                String key = String.join(":", keys);
                byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(hash);
        }
}
