package ifpe.edu.br.servsimples.servsimples.controller;

import com.google.gson.Gson;
import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import ifpe.edu.br.servsimples.servsimples.dao.ServiceManager;
import ifpe.edu.br.servsimples.servsimples.dao.UserManager;
import ifpe.edu.br.servsimples.servsimples.model.Service;
import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private static final String TAG = UserController.class.getSimpleName();

    private final UserRepo userRepo;
    private final UserManager mUserManager;
    private final ServiceManager mServiceManager;

    @Autowired
    public UserController(UserRepo userController) {
        this.userRepo = userController;
        mUserManager = new UserManager(userRepo);
        mServiceManager = new ServiceManager();
    }

    @CrossOrigin("*")
    @PostMapping("api/register/user")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerUser");
        int validationCode = mUserManager.getUserValidationCode(user);
        if (validationCode == UserManager.USER_NOT_EXISTS) {
            userRepo.save(user);
            return getResponseEntityFrom(HttpStatus.OK, user);
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(validationCode));
    }

    @CrossOrigin("*")
    @PostMapping("api/unregister/user")
    public ResponseEntity<String> unregisterUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "unregisterUser");
        int validationCode = mUserManager.getUserValidationCode(user);
        if (validationCode == UserManager.USER_EXISTS) {
            User restoredUser = userRepo.findByCPF(user.getCPF());
            userRepo.delete(restoredUser);
            return getResponseEntityFrom(HttpStatus.OK, user);
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(validationCode));
    }

    @CrossOrigin("*")
    @PostMapping("api/update/user")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "updateUser");
        int validationCode = mUserManager.getUserValidationCode(user);
        if (validationCode == UserManager.USER_EXISTS) {
            User restoredUser = userRepo.findByCPF(user.getCPF());
            restoredUser.setUserType(user.getUserType());
            restoredUser.setUserName(user.getUserName());
            restoredUser.setPassword(user.getPassword());
            restoredUser.setName(user.getName());
            userRepo.save(restoredUser);
            return getResponseEntityFrom(HttpStatus.OK, restoredUser);
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(validationCode));
    }

    @CrossOrigin("*")
    @PostMapping("api/register/service")
    public ResponseEntity<String> registerService(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerService: body received:" + new Gson().toJson(user));
        int userValidationCode = mUserManager.getUserValidationCode(user);
        if (userValidationCode == UserManager.USER_EXISTS) {
            int serviceValidationCode = mServiceManager.getServiceValidationCode(user.getServices());
            if (serviceValidationCode == ServiceManager.SERVICE_VALID) {
                User restoredUser = userRepo.findByCPF(user.getCPF());
                restoredUser.addService(user.getServices().get(0));
                userRepo.save(restoredUser);
                return getResponseEntityFrom(HttpStatus.OK, restoredUser);
            }
            return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(serviceValidationCode));
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(userValidationCode));
    }

    @CrossOrigin("*")
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
            case UserManager.ERROR_USERNAME -> "USERNAME ERROR";
            case UserManager.ERROR_CPF -> "CPF ERROR";
            case UserManager.ERROR_PASSWORD -> "PASSWORD ERROR";
            case UserManager.ERROR_NAME -> "NAME ERROR";
            case UserManager.USER_NULL -> "USER IS NULL";
            case UserManager.USER_EXISTS -> "USER EXISTS";
            case UserManager.USER_NOT_EXISTS -> "USER NOT EXISTS";

            case ServiceManager.SERVICE_COST_IS_NULL -> "SERVICE COST IS NULL";
            case ServiceManager.SERVICE_VALUE_ERROR -> "SERVICE VALUE ERROR";
            case ServiceManager.SERVICE_NAME_ERROR -> "SERVICE NAME ERROR";
            case ServiceManager.SERVICE_IS_NULL -> "SERVICE IS NULL";
            case ServiceManager.SERVICE_COST_ERROR -> "SERVICE COST ERROR";
            case ServiceManager.SERVICE_IS_EMPTY -> "SERVICE IS EMPTY";
            case ServiceManager.SERVICE_DUPLICATE -> "SERVICE DUPLICATE";
            case ServiceManager.SERVICE_VALID -> "SERVICE VALID";
            default -> "NOT MAPPED ERROR";
        };
    }
}