package ifpe.edu.br.servsimples.servsimples;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifpe.edu.br.servsimples.servsimples.controller.MainController;
import ifpe.edu.br.servsimples.servsimples.managers.AvailabilityManager;
import ifpe.edu.br.servsimples.servsimples.model.*;
import ifpe.edu.br.servsimples.servsimples.repo.ServiceRepo;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import ifpe.edu.br.servsimples.servsimples.utils.AppointmentWrapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import static ifpe.edu.br.servsimples.servsimples.ServSimplesApplication.MAIN_TAG;

@SpringBootTest
class UserControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ServSimplesApplication.class);
    public static final String PROFESSIONAL_MOCK_CPF = "02154121454545478";
    public static final String PROFESSIONAL_MOCK_NAME = "Professional user";
    public static final String PROFESSIONAL_MOCK_PASSWORD = "123";
    public static final String PROFESSIONAL_MOCK_USERNAME = "TEST_USERNAME46565465xxvs44";
    public static final User.UserType PROFESSIONAL_MOCK_TYPE = User.UserType.PROFESSIONAL;
    public static final String PROFESSIONAL_MOCK_BIO = "Bio 1";
    public static final String USER_MOCK_BIO = "bio-slksdjdjdkl";

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

    private final ObjectMapper objectMapper;
    private User currentUser;

    @Autowired
    public UserControllerTest(UserRepo userRepo, ServiceRepo serviceRepo, ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
        userController = new MainController(userRepo, serviceRepo);
    }

    @Test
    public void registerUserTest() {
        registerMockedUser();
        User restoredUser = userRepo.findByCpf(USER_MOCK_CPF);
        checkMockedUserInfo(restoredUser);
        userRepo.delete(restoredUser);
    }

    @Test
    public void registerAvailabilityTest() {
        Availability av = new Availability();
        av.setState(Availability.AVAILABLE);
        av.setStartTime(100);
        av.setEndTime(200);

        User professional = getUserFromResponseEntity(userController.registerUser(getMockProfUser()));
        assert professional != null;
        checkMockedProfessionalInfo(professional);

        professional.getAgenda().getAvailabilities().add(av);

        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professional));
        logi(TAG, String.valueOf(responseCode));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;

        userRepo.delete(professional);
    }

    @Test
    public void registerAppointmentTest() {
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(Availability.AVAILABLE);
        professionalAvailability.setStartTime(100);
        professionalAvailability.setEndTime(200);

        User professional = getUserFromResponseEntity(userController.registerUser(getMockProfUser()));
        assert professional != null;

        User client = getUserFromResponseEntity(userController.registerUser(getMockUser()));
        assert client != null;

        User prof = userRepo.findByCpf(professional.getCpf());
        assert prof != null;
        Appointment appointment = new Appointment();
        appointment.setStartTime(100);
        appointment.setEndTime(200);
        appointment.setSubscriberId(prof.getId());

        Availability clientAvailability = new Availability();
        clientAvailability.setAppointment(appointment);
        clientAvailability.setState(Availability.ON_HOLD);
        clientAvailability.setStartTime(100);
        clientAvailability.setEndTime(200);

        client.getAgenda().addAvailability(clientAvailability);

        professional.getAgenda().getAvailabilities().add(professionalAvailability);

        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professional));
        logi(TAG, String.valueOf(responseCode));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;

        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setProfessional(professional);
        appointmentWrapper.setClient(client);

        boolean booleanFromResponseEntity = getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));

        if (booleanFromResponseEntity) {
            User restoredProfessional = userRepo.findByCpf(professional.getCpf());
            User restoredClient = userRepo.findByCpf(client.getCpf());

            assert restoredProfessional != null;
            assert restoredClient != null;

            List<Availability> clientAvailabilities = restoredClient.getAgenda().getAvailabilities();
            for (Availability a : clientAvailabilities) {
                if (a.getStartTime() == clientAvailability.getStartTime() && a.getEndTime() == clientAvailability.getEndTime()) {
                    assert a.getState() == Availability.ON_HOLD;
                    Appointment clientAppointment = a.getAppointment();
                    assert clientAppointment != null;
                    assert clientAppointment.getSubscriberId() == restoredProfessional.getId();
                    assert a.getStartTime() == clientAppointment.getStartTime();
                    assert a.getEndTime() == clientAppointment.getEndTime();
                }
            }

            List<Availability> professionalAv = restoredProfessional.getAgenda().getAvailabilities();
            for (Availability av : professionalAv) {
                if (av.getStartTime() == professionalAvailability.getStartTime() && av.getEndTime() == professionalAvailability.getStartTime()) {
                    assert av.getState() == Availability.ON_HOLD;

                    Appointment profAppointment = av.getAppointment();
                    assert profAppointment != null;
                    assert profAppointment.getSubscriberId() == restoredClient.getId();
                    assert av.getStartTime() == profAppointment.getStartTime();
                    assert av.getEndTime() == profAppointment.getEndTime();
                }
            }
            userRepo.delete(restoredProfessional);
            userRepo.delete(restoredClient);
            assert true;
        } else {
            assert false;
        }
    }

    public Integer getIntegerFromResponseEntity(ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                return objectMapper.readValue(response.getBody(), Integer.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean getBooleanFromResponseEntity(ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                String responseBody = response.getBody();
                return "true".equalsIgnoreCase(responseBody) || "1".equals(responseBody);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public void unregisterUserTest() {
        registerMockedUser();
        User restoredUserBefore = userRepo.findByCpf(USER_MOCK_CPF);
        checkMockedUserInfo(restoredUserBefore);
        userController.unregisterUser(restoredUserBefore);
        User restoredUserAfter = userRepo.findByCpf(USER_MOCK_CPF);
        assert restoredUserAfter == null;
    }


    public void registerServiceTest() {
        final String SERVICE_NAME_MOCK = "NOME DO SERVIÇO MOCK";
        final String COST_TIME_MOCK = "HORA";
        final String COST_VALUE_MOCK = "R$59,99";
        Cost cost = new Cost();
        cost.setTime(COST_TIME_MOCK);
        cost.setValue(COST_VALUE_MOCK);

        registerMockedUser();
        updateMockedUserType(User.UserType.PROFESSIONAL);
        User restoredUser = userRepo.findByCpf(USER_MOCK_CPF);

        Service service = new Service();
        service.setName(SERVICE_NAME_MOCK);
        service.setCost(cost);

        restoredUser.addService(service);

        userController.registerService(restoredUser);
        restoredUser = userRepo.findByCpf(USER_MOCK_CPF);

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
        User restoredUser = userRepo.findByCpf(USER_MOCK_CPF);
        restoredUser.setUserType(userType);
        userController.updateUser(restoredUser);
    }


    public void testeAddEvento() {
        registerMockedUser();
        User restoredUser = userRepo.findByCpf(USER_MOCK_CPF);
        checkMockedUserInfo(restoredUser);

        Service mockedService = getMockedService();
        restoredUser.addService(mockedService);
        userRepo.save(restoredUser);

        User restoredUser2 = userRepo.findByCpf(USER_MOCK_CPF);

        Agenda agenda = restoredUser2.getAgenda();
        Event event = new Event();
        event.setService(mockedService);
        event.setEnd(100L);
        event.setStart(50L);
        event.setDescription("MEU VENTINHO");
        event.setType(Event.TYPE_PUBLISH);
        userRepo.save(restoredUser);

        User restoredUser3 = userRepo.findByCpf(USER_MOCK_CPF);
        userRepo.delete(restoredUser3);
    }


    public void testeee() {
        LocalTime now = LocalTime.now();
        //logger.debug(now.toString());
        assert true;
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
        assert user.getCpf().equals(USER_MOCK_CPF);
        assert user.getPassword().equals(USER_MOCK_PASSWORD);
        assert user.getUserName().equals(USER_MOCK_USERNAME);
        assert user.getUserType().equals(USER_MOCK_TYPE);
        assert user.getBio().equals(USER_MOCK_BIO);
    }

    private void checkMockedProfessionalInfo(User professional) {
        assert professional != null;
        assert professional.getName().equals(PROFESSIONAL_MOCK_NAME);
        assert professional.getCpf().equals(PROFESSIONAL_MOCK_CPF);
        assert professional.getPassword().equals(PROFESSIONAL_MOCK_PASSWORD);
        assert professional.getUserName().equals(PROFESSIONAL_MOCK_USERNAME);
        assert professional.getUserType().equals(PROFESSIONAL_MOCK_TYPE);
        assert professional.getBio().equals(PROFESSIONAL_MOCK_BIO);
    }

    private void checkEvent(Event reference, Event test) {
        assert test != null;
        assert reference.getEnd().equals(test.getEnd());
        assert reference.getStart().equals(test.getStart());
        assert reference.getDescription().equals(test.getDescription());
        assert reference.getType() == test.getType();
    }

    private User registerMockedUser() {
        return getUserFromResponseEntity(userController.registerUser(getMockUser()));
    }

    public User getUserFromResponseEntity(ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            try {
                return objectMapper.readValue(response.getBody(), User.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean userIsNull() {
        return currentUser == null;
    }

    private User getMockUser() {
        User user = new User();
        user.setCpf(USER_MOCK_CPF);
        user.setName(USER_MOCK_NAME);
        user.setPassword(USER_MOCK_PASSWORD);
        user.setUserName(USER_MOCK_USERNAME);
        user.setUserType(USER_MOCK_TYPE);
        user.setBio(USER_MOCK_BIO);
        return user;
    }

    private User getMockProfUser() {
        User user = new User();
        user.setCpf(PROFESSIONAL_MOCK_CPF);
        user.setName(PROFESSIONAL_MOCK_NAME);
        user.setPassword(PROFESSIONAL_MOCK_PASSWORD);
        user.setUserName(PROFESSIONAL_MOCK_USERNAME);
        user.setUserType(PROFESSIONAL_MOCK_TYPE);
        user.setBio(PROFESSIONAL_MOCK_BIO);
        return user;
    }
}