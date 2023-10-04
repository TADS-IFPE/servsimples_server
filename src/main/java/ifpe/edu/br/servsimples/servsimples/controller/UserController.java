package ifpe.edu.br.servsimples.servsimples.controller;

import com.google.gson.Gson;
import ifpe.edu.br.servsimples.servsimples.dao.UserManager;
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

@RestController
public class UserController {

    private final UserRepo userRepo;
    private final UserManager mUserManager;

    @Autowired
    public UserController(UserRepo userController) {
        this.userRepo = userController;
        mUserManager = new UserManager(userRepo);
    }

    @CrossOrigin("*")
    @PostMapping("api/register/user")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        int validationCode = mUserManager.getUserValidationCode(user);
        if (validationCode == UserManager.USER_VALID) {
            userRepo.save(user);
            return getResponseEntityFrom(HttpStatus.OK, user);
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN, getErrorMessageByCode(validationCode));
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
            default -> "USER EXISTS";
        };
    }
}