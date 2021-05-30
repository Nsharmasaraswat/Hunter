package com.gtp.hunter.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Formatter;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import timber.log.Timber;

public class CryptoUtil {

    public static String hexFormat(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public static String getRandomSalt() {
        SecureRandom sr = null;
        byte[] salt = new byte[32];
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
            sr.nextBytes(salt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hexFormat(salt);
    }

    public static String getPbkdf2(String pwd, byte[] salt) {
        int iterations = 1000;
        char[] chars = pwd.toCharArray();

        byte[] hash = new byte[1];
        try {
            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            hash = skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            Timber.e(e);
        }
        return hexFormat(hash);
    }

    public static byte[] byteFromHex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
