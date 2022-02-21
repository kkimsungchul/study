//package com.sungchul.keycloak.spi.helper;
//
//
//
//import com.sungchul.keycloak.spi.CustomKeycloakPasswordEncryptor;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.codec.binary.Hex;
//import javax.crypto.Cipher;
//import javax.crypto.SecretKey;
//import javax.crypto.SecretKeyFactory;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.PBEKeySpec;
//import javax.crypto.spec.SecretKeySpec;
//import java.security.spec.KeySpec;
//
//
///**
// * Date: 2018-02-28
// */
//public class EncryptionHelperBackup {
//
//
//    public static String encrypt(String salt, String iv, String passphrase, String text, int iterationCount, int keySize) throws Exception {
//        ClassLoader moduleClassLoader = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(EncryptionHelper.class.getClassLoader());
//        //add here the code where apache configuration2 is used, that is where configuration is built
//        Thread.currentThread().setContextClassLoader(moduleClassLoader);
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), Hex.decodeHex(salt.toCharArray()), iterationCount, keySize);
//        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
//        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
//        return new String((Base64.encodeBase64(results)));
//    }
//
//    public static String encrypt(String text) throws Exception {
//        String iv = "736563757275735f5f736167756e6a61";
//        String salt = "68616e61736f6674";
//        String passPhrase = "1234";
//        return encrypt(salt, iv, passPhrase, text, 100, 128);
//    }
//
//
//    public static String decrypt(String salt, String iv, String passphrase, String ciphertext, int iterationCount, int keySize) throws Exception {
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), Hex.decodeHex(salt.toCharArray()), iterationCount, keySize);
//        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
//        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
//        byte[] decrypted = cipher.doFinal(Base64.decodeBase64(ciphertext));
//        return new String(decrypted, "UTF-8");
//    }
//
//
//    public static String decrypt(String ciphertext) throws Exception {
//        String iv = "736563757275735f5f736167756e6a61";
//        String salt = "68616e61736f6674";
//        String passPhrase = "1234";
//        return decrypt(salt, iv, passPhrase, ciphertext, 100, 128);
//    }
//
//
//
//}
