package ifpe.edu.br.servsimples.servsimples.dao;

import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;

public class UserManager {

    public static final int USER_NOT_EXISTS = 0;
    public static final int ERROR_NAME = 1;
    public static final int ERROR_USERNAME = 2;
    public static final int ERROR_PASSWORD = 3;
    public static final int ERROR_CPF = 4;
    public static final int USER_EXISTS = 5;
    public static final int USER_NULL = 6;


    private final UserRepo userRepo;

    public UserManager(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public int getUserValidationCode(User user) {
        if (user == null) return USER_NULL;
        if (user.getUserName() == null || user.getUserName().isEmpty() || user.getUserName().isBlank()) {
            return ERROR_USERNAME;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()) {
            return ERROR_PASSWORD;
        }
        if (user.getCPF() == null || user.getCPF().isEmpty() || user.getCPF().isBlank()) {
            return ERROR_CPF;
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            return ERROR_NAME;
        }
        User restoredUser = userRepo.findByCPF(user.getCPF());
        if (restoredUser != null) {
            return USER_EXISTS;
        }
        return USER_NOT_EXISTS;
    }
}