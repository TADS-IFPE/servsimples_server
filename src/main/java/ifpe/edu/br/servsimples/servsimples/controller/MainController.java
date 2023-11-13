package ifpe.edu.br.servsimples.servsimples.controller;

import com.google.gson.Gson;
import ifpe.edu.br.servsimples.servsimples.InterfacesWrapper;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@CrossOrigin("*")
@RestController
public class MainController {

    private static final String TAG = MainController.class.getSimpleName();

    private final UserRepo userRepo;
    private final UserManager mUserManager;
    private final ServiceManager mServiceManager;
    private final AuthManager mAuthManager;

    @Autowired
    public MainController(UserRepo userController) {
        this.userRepo = userController;
        mUserManager = new UserManager(userRepo);
        mServiceManager = new ServiceManager();
        mAuthManager = AuthManager.getInstance();
    }

    @PostMapping("api/register/user")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerUser: " + showUserInfo(user));
        int validationCode = mUserManager.getUserInfoValidationCode(user);
        if (validationCode == UserManager.USER_VALID) {
            User restoredUser = mUserManager.getUserByCPF(user.getCpf());
            if (restoredUser != null) {
                return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_EXISTS,
                        getErrorMessageByCode(UserManager.USER_EXISTS));
            }
            Token token = mAuthManager.getTokenForUser(user, true);
            User responseUser = new User();
            responseUser.setToken(token.getEncryptedToken());
            responseUser.setCpf(user.getCpf());
            responseUser.setUserType(user.getUserType());
            responseUser.setName(user.getName());
            mUserManager.save(user);
            return getResponseEntityFrom(HttpStatus.OK, responseUser);
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                getErrorMessageByCode(validationCode));
    }

    @PostMapping("api/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "login:" + showUserInfo(user));
        int userValidationCode = mUserManager.getLoginInfoValidationCode(user);
        if (userValidationCode == UserManager.USER_VALID) {
            User restoredUser = mUserManager.getUserByUsername(user.getUserName());
            if (restoredUser == null) {
                return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                        getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
            }
            int loginValidationCode = mAuthManager.getLoginValidationCode(user, restoredUser);
            if (loginValidationCode == AuthManager.USER_INFO_NOT_MATCH) {
                return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_INFO_NOT_MATCH,
                        getErrorMessageByCode(AuthManager.USER_INFO_NOT_MATCH));
            }
            Token token = mAuthManager.getTokenForUser(user, true);
            User responseUser = new User();
            responseUser.setToken(token.getEncryptedToken());
            responseUser.setCpf(restoredUser.getCpf());
            responseUser.setName(restoredUser.getName());
            responseUser.setUserType(restoredUser.getUserType());
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.OK, responseUser);
        }
        return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_INVALID,
                getErrorMessageByCode(userValidationCode));
    }

    @PostMapping("api/get/user")
    public ResponseEntity<String> getUSer(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "getUSer:" + showUserInfo(user));
        User restoredUser = mUserManager.getUserByCPF(user.getCpf());
        if (restoredUser == null) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        int tokenValidationCode = mAuthManager.getTokenValidationCode(restoredUser, user.getTokenString());
        return mAuthManager.handleTokenValidation(() -> restoredUser, tokenValidationCode);
    }

    @PostMapping("api/unregister/user")
    public ResponseEntity<String> unregisterUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "unregisterUser");
        User restoredUser = mUserManager.getUserByCPF(user.getCpf());
        if (restoredUser == null) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        int tokenValidationCode = mAuthManager.getTokenValidationCode(restoredUser, user.getTokenString());
        return mAuthManager.handleTokenValidation(() -> {
            mUserManager.removeUser(restoredUser);
            return null;
        }, tokenValidationCode);
    }

    @PostMapping("api/update/user")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "updateUser: " + showUserInfo(user));
        User restoredUser = mUserManager.getUserByCPF(user.getCpf());
        if (restoredUser == null) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        int tokenValidationCode = mAuthManager.getTokenValidationCode(restoredUser, user.getTokenString());
        return mAuthManager.handleTokenValidation(() -> {
            restoredUser.setUserName(user.getUserName());
            restoredUser.setPassword(user.getPassword());
            restoredUser.setUserType(user.getUserType());
            restoredUser.setName(user.getName());
            mUserManager.updateUser(restoredUser);

            Token token = mAuthManager.getTokenForUser(restoredUser, true);
            User responseUser = new User();
            responseUser.setToken(token.getEncryptedToken());
            responseUser.setUserType(user.getUserType());
            responseUser.setUserName(user.getUserName());
            responseUser.setCpf(user.getCpf());
            responseUser.setName(user.getName());
            return responseUser;
        }, tokenValidationCode);
    }

    @PostMapping("api/get/service/categories")
    public ResponseEntity<String> getCategories(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "getCategories:" + showUserInfo(user));
        User restoredUser = mUserManager.getUserByCPF(user.getCpf());
        if (restoredUser == null) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        if (restoredUser.getUserType() != User.UserType.PROFESSIONAL) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_ALLOWED,
                    getErrorMessageByCode(UserManager.USER_NOT_ALLOWED));
        }
        int tokenValidationCode = mAuthManager.getTokenValidationCode(restoredUser, user.getTokenString());
        return mAuthManager.handleTokenValidation(this::getMockCategories, tokenValidationCode);
    }

    private List<String> getMockCategories() {
        return new ArrayList<>(Arrays.asList(
                "Saúde", "Educação", "Lazer"
        ));
    }

    @PostMapping("api/register/service")
    public ResponseEntity<String> registerService(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerService:" + showUserInfo(user));
        User restoredUser = mUserManager.getUserByCPF(user.getCpf());
        if (restoredUser == null) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        if (restoredUser.getUserType() != User.UserType.PROFESSIONAL) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_ALLOWED,
                    getErrorMessageByCode(UserManager.USER_NOT_ALLOWED));
        }

        int serviceValidationCode = mServiceManager.getServiceValidationCode(user.getServices());
        if (serviceValidationCode != ServiceManager.SERVICE_VALID) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.SERVICE_INVALID,
                    getErrorMessageByCode(serviceValidationCode));
        }

        int tokenValidationCode = mAuthManager.getTokenValidationCode(restoredUser, user.getTokenString());
        return mAuthManager.handleTokenValidation(() -> {
            restoredUser.addService(user.getServices().get(0));
            mUserManager.updateUser(restoredUser);
            return user;
        }, tokenValidationCode);
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

    public static String getErrorMessageByCode(int code) {
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
            case UserManager.MISSING_LOGIN_INFO -> "MISSING LOGIN INFO";
            case UserManager.LOGIN_INFO_LENGTH_ERROR -> "LOGIN INFO LENGTH ERROR";
            case UserManager.USER_INFO_DUPLICATED -> "USER INFO DUPLICATED";

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
            case AuthManager.USER_INFO_NOT_MATCH -> "USER INFO NOT MATCH";
            case AuthManager.TOKEN_DECRYPT_FAILURE -> "TOKEN DECRYPT FAILURE";

            default -> "NOT MAPPED ERROR";
        };
    }

    private String showUserInfo(User user) {
        if (user == null) {
            return "user is null";
        }
        String response = "";
        response += "name:" + user.getName();
        response += " username:" + user.getUserName();
        response += " cpf:" + user.getCpf();
        response += " password:" + user.getPassword();
        response += " token:" + user.getToken();
        response += " type:" + user.getUserType();
        return response;
    }
}