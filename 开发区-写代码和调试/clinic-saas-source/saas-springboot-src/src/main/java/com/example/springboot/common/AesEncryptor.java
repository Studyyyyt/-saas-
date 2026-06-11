package com.example.springboot.common;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AesEncryptor {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final byte[] IV = new byte[16]; // 固定零IV，简化实现
    private static final String KEY = System.getProperty("ai.encryption.key", "shuao-clinic-ai-key-2026");

    private static SecretKeySpec getKeySpec() {
        String key = KEY;
        if (key.length() < 16) {
            key = String.format("%-16s", key).replace(' ', '0');
        } else if (key.length() < 24) {
            key = String.format("%-24s", key).replace(' ', '0');
        } else if (key.length() < 32) {
            key = String.format("%-32s", key).replace(' ', '0');
        } else {
            key = key.substring(0, 32);
        }
        return new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return plainText;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKeySpec(), new IvParameterSpec(IV));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES 加密失败", e);
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) return cipherText;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKeySpec(), new IvParameterSpec(IV));
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 解密失败时返回原文（兼容旧数据未加密的情况）
            return cipherText;
        }
    }
}
