package ifpe.edu.br.servsimples.servsimples;

import ifpe.edu.br.servsimples.servsimples.controller.MainController;
import ifpe.edu.br.servsimples.servsimples.model.*;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Random;

import static ifpe.edu.br.servsimples.servsimples.ServSimplesApplication.MAIN_TAG;

@SpringBootTest
class UserControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ServSimplesApplication.class);

    private static void logi(String tag, String message) {
        logger.info("[" + MAIN_TAG + "] : [" + tag + "] :" + message);
    }

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
    private final MainController userController;

    @Autowired
    public UserControllerTest(UserRepo userRepo) {
        this.userRepo = userRepo;
        userController = new MainController(userRepo);
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

    @Test
    public void registerServiceTest() {
        final String SERVICE_NAME_MOCK = "NOME DO SERVIÇO MOCK";
        final String COST_TIME_MOCK = "HORA";
        final String COST_VALUE_MOCK = "R$59,99";
        Cost cost = new Cost();
        cost.setTime(COST_TIME_MOCK);
        cost.setValue(COST_VALUE_MOCK);

        registerMockedUser();
        updateMockedUserType(User.UserType.PROFESSIONAL);
        User restoredUser = userRepo.findByCPF(USER_MOCK_CPF);

        Service service = new Service();
        service.setName(SERVICE_NAME_MOCK);
        service.setCost(cost);

        restoredUser.addService(service);

        userController.registerService(restoredUser);
        restoredUser = userRepo.findByCPF(USER_MOCK_CPF);

        // CHECK
        List<Service> services = restoredUser.getServices();
        assert !services.isEmpty();
        assert services.size() == 1;
        assert services.get(0).getCost() != null;
        assert services.get(0).getName().equals(SERVICE_NAME_MOCK);
        assert services.get(0).getCost().getValue().equals(COST_VALUE_MOCK);
        assert services.get(0).getCost().getTime().equals(COST_TIME_MOCK);

        userRepo.delete(restoredUser);
    }

    private void updateMockedUserType(User.UserType userType) {
        User restoredUser = userRepo.findByCPF(USER_MOCK_CPF);
        restoredUser.setUserType(userType);
        userController.updateUser(restoredUser);
    }


    @Deprecated
    public void testeAddEvento() {
        registerMockedUser();
        User restoredUser = userRepo.findByCPF(USER_MOCK_CPF);
        checkMockedUserInfo(restoredUser);

        Service mockedService = getMockedService();
        restoredUser.addService(mockedService);
        userRepo.save(restoredUser);

        User restoredUser2 = userRepo.findByCPF(USER_MOCK_CPF);

        Agenda agenda = restoredUser2.getAgenda();
        Event event = new Event();
        event.setService(mockedService);
        event.setEnd(100L);
        event.setStart(50L);
        event.setDescription("MEU VENTINHO");
        event.setType(Event.TYPE_PUBLISH);
        agenda.setEvent(event);
        userRepo.save(restoredUser);

        User restoredUser3 = userRepo.findByCPF(USER_MOCK_CPF);
        userRepo.delete(restoredUser3);
    }

    private Event getMockedPublishEventFromService(Service mockedService) {
        Random r = new Random();
        Event event = new Event();
        event.setType(Event.TYPE_PUBLISH);
        event.setStart(r.nextLong());
        event.setEnd(r.nextLong());
        event.setDescription("FORNECIMENTO DE " + r.nextInt());
        event.setService(mockedService);
        return event;
    }

    private Service getMockedService() {
        Random r = new Random();
        Service service = new Service();
        service.setName("LAVADEIRA:" + r.nextInt());
        service.setCost(getMockedCost());
        return service;
    }

    private Cost getMockedCost() {
        Random r = new Random();
        Cost cost = new Cost();
        cost.setTime("hora");
        cost.setValue("R$" + r.nextInt());
        return cost;
    }

    private void checkMockedUserInfo(User user) {
        assert user != null;
        assert user.getName().equals(USER_MOCK_NAME);
        assert user.getCPF().equals(USER_MOCK_CPF);
        assert user.getPassword().equals(USER_MOCK_PASSWORD);
        assert user.getUserName().equals(USER_MOCK_USERNAME);
        assert user.getUserType().equals(USER_MOCK_TYPE);
    }

    private void checkEvent(Event reference, Event test) {
        assert test != null;
        assert reference.getEnd().equals(test.getEnd());
        assert reference.getStart().equals(test.getStart());
        assert reference.getDescription().equals(test.getDescription());
        assert reference.getType() == test.getType();
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