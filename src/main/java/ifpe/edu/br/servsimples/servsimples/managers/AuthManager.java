package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.InterfacesWrapper;
import ifpe.edu.br.servsimples.servsimples.autentication.Token;
import ifpe.edu.br.servsimples.servsimples.controller.MainController;
import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.utils.CryptoHelper;
import org.springframework.http.ResponseEntity;

import java.util.Date;

public class AuthManager extends Manager {

    private static final String MASTER_KEY = "serv?simpl3sK3yifp3202eT@d5w1ll#";
    private static final String TAG = AuthManager.class.getSimpleName();

    private static final long ONE_DAY = 1000L * 60L * 60L * 24L;
    private static final long ONE_HOUR = 1000L * 60L * 60;

    public static final int TOKEN_INVALID = 200;
    public static final int TOKEN_VALID = 201;
    public static final int TOKEN_NOT_PRESENT = 202;
    public static final int USERNAME_INVALID = 203;
    public static final int PASSWORD_INVALID = 204;
    public static final int USER_NOT_LOGGED_IN = 205;
    public static final int TOKEN_EXPIRED = 206;
    public static final int USER_ALREADY_LOGGED_IN = 207;
    public static final int USER_INFO_MATCH = 208;
    public static final int USER_INFO_NOT_MATCH = 209;
    public static final int TOKEN_DECRYPT_FAILURE = 210;

    private static AuthManager instance;

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    private AuthManager() {

    }

    /**
     * This method verifies if the token is valid, if so
     * the method InterfacesWrapper.ITokenValidation#onSuccess() is called.
     * However, the response is based on the token validation issue.
     *
     * @param func                the routine that mus be executed in case of success token validation
     * @param tokenValidationCode the code that indicates the token validation status
     * @return an object with request information to be sent to user
     */
    public ResponseEntity<String> handleTokenValidation(InterfacesWrapper.ITokenValidation func,
                                                        int tokenValidationCode) {
        ResponseEntity<String> response = null;
        switch (tokenValidationCode) {
            case AuthManager.TOKEN_VALID -> {
                Object result = func.onSuccess();
                response = getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.OK, result);
            }
            case AuthManager.TOKEN_NOT_PRESENT ->
                    response = getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.TOKEN_NOT_PRESENT,
                            MainController.getErrorMessageByCode(tokenValidationCode));
            case AuthManager.TOKEN_DECRYPT_FAILURE ->
                    response = getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.TOKEN_DECRYPT_FAILURE,
                            MainController.getErrorMessageByCode(tokenValidationCode));
            case AuthManager.USERNAME_INVALID ->
                    response = getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USERNAME_INVALID,
                            MainController.getErrorMessageByCode(tokenValidationCode));
            case AuthManager.PASSWORD_INVALID ->
                    response = getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.PASSWORD_INVALID,
                            MainController.getErrorMessageByCode(tokenValidationCode));
            case AuthManager.TOKEN_EXPIRED ->
                    response = getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.TOKEN_EXPIRED,
                            MainController.getErrorMessageByCode(tokenValidationCode));
        }
        return response;
    }

    public Token createTokenForUser(User user) {
        return new Token.Builder(user)
                .addState(true)
                .addTimeStamp(new Date().getTime())
                .build();
    }

    @Deprecated
    public int getTokenValidationCodeForUser(User userFromRequest, User restoredUser) {
        String token = userFromRequest.getTokenString();
        if (token == null || token.isEmpty()) {
            return TOKEN_NOT_PRESENT;
        }
        String decrypt = CryptoHelper.decrypt(token);
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
        if (delta > (ONE_HOUR)) return TOKEN_EXPIRED;
        return TOKEN_VALID;
    }


    /**
     * This API is a workaround to verify the token just when updating a user
     *
     * @param userFromRequest the user that came from request
     * @param restoredUser    user from db
     * @return the token validation code
     */
    @Deprecated
    public int getTokenValidationCodeForUpdateUser(User userFromRequest, User restoredUser) {
        String token = userFromRequest.getTokenString();
        if (token == null || token.isEmpty()) {
            return TOKEN_NOT_PRESENT;
        }
        String decrypt = CryptoHelper.decrypt(token);
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

        if (!username.equals(restoredUser.getUserName()))
            return USERNAME_INVALID;
        if (!password.equals(restoredUser.getPassword()))
            return PASSWORD_INVALID;
        if (!isLoggedIn) return USER_NOT_LOGGED_IN;

        Long timeStampLong = Long.valueOf(timestamp);
        Long now = new Date().getTime();
        long delta = now - timeStampLong;
        if (delta > (ONE_HOUR)) return TOKEN_EXPIRED;
        return TOKEN_VALID;
    }

    public int getLoginValidationCode(User user, User restoredUser) {
        if (restoredUser.getPassword().equals(user.getPassword()) &&
                restoredUser.getUserName().equals(user.getUserName())) {
            return USER_INFO_MATCH;
        } else {
            return USER_INFO_NOT_MATCH;
        }
    }

    public int getTokenValidationCode(User restoredUser, String token) {
        if (token == null || token.isEmpty()) {
            return TOKEN_NOT_PRESENT;
        }

        String decryptedToken = CryptoHelper.decrypt(token);
        if (decryptedToken == null) {
            return TOKEN_DECRYPT_FAILURE;
        }

        String username = getUserNameFromDecryptedToken(decryptedToken);
        String password = getPasswordFromDecryptedToken(decryptedToken);

        if (!restoredUser.getUserName().equals(username)) return USERNAME_INVALID;
        if (!restoredUser.getPassword().equals(password)) return PASSWORD_INVALID;

        Long timeStampLong = Long.valueOf(getTimestampFromDecryptedToken(decryptedToken));
        Long now = new Date().getTime();
        long delta = now - timeStampLong;
        if (delta > (ONE_HOUR)) return TOKEN_EXPIRED;

        if (!getSessionStateFromDecryptedToken(decryptedToken)) return USER_NOT_LOGGED_IN;

        return TOKEN_VALID;
    }

    private String getUserNameFromDecryptedToken(String decryptedToken) {
        int usernameStartIndex = decryptedToken.indexOf("%");
        int passwordStartIndex = decryptedToken.indexOf(":");
        return decryptedToken.substring(usernameStartIndex + 1, passwordStartIndex);
    }

    private String getPasswordFromDecryptedToken(String decryptedToken) {
        int passwordStartIndex = decryptedToken.indexOf(":");
        int timestampStartIndex = decryptedToken.indexOf("*");
        return decryptedToken.substring(passwordStartIndex + 1, timestampStartIndex);
    }

    private String getTimestampFromDecryptedToken(String decryptedToken) {
        int timestampStartIndex = decryptedToken.indexOf("*");
        int stateStartIndex = decryptedToken.indexOf("$");
        return decryptedToken.substring(timestampStartIndex + 1, stateStartIndex);
    }

    private boolean getSessionStateFromDecryptedToken(String decryptedToken) {
        int stateStartIndex = decryptedToken.indexOf("$");
        int stateEndIndex = decryptedToken.indexOf("#");
        String state = decryptedToken.substring(stateStartIndex + 1, stateEndIndex);
        return state.equals("true");
    }
}