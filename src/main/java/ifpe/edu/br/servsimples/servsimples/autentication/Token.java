/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.autentication;

import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.utils.CryptoHelper;

public class Token {

    private final String encryptedToken;

    public String getEncryptedToken() {
        return encryptedToken;
    }

    private Token(Builder builder) {
        long timestamp = builder.timeStamp;
        boolean sessionState = builder.sessionState;
        User user = builder.user;
        this.encryptedToken = CryptoHelper.encrypt(user.getUserName()
                + ":" + user.getPassword() + "*" + timestamp + "$" + sessionState + "#");
    }

    public static class Builder {
        private boolean sessionState;
        private long timeStamp;
        private final User user;

        public Builder(User user) {
            this.user = user;
        }

        public Builder addTimeStamp(long timestamp) {
            this.timeStamp = timestamp;
            return this;
        }

        public Builder addState(boolean isLoggedIn) {
            this.sessionState = isLoggedIn;
            return this;
        }

        public Token build() {
            return new Token(this);
        }
    }
}