package ifpe.edu.br.servsimples.servsimples.utils;

import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class CryptoHelper {

    private static final String MASTER_KEY = "serv?simpl3sK3yifp3202eT@d5w1ll#";
    private static final String TAG = CryptoHelper.class.getSimpleName();

    public static String encrypt(String content) {
        try {
            SecretKey secretKey = new SecretKeySpec(MASTER_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            ServSimplesApplication.logi(TAG, "encryption error:" + e.getMessage());
            return null;
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            SecretKey secretKey = new SecretKeySpec(MASTER_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            ServSimplesApplication.logi(TAG, "decryption error:" + e.getMessage());
            return null;
        }
    }
}
