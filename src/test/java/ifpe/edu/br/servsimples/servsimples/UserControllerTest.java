package ifpe.edu.br.servsimples.servsimples;

import ifpe.edu.br.servsimples.servsimples.controller.UserController;
import ifpe.edu.br.servsimples.servsimples.model.Agenda;
import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;

@SpringBootTest
class UserControllerTest {

    private static final String TAG = UserControllerTest.class.getSimpleName();

    public static final String EVENT_MOCK_DESCRIPTION = "DESCRIPTION";
    public static final long EVENT_MOCK_START = 0L;
    public static final long EVENT_MOCK_END = 1000L;
    public static final int EVENT_MOCK_TYPE = 1;
    private final String USER_MOCK_CPF = "11111111111";
    private final String USER_MOCK_USERNAME = "USERNAME";
    private final String USER_MOCK_PASSWORD = "PASSWORD";
    private final String USER_MOCK_NAME = "NAME";
    private final UserRepo userRepo;
    private final UserController userController;


    @Autowired
    public UserControllerTest(UserRepo userRepo) {
        this.userRepo = userRepo;
        userController = new UserController(userRepo);
    }


    @Test
    public void registerUserTest() {
        ResponseEntity<String> stringResponseEntity = userController.registerUser(getMockUser());
        ServSimplesApplication.logi(TAG, stringResponseEntity.toString());

        // CHECK INFO
        User restoredUser = userRepo.findByCpf(USER_MOCK_CPF);
        assert restoredUser != null;
        assert restoredUser.getName().equals(USER_MOCK_NAME);
        assert restoredUser.getCpf().equals(USER_MOCK_CPF);
        assert restoredUser.getPassword().equals(USER_MOCK_PASSWORD);
        assert restoredUser.getUsername().equals(USER_MOCK_USERNAME);

        userRepo.delete(restoredUser);
    }

    private User getMockUser() {
        User user = new User();
        user.setCpf(USER_MOCK_CPF);
        user.setName(USER_MOCK_NAME);
        user.setPassword(USER_MOCK_PASSWORD);
        user.setUsername(USER_MOCK_USERNAME);
        return user;
    }
}