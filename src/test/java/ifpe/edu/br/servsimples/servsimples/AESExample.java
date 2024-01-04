package ifpe.edu.br.servsimples.servsimples;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class AESExample {

    private static final String MASTER_KEY = "serv?simpl3sK3yifp3202eT@d5w1ll#";

    public static String encrypt(String content) throws Exception {
        SecretKey secretKey = new SecretKeySpec(MASTER_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(content.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedText) throws Exception {
        SecretKey secretKey = new SecretKeySpec(MASTER_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes);
    }

    public static String generateRandomKey(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[length];
        secureRandom.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    public static void main(String[] args) throws Exception {
        String content = "Hello, AES!";

        String encryptedText = encrypt(content);
        System.out.println("Encrypted Text: " + encryptedText);

        String decryptedText = decrypt("uQPeu0CV4H0PLe6qzEFWYax0lTl7euNLZexsDkVdLI8TJKKVEwKBNhPNcTUtxlmJg/imw0Vxbw3XlhSAqfi6/Nl7+4DmHIwV8DjrnmUz1T5dGqjsF/lhk+88/zZqdemwGR3FR/ozvzM8ZHx9b/jOsGo63h5TgyXax8mObVwxgCFx0vW0QiM0vOhmSpHODnt1Rus2f7znWdFGJkgSmpxQOg==");
        System.out.println("Decrypted Text: " + decryptedText);


        String decrypt = "%4c94485e0c21ae6c41ce1dfe7b6bfacedea5ab6e40a2476f50208e526f506080:4c94485e0c21ace6c41ce1dfe7b6bfacea5ab68e40a2476f50208e526f506080*1697205801957$true#";

        int usernameStartIndex = decrypt.indexOf("%");
        int passwordStartIndex = decrypt.indexOf(":");
        int timestampStartIndex = decrypt.indexOf("*");
        int stateStartIndex = decrypt.indexOf("$");
        int stateEndIndex = decrypt.indexOf("#");

        String username = decrypt.substring(usernameStartIndex + 1, passwordStartIndex);
        String password = decrypt.substring(passwordStartIndex + 1, timestampStartIndex);
        String timestamp = decrypt.substring(timestampStartIndex + 1, stateStartIndex);
        String state = decrypt.substring(stateStartIndex + 1, stateEndIndex);

        System.out.println("");
        System.out.println("");
        System.out.println("username: " + username);
        System.out.println("password: " + password);
        System.out.println("timestamp: " + timestamp);
        System.out.println("state: " + state);
    }
}