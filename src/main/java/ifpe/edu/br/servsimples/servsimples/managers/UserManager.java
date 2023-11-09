package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;

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
    public static final int MISSING_LOGIN_INFO = 11;
    public static final int LOGIN_INFO_LENGTH_ERROR = 12;
    public static final int USER_INFO_DUPLICATED = 13;


    private final UserRepo userRepo;

    public UserManager(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public int getUserInfoValidationCode(User user) {
        if (user == null) return USER_NULL;
        if (user.getUserName() == null || user.getUserName().isEmpty() || user.getUserName().isBlank() /*|| user.getUserName().length() != 64*/) {
            return ERROR_USERNAME;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()/* || user.getPassword().length() != 64*/) {
            return ERROR_PASSWORD;
        }
        if (user.getCpf() == null || user.getCpf().isEmpty() || user.getCpf().isBlank()/* || user.getCpf().length() != 64*/) {
            return ERROR_CPF;
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            return ERROR_NAME;
        }
        if (user.getPassword().equals(user.getUserName()) || user.getCpf().equals(user.getUserName()) ||
        user.getCpf().equals(user.getPassword())) {
            return USER_INFO_DUPLICATED;
        }
        return USER_VALID;
    }


    public int getLoginInfoValidationCode(User user) {
        String userName = user.getUserName();
        String password = user.getPassword();

        if (userName.isEmpty() || userName.isBlank() || password.isEmpty() || password.isBlank()) {
            return MISSING_LOGIN_INFO;
        }

//        if (userName.length() != 64 || password.length() != 64) {
//            return LOGIN_INFO_LENGTH_ERROR;
//        }

        return USER_VALID;
    }

    public boolean userExists(User user) {

        return false;
    }

    public User getUserByCPF(String cpf) {
        return userRepo.findByCpf(cpf);
    }

    public void removeUser(User restoredUser) {
        userRepo.delete(restoredUser);
    }

    public void updateUser(User restoredUser) {
        save(restoredUser);
    }

    public void save(User user) {
        userRepo.save(user);
    }

    public User getUserByUsername(String userName) {
        return userRepo.findByUserName(userName);
    }
}