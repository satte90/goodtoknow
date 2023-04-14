package com.teliacompany.tiberius.crypto;

import com.teliacompany.tiberius.crypto.exception.TiberiusCryptoException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;

/**
 * Encryptor executable to encrypt passwords. Uses same implementation as tiberius-assets-service is using in runtime.
 * <p>
 * Run with two args:
 * 0: mode [encrypt|decrypt|both]
 * 1: whatever you need encrypted (e.g. password to kuben api)
 * 2: secret that is provided as env/runtime variable for the service.
 * <p>
 * Secret should be different for different environments.
 * See readme for more info (maybe...).
 */
public class CryptoApp {
    private static final List<String> MODES = Arrays.asList("encrypt", "decrypt", "both", "encrypt-rsa", "decrypt-rsa", "both-rsa");
    private static final String ACUA = "" +
            "     _    ____ _   _   _    \n" +
            "    / \\  / ___| | | | / \\   \n" +
            "   / _ \\| |   | | | |/ _ \\  \n" +
            "  / ___ \\ |___| |_| / ___ \\ \n" +
            " /_/   \\_\\____|\\___/_/   \\_\\\n" +
            "                            \n" +
            " - Tiberius Crypto Utils App";


    public static void main(String[] args) throws IOException {
        String mode;
        String input;
        String secretKey = null;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        if(args.length != 3) {
            System.out.println(ACUA);
            System.out.println();
            do {
                System.out.println("Please provide some input...");
                System.out.print("Mode [" + String.join(" | ", MODES) + "]: ");
                mode = br.readLine();
            } while(mode != null && !MODES.contains(mode.toLowerCase()));
            assert mode != null;
            String operation = mode.startsWith("both") ? "encrypted" : mode + "ed";
            System.out.print("To be " + operation + ": ");
            input = br.readLine();
            if(!mode.endsWith("-rsa")) {
                System.out.print("Secret Key: ");
                secretKey = br.readLine();
            }
        } else {
            mode = args[0];
            input = args[1];
            secretKey = args[2];
        }

        System.out.println();
        System.out.println("****************************************************************");
        printResult("Mode", mode);
        printResult("Input", input);
        if(secretKey != null) {
            printResult("Secret Key", secretKey);
        }

        requireNonNull(mode, "mode cannot be null");
        requireNonNull(input, "input to be encrypted/decrypted cannot be null");

        KeyPair keyPair = getKeyPair();
        Key privateKey = getRsaPrivateKey(keyPair);
        Key publicKey = getRsaPublicKey(keyPair);

        try {
            if(mode.equalsIgnoreCase("encrypt")) {
                String encrypted = CryptoUtils.encrypt(input, secretKey);
                printResult("Encrypted result", encrypted);
            } else if(mode.equalsIgnoreCase("encrypt-rsa")) {
                String encrypted = CryptoUtils.encrypt(input, privateKey);
                printResult("Encrypted result", encrypted);
            }else if(mode.equalsIgnoreCase("decrypt")) {
                String decrypted = CryptoUtils.decrypt(input, secretKey);
                printResult("Decrypted result", decrypted);
            } else if(mode.equalsIgnoreCase("decrypt-rsa")) {
                String decrypted = CryptoUtils.decrypt(input, publicKey);
                printResult("Decrypted result", decrypted);
            } else if(mode.equalsIgnoreCase("both")) {
                String encrypted = CryptoUtils.encrypt(input, secretKey);
                String decrypted = CryptoUtils.decrypt(encrypted, secretKey);
                printResult("Encrypted result", encrypted);
                printResult("Decrypted result", decrypted);
            }  else if(mode.equalsIgnoreCase("both-rsa")) {
                String encrypted = CryptoUtils.encrypt(input, privateKey);
                String decrypted = CryptoUtils.decrypt(encrypted, publicKey);
                printResult("Encrypted result", encrypted);
                printResult("Decrypted result", decrypted);
            } else {
                printError("Invalid mode!");
            }
        } catch(TiberiusCryptoException e) {
            printError("Wrong secret (probably)");
        }
        System.out.println("****************************************************************");
    }

    private static void requireNonNull(Object o, String message) {
        if(o == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Some old-school code :)
     */
    private static void printResult(String s, String result) {
        System.out.println(getPaddedString(s, result));
    }

    private static void printError(String error) {
        System.err.println(getPaddedString("ERROR", error));
    }

    /**
     * Some old-school code :)
     */
    private static String getPaddedString(String s, String result) {
        int spaces = 16 - s.length();
        StringBuilder sb = new StringBuilder(s);
        for(int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        sb.append(": ");
        sb.append(result);
        return sb.toString();
    }

    private static KeyPair getKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static Key getRsaPrivateKey(KeyPair keys) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keys.getPrivate().getEncoded());
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static Key getRsaPublicKey(KeyPair keys) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keys.getPublic().getEncoded());
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

}
