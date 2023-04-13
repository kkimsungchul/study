package util;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;


/**
 * Date: 2018-02-28
 */
public class CryptoJSCipherAES128 {


    public static String encrypt(String salt, String iv, String passphrase, String text, int iterationCount, int keySize) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), Hex.decodeHex(salt.toCharArray()), iterationCount, keySize);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
        return new String((Base64.encodeBase64(results)));
    }

    public static String encryptData(String text) throws Exception {
        String iv = "736563757275735f5f736167756e6a61";
        String salt = "68616e61736f6674";
        String passPhrase = "1234";
        return encrypt(salt, iv, passPhrase, text, 100, 128);
    }


    public static String decrypt(String salt, String iv, String passphrase, String ciphertext, int iterationCount, int keySize) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), Hex.decodeHex(salt.toCharArray()), iterationCount, keySize);
        SecretKey key = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(Hex.decodeHex(iv.toCharArray())));
        byte[] decrypted = cipher.doFinal(Base64.decodeBase64(ciphertext));
        return new String(decrypted, "UTF-8");
    }


    public static String decryptData(String ciphertext) throws Exception {
        String iv = "736563757275735f5f736167756e6a61";
        String salt = "68616e61736f6674";
        String passPhrase = "1234";
        return decrypt(salt, iv, passPhrase, ciphertext, 100, 128);
    }



    public static String[] split(String source, String separator) throws NullPointerException {
        String rtnVal[] = (String[]) null;
        int cnt = 1;
        int index = source.indexOf(separator);
        int index0 = 0;
        for (; index >= 0; index = source.indexOf(separator, index + 1))
            cnt++;

        rtnVal = new String[cnt];
        cnt = 0;
        for (index = source.indexOf(separator); index >= 0; ) {
            rtnVal[cnt] = source.substring(index0, index);
            index0 = index + 1;
            index = source.indexOf(separator, index + 1);
            cnt++;
        }

        rtnVal[cnt] = source.substring(index0);
        return rtnVal;
    }
}
