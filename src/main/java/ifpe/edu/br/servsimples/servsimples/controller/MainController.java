package ifpe.edu.br.servsimples.servsimples.controller;

import com.google.gson.Gson;
import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import ifpe.edu.br.servsimples.servsimples.autentication.Token;
import ifpe.edu.br.servsimples.servsimples.managers.AuthManager;
import ifpe.edu.br.servsimples.servsimples.managers.ServiceManager;
import ifpe.edu.br.servsimples.servsimples.managers.UserManager;
import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin("*")
@RestController
public class MainController {

    private static final String TAG = MainController.class.getSimpleName();

    private final UserRepo userRepo;
    private final UserManager mUserManager;
    private final ServiceManager mServiceManager;

    @Autowired
    public MainController(UserRepo userController) {
        this.userRepo = userController;
        mUserManager = new UserManager(userRepo);
        mServiceManager = new ServiceManager();
    }

    @PostMapping("api/register/user")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerUser");
        int validationCode = mUserManager.getUserValidationCode(user);
        if (validationCode == UserManager.USER_VALID) {
            User restoredUser = userRepo.findByCPF(user.getCPF());
            if (restoredUser != null) {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(UserManager.USER_EXISTS));
            }
            Token token = AuthManager.getToken(user, true);
            userRepo.save(user);
            user.setToken(token.getEncryptedToken());
            return getResponseEntityFrom(HttpStatus.OK, user);
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(validationCode));
    }

    @PostMapping("api/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "login:");
        int userValidationCode = mUserManager.getUserValidationCode(user);
        if (userValidationCode == UserManager.USER_VALID) {
            User restoredUser = userRepo.findByCPF(user.getCPF());
            if (restoredUser == null) {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
            }
            int tokenValidationCodeForUser = AuthManager.getTokenValidationCodeForUser(user, restoredUser);
            if (tokenValidationCodeForUser == AuthManager.TOKEN_VALID) {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(AuthManager.USER_ALREADY_LOGGED_IN));
            }
            if (restoredUser.getUserName().equals(user.getUserName()) &&
                    restoredUser.getPassword().equals(user.getPassword())) {
                Token token = AuthManager.getToken(user, true);
                user.setToken(token.getEncryptedToken());
                return getResponseEntityFrom(HttpStatus.OK, user);
            } else {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(tokenValidationCodeForUser));
            }
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                getErrorMessageByCode(userValidationCode));
    }

    @PostMapping("api/get/user")
    public ResponseEntity<String> getUSer(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "getUSer:");
        int userValidationCode = mUserManager.getUserValidationCode(user);
        if (userValidationCode == UserManager.USER_VALID) {
            User restoredUser = userRepo.findByCPF(user.getCPF());
            if (restoredUser == null) {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
            }
            int tokenValidationCodeForUser = AuthManager.getTokenValidationCodeForUser(user, restoredUser);
            if (tokenValidationCodeForUser == AuthManager.TOKEN_VALID) {
                return getResponseEntityFrom(HttpStatus.OK, restoredUser);
            }
            return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                    getErrorMessageByCode(tokenValidationCodeForUser));
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                getErrorMessageByCode(userValidationCode));
    }

    @PostMapping("api/unregister/user")
    public ResponseEntity<String> unregisterUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "unregisterUser");
        int userValidationCode = mUserManager.getUserValidationCode(user);
        if (userValidationCode == UserManager.USER_VALID) {
            User restoredUser = userRepo.findByCPF(user.getCPF());
            if (restoredUser == null) {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
            }
            int tokenValidationCodeForUser = AuthManager.getTokenValidationCodeForUser(user, restoredUser);
            if (tokenValidationCodeForUser == AuthManager.TOKEN_VALID) {
                userRepo.delete(restoredUser);
                return getResponseEntityFrom(HttpStatus.OK, restoredUser);
            } else {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(tokenValidationCodeForUser));
            }
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(userValidationCode));
    }

    @PostMapping("api/update/user")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "updateUser");
        int userValidationCode = mUserManager.getUserValidationCode(user);
        if (userValidationCode == UserManager.USER_VALID) {
            User restoredUser = userRepo.findByCPF(user.getCPF());
            if (restoredUser == null) {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
            }
            int tokenValidationCodeForUser = AuthManager.getTokenValidationCodeForUpdateUser(user, restoredUser);
            if (tokenValidationCodeForUser == AuthManager.TOKEN_VALID) {
                restoredUser.setUserType(user.getUserType());
                restoredUser.setUserName(user.getUserName());
                restoredUser.setPassword(user.getPassword());
                restoredUser.setName(user.getName());
                userRepo.save(restoredUser);
                Token token = AuthManager.getToken(user, true);
                user.setToken(token.getEncryptedToken());
                return getResponseEntityFrom(HttpStatus.OK, user);
            } else {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                        getErrorMessageByCode(tokenValidationCodeForUser));
            }
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(userValidationCode));
    }

    @PostMapping("api/register/service")
    public ResponseEntity<String> registerService(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerService:");
        int userValidationCode = mUserManager.getUserValidationCode(user);
        if (userValidationCode == UserManager.USER_EXISTS) {
            User restoredUser = userRepo.findByCPF(user.getCPF());
            if (!restoredUser.getUserType().equals(User.UserType.PROFESSIONAL)) {
                return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(UserManager.USER_NOT_ALLOWED));
            }
            int serviceValidationCode = mServiceManager.getServiceValidationCode(user.getServices());
            if (serviceValidationCode == ServiceManager.SERVICE_VALID) {
                restoredUser.addService(user.getServices().get(0));
                userRepo.save(restoredUser);
                return getResponseEntityFrom(HttpStatus.OK, restoredUser);
            }
            return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(serviceValidationCode));
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(userValidationCode));
    }

    @PostMapping("api/unregister/service")
    public ResponseEntity<String> unregisterService(@RequestBody User user) {

        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(10));
    }

    private ResponseEntity<String> getResponseEntityFrom(HttpStatus status, Object object) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Gson().toJson(object));
    }

    private String getErrorMessageByCode(int code) {
        return switch (code) {
            // USER
            case UserManager.ERROR_USERNAME -> "USERNAME ERROR";
            case UserManager.ERROR_CPF -> "CPF ERROR";
            case UserManager.ERROR_PASSWORD -> "PASSWORD ERROR";
            case UserManager.ERROR_NAME -> "NAME ERROR";
            case UserManager.USER_NULL -> "USER IS NULL";
            case UserManager.USER_EXISTS -> "USER EXISTS";
            case UserManager.USER_NOT_EXISTS -> "USER NOT EXISTS";
            case UserManager.USER_NOT_ALLOWED -> "USER NOT ALLOWED";
            case UserManager.USER_VALID -> "USER VALID";
            case UserManager.PASSWORD_MISMATCH -> "PASSWORD MISMATCH";
            case UserManager.USERNAME_MISMATCH -> "USERNAME MISMATCH";

            // SERVICE
            case ServiceManager.SERVICE_COST_IS_NULL -> "SERVICE COST IS NULL";
            case ServiceManager.SERVICE_VALUE_ERROR -> "SERVICE VALUE ERROR";
            case ServiceManager.SERVICE_NAME_ERROR -> "SERVICE NAME ERROR";
            case ServiceManager.SERVICE_IS_NULL -> "SERVICE IS NULL";
            case ServiceManager.SERVICE_COST_ERROR -> "SERVICE COST ERROR";
            case ServiceManager.SERVICE_IS_EMPTY -> "SERVICE IS EMPTY";
            case ServiceManager.SERVICE_DUPLICATE -> "SERVICE DUPLICATE";
            case ServiceManager.SERVICE_VALID -> "SERVICE VALID";

            // TOKEN
            case AuthManager.TOKEN_VALID -> "VALID TOKEN";
            case AuthManager.TOKEN_INVALID -> "INVALID TOKEN";
            case AuthManager.USERNAME_INVALID -> "TOKEN USERNAME INVALID";
            case AuthManager.PASSWORD_INVALID -> "TOKEN PASSWORD INVALID";
            case AuthManager.TOKEN_NOT_PRESENT -> "TOKEN NOT PRESENT";
            case AuthManager.TOKEN_EXPIRED -> "TOKEN EXPIRED";
            case AuthManager.USER_NOT_LOGGED_IN -> "USER NOT LOGGED IN";
            case AuthManager.USER_ALREADY_LOGGED_IN -> "USER ALREADY LOGGED IN";

            default -> "NOT MAPPED ERROR";
        };
    }
}