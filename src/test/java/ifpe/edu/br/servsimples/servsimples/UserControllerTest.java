package ifpe.edu.br.servsimples.servsimples;

import ifpe.edu.br.servsimples.servsimples.controller.UserController;
import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserControllerTest {

    private static final String TAG = UserControllerTest.class.getSimpleName();

    public static final String EVENT_MOCK_DESCRIPTION = "DESCRIPTION";
    public static final long EVENT_MOCK_START = 0L;
    public static final long EVENT_MOCK_END = 1000L;
    public static final int EVENT_MOCK_TYPE = 1;
    public static final User.UserType USER_MOCK_TYPE = User.UserType.USER;
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
        registerMockedUser();
        User restoredUser = userRepo.findByCPF(USER_MOCK_CPF);
        checkMockedUserInfo(restoredUser);
        userRepo.delete(restoredUser);
    }

    @Test
    public void unregisterUserTest() {
        registerMockedUser();
        User restoredUserBefore = userRepo.findByCPF(USER_MOCK_CPF);
        checkMockedUserInfo(restoredUserBefore);
        userController.unregisterUser(restoredUserBefore);
        User restoredUserAfter = userRepo.findByCPF(USER_MOCK_CPF);
        assert restoredUserAfter == null;
    }

    private void checkMockedUserInfo(User user) {
        assert user != null;
        assert user.getName().equals(USER_MOCK_NAME);
        assert user.getCPF().equals(USER_MOCK_CPF);
        assert user.getPassword().equals(USER_MOCK_PASSWORD);
        assert user.getUserName().equals(USER_MOCK_USERNAME);
        assert user.getUserType().equals(USER_MOCK_TYPE);
    }

    private void registerMockedUser() {
        userController.registerUser(getMockUser());
    }

    private User getMockUser() {
        User user = new User();
        user.setCPF(USER_MOCK_CPF);
        user.setName(USER_MOCK_NAME);
        user.setPassword(USER_MOCK_PASSWORD);
        user.setUserName(USER_MOCK_USERNAME);
        user.setUserType(USER_MOCK_TYPE);
        return user;
    }
}