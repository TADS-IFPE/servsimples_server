package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import ifpe.edu.br.servsimples.servsimples.autentication.Token;
import ifpe.edu.br.servsimples.servsimples.model.User;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

public class AuthManager {

    private static final String MASTER_KEY = "serv?simpl3sK3yifp3202eT@d5w1ll#";
    private static final String TAG = AuthManager.class.getSimpleName();

    private static final long ONE_DAY = 1000L * 60L * 60L * 24L;

    public static final int TOKEN_INVALID = 200;
    public static final int TOKEN_VALID = 201;
    public static final int TOKEN_NOT_PRESENT = 202;
    public static final int USERNAME_INVALID = 203;
    public static final int PASSWORD_INVALID = 204;
    public static final int USER_NOT_LOGGED_IN = 205;
    public static final int TOKEN_EXPIRED = 206;
    public static final int USER_ALREADY_LOGGED_IN = 207;

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

    private static String decrypt(String encryptedText) {
        try {
            SecretKey secretKey = new SecretKeySpec(MASTER_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes);
        } catch (Exception e) {
            return null;
        }
    }

    public static Token getToken(User user, boolean isUserLoggedIn) {
        return new Token.Builder(user)
                .addState(isUserLoggedIn)
                .addTimeStamp(new Date().getTime())
                .build();
    }

    public static int getTokenValidationCodeForUser(User userFromRequest, User restoredUser) {
        String token = userFromRequest.getTokenString();
        if (token == null || token.isEmpty()) {
            return TOKEN_NOT_PRESENT;
        }
        String decrypt = decrypt(token);
        if (decrypt == null) {
            return TOKEN_INVALID;
        }

        int usernameStartIndex = decrypt.indexOf("%");
        int passwordStartIndex = decrypt.indexOf(":");
        int timestampStartIndex = decrypt.indexOf("*");
        int stateStartIndex = decrypt.indexOf("$");
        int stateEndIndex = decrypt.indexOf("#");

        String username = decrypt.substring(usernameStartIndex + 1, passwordStartIndex);
        String password = decrypt.substring(passwordStartIndex + 1, timestampStartIndex);
        String timestamp = decrypt.substring(timestampStartIndex + 1, stateStartIndex);
        String state = decrypt.substring(stateStartIndex + 1, stateEndIndex);
        boolean isLoggedIn = state.equals("true");

        if (!username.equals(restoredUser.getUserName()) || !username.equals(userFromRequest.getUserName()))
            return USERNAME_INVALID;
        if (!password.equals(restoredUser.getPassword()) || !password.equals(userFromRequest.getPassword()))
            return PASSWORD_INVALID;
        if (!isLoggedIn) return USER_NOT_LOGGED_IN;

        Long timeStampLong = Long.valueOf(timestamp);
        Long now = new Date().getTime();
        long delta = now - timeStampLong;
        if (delta > (ONE_DAY)) return TOKEN_EXPIRED;
        return TOKEN_VALID;
    }

    /**
     * This API is a workaround to verify the token just when updating a user
     *
     * @param userFromRequest the user that came from request
     * @param restoredUser user from db
     *
     * @return the token validation code
     */
    public static int getTokenValidationCodeForUpdateUser(User userFromRequest, User restoredUser) {
        String token = userFromRequest.getTokenString();
        if (token == null || token.isEmpty()) {
            return TOKEN_NOT_PRESENT;
        }
        String decrypt = decrypt(token);
        if (decrypt == null) {
            return TOKEN_INVALID;
        }

        int usernameStartIndex = decrypt.indexOf("%");
        int passwordStartIndex = decrypt.indexOf(":");
        int timestampStartIndex = decrypt.indexOf("*");
        int stateStartIndex = decrypt.indexOf("$");
        int stateEndIndex = decrypt.indexOf("#");

        String username = decrypt.substring(usernameStartIndex + 1, passwordStartIndex);
        String password = decrypt.substring(passwordStartIndex + 1, timestampStartIndex);
        String timestamp = decrypt.substring(timestampStartIndex + 1, stateStartIndex);
        String state = decrypt.substring(stateStartIndex + 1, stateEndIndex);
        boolean isLoggedIn = state.equals("true");

        if (!username.equals(restoredUser.getUserName()) )
            return USERNAME_INVALID;
        if (!password.equals(restoredUser.getPassword()) )
            return PASSWORD_INVALID;
        if (!isLoggedIn) return USER_NOT_LOGGED_IN;

        Long timeStampLong = Long.valueOf(timestamp);
        Long now = new Date().getTime();
        long delta = now - timeStampLong;
        if (delta > (ONE_DAY)) return TOKEN_EXPIRED;
        return TOKEN_VALID;
    }
}