package com.rauio.ZhihuiDangjiang.utils;

import java.security.MessageDigest;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {

    private static final String ALGORITHM_SHA256 = "SHA-256";
    private static final int BUFFER_SIZE = 8192;

    public static String calculateSHA256(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM_SHA256);

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }

        byte[] hashBytes = digest.digest();

        return bytesToHex(hashBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
