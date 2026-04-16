package com.rounak.consistent_hashing.util;
import java.security.MessageDigest;

public class HashUtil {

    public static long hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes());

            long hash = 0;

            // take first 4 bytes → 32-bit hash
            for (int i = 0; i < 4; i++) {
                hash = (hash << 8) | (digest[i] & 0xFF);
            }

            return hash & 0xffffffffL;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}