package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import org.springframework.http.ResponseEntity;

public class UserManager {

    public static final int USER_VALID = 0;
    public static final int ERROR_NAME = 1;
    public static final int ERROR_USERNAME = 2;
    public static final int ERROR_PASSWORD = 3;
    public static final int ERROR_CPF = 4;
    public static final int USER_EXISTS = 5;
    public static final int USER_NULL = 6;
    public static final int USER_NOT_ALLOWED = 7;
    public static final int PASSWORD_MISMATCH = 8;
    public static final int USERNAME_MISMATCH = 9;
    public static final int USER_NOT_EXISTS = 10;


    private final UserRepo userRepo;

    public UserManager(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public int getUserValidationCode(User user) {
        if (user == null) return USER_NULL;
        if (user.getUserName() == null || user.getUserName().isEmpty() || user.getUserName().isBlank() || user.getUserName().length() != 64) {
            return ERROR_USERNAME;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank() || user.getPassword().length() != 64) {
            return ERROR_PASSWORD;
        }
        if (user.getCPF() == null || user.getCPF().isEmpty() || user.getCPF().isBlank() || user.getCPF().length() != 64) {
            return ERROR_CPF;
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            return ERROR_NAME;
        }
        return USER_VALID;
    }
}