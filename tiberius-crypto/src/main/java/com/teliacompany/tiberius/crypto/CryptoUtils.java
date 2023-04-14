package com.teliacompany.tiberius.crypto;

import com.teliacompany.tiberius.crypto.exception.TiberiusCryptoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * Simple util class for encrypting and decrypting strings.
 * Run CryptoApp to generate encrypted passwords
 */
public final class CryptoUtils {
    private static final byte[] SALT = "_SeconS3cretP4Rt".getBytes(StandardCharsets.UTF_8);
    private static final String PBKDF_2_WITH_HMAC_SHA_256 = "PBKDF2WithHmacSHA256";
    private static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5Padding";
    private static final String RSA_ECB_PKCS_8_PADDING = "RSA/ECB/PKCS1Padding";
    private static final String AES = "AES";

    public static String encrypt(String toBeEncrypted, String secret) {
        try {
            return encrypt(toBeEncrypted, getAesKey(secret));
        } catch(GeneralSecurityException e) {
            throw new TiberiusCryptoException("Could not encrypt", e);
        }
    }

    /**
     * Encrypts and base64 encodes provided string using provided secret and generated initialization vector (IV) and a static salt.
     *
     * @param toBeEncrypted - unencrypted string / password
     * @param key           - key to be used
     * @return encrypted and encoded string of IV + toBeEncrypted string
     */
    public static String encrypt(String toBeEncrypted, Key key) {
        try {
            final String transformation = key.getAlgorithm().equals(AES) ? AES_CBC_PKCS_5_PADDING : RSA_ECB_PKCS_8_PADDING;
            Cipher cipher = Cipher.getInstance(transformation);

            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] cipherText = cipher.doFinal(toBeEncrypted.getBytes(StandardCharsets.UTF_8)); //This uses the IV from cipher params (extracted as iv above)

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if(key.getAlgorithm().equals(AES)) {
                AlgorithmParameters params = cipher.getParameters();
                // Let iv be the first bytes
                byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
                outputStream.write(iv);
            }
            outputStream.write(cipherText);

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch(GeneralSecurityException | IOException e) {
            throw new TiberiusCryptoException("Could not encrypt", e);
        }
    }

    public static String decrypt(String base64EncryptedString, String secret) {
        try {
            return decrypt(base64EncryptedString, getAesKey(secret));
        } catch(GeneralSecurityException e) {
            throw new TiberiusCryptoException("Could not encrypt", e);
        }
    }

    /**
     * Decrypt encrypted and base64 encoded string using provided secret.
     *
     * @param base64EncryptedString - encrypted and encoded string / password prefixed by IV
     * @param key                   - key
     * @return decoded and decrypted string / password
     */
    public static String decrypt(String base64EncryptedString, Key key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64EncryptedString);

            byte[] cipherText;
            Cipher cipher;
            if(key.getAlgorithm().equals(AES)) {
                byte[] iv = Arrays.copyOfRange(decoded, 0, 16);
                cipherText = Arrays.copyOfRange(decoded, 16, decoded.length);
                cipher = Cipher.getInstance(AES_CBC_PKCS_5_PADDING);
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            } else {
                cipherText = decoded;
                cipher = Cipher.getInstance(RSA_ECB_PKCS_8_PADDING);
                cipher.init(Cipher.DECRYPT_MODE, key);
            }
            byte[] decrypted = cipher.doFinal(cipherText);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch(GeneralSecurityException e) {
            throw new TiberiusCryptoException("Could not decrypt", e);
        }
    }

    private static Key getAesKey(String secret) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF_2_WITH_HMAC_SHA_256);
        KeySpec spec = new PBEKeySpec(secret.toCharArray(), SALT, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), AES);
    }
}
