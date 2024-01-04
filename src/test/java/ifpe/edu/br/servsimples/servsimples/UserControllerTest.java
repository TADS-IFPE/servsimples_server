/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifpe.edu.br.servsimples.servsimples.controller.MainController;
import ifpe.edu.br.servsimples.servsimples.managers.AvailabilityManager;
import ifpe.edu.br.servsimples.servsimples.managers.ServiceManager;
import ifpe.edu.br.servsimples.servsimples.managers.UserManager;
import ifpe.edu.br.servsimples.servsimples.model.*;
import ifpe.edu.br.servsimples.servsimples.repo.Repository;
import ifpe.edu.br.servsimples.servsimples.repo.ServiceRepo;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import ifpe.edu.br.servsimples.servsimples.utils.AppointmentWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.List;

@SpringBootTest
class UserControllerTest {
    private final MainController userController;
    private final ObjectMapper objectMapper;
    private final Repository mRepository;


    @Autowired
    public UserControllerTest(UserRepo userRepo, ServiceRepo serviceRepo, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        mRepository = Repository.create(userRepo, serviceRepo);
        userController = new MainController(userRepo, serviceRepo);
    }

    @Test
    public void registerUserTest() {
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        User restoredUser = mRepository.getUserByCPF(LOCAL_PROFESSIONAL_CPF);
        mRepository.removeUser(restoredUser);
    }

    @Test
    public void getUserTest() {
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;
        assert professionalFromServerResponse.getAgenda().getAvailabilities().isEmpty();

        User userFromResponseEntity = getUserFromResponseEntity(userController.getUSer(professionalFromServerResponse));
        assert userFromResponseEntity != null;
        assert userFromResponseEntity.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert userFromResponseEntity.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert userFromResponseEntity.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert userFromResponseEntity.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert userFromResponseEntity.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert userFromResponseEntity.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert userFromResponseEntity.getTokenString() == null;

        User restoredUser = mRepository.getUserByCPF(LOCAL_PROFESSIONAL_CPF);
        mRepository.removeUser(restoredUser);
    }

    @Test
    public void cancelAppointmentByClientTest() {
        // Create and register professional
        User mockProfessional = getProfessionalUserMock();
        UserManager profFromServerUM = UserManager.create(getUserFromResponseEntity(userController.registerUser(mockProfessional)));
        assertUserRegistrationInfo(mockProfessional, profFromServerUM.user());

        // Create and register client
        User mockClient = getClientUserMock();
        UserManager clientFromServerUM = UserManager.create(getUserFromResponseEntity(userController.registerUser(mockClient)));
        assertUserRegistrationInfo(mockClient, clientFromServerUM.user());

        // Create and register availability
        final long MOCK_AVAILABILITY_START_TIME = 1702796400000L;
        final long MOCK_AVAILABILITY_END_TIME = 1702839600000L;
        final int MOCK_AVAILABILITY_STATE = Availability.AVAILABLE;
        Availability mockAvailability = getMockAvailability(MOCK_AVAILABILITY_START_TIME, MOCK_AVAILABILITY_END_TIME, MOCK_AVAILABILITY_STATE);
        profFromServerUM.availability(mockAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(profFromServerUM.user()));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;
        List<Availability> rstAv = mRepository.getUserByCPF(profFromServerUM.cpf()).getAgenda().getAvailabilities();
        assert rstAv.size() == 1;
        assertAvailabilityInfo(mockAvailability, rstAv.get(0));

        // Create and register a service
        profFromServerUM.service(getMockService());
        User resultUser = getUserFromResponseEntity(userController.registerService(profFromServerUM.user()));
        assert resultUser != null;
        UserManager profFromDb = UserManager.create(mRepository.getUserByCPF(profFromServerUM.cpf()));
        assert !profFromDb.isNull();
        assert profFromDb.services().size() == 1;
        assertServiceEqualInfo(profFromDb.service(), getMockService());

        // Create and register appointment
        Service restoredService = profFromDb.services().get(0);
        final long APPOINTMENT_START = MOCK_AVAILABILITY_START_TIME;
        final long APPOINTMENT_END = MOCK_AVAILABILITY_END_TIME;
        Appointment localClientAppointment = new Appointment();
        localClientAppointment.setStartTime(APPOINTMENT_START);
        localClientAppointment.setEndTime(APPOINTMENT_END);
        localClientAppointment.setServiceId(restoredService.getId());
        Availability clientAvailability = getMockAvailability(0, 0, 0);
        clientAvailability.setAppointment(localClientAppointment);
        clientAvailability.setState(Availability.ON_HOLD);
        assert !clientFromServerUM.isNull();
        clientFromServerUM.availability(clientAvailability);

        User professionalForRequest = new User();
        professionalForRequest.setCpf(profFromServerUM.cpf());
        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(clientFromServerUM.user());
        appointmentWrapper.setProfessional(professionalForRequest);

        assert getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));
        User clientFromDB = mRepository.getUserByCPF(clientFromServerUM.cpf());
        List<Availability> clientAvailabilities = clientFromDB.getAgenda().getAvailabilities();
        assert clientAvailabilities.size() == 1;
        Appointment restoredClientAppointment = clientAvailabilities.get(0).getAppointment();
        assertAppointmentInfo(localClientAppointment, restoredClientAppointment);

        User professionalFromDB = mRepository.getUserByCPF(profFromServerUM.cpf());
        List<Availability> profAvailabilities = professionalFromDB.getAgenda().getAvailabilities();
        assert profAvailabilities.size() == 1;
        Appointment restoredProfAppointment = profAvailabilities.get(0).getAppointment();
        assertAppointmentInfo(localClientAppointment, restoredProfAppointment);

        assert restoredClientAppointment.getSubscriberId() == professionalFromDB.getId();
        assert restoredProfAppointment.getSubscriberId() == clientFromDB.getId();

        // Cancel appointment by user
        assert getBooleanFromResponseEntity(userController.cancelAppointment(clientFromServerUM.user()));
        clientFromDB = mRepository.getUserByCPF(clientFromServerUM.cpf());
        assert clientFromDB.getAgenda().getAvailabilities().isEmpty();
        professionalFromDB = mRepository.getUserByCPF(profFromServerUM.cpf());
        assert professionalFromDB.getAgenda().getAvailabilities().size() == 1;
        assertAvailabilityInfo(mockAvailability, professionalFromDB.getAgenda().getAvailabilities().get(0));

        // CLEAR DB ==============================================================================
        User professionalByCPF = mRepository.getUserByCPF(profFromServerUM.cpf());
        User clientByCPF = mRepository.getUserByCPF(clientFromServerUM.cpf());

        mRepository.removeUser(professionalByCPF);
        mRepository.removeUser(clientByCPF);
    }

    @Test
    public void cancelAppointmentByProfessionalTest() {
        // Create and register professional
        User mockProfessional = getProfessionalUserMock();
        UserManager profFromServerUM = UserManager.create(getUserFromResponseEntity(userController.registerUser(mockProfessional)));
        assertUserRegistrationInfo(mockProfessional, profFromServerUM.user());

        // Create and register client
        User mockClient = getClientUserMock();
        UserManager clientFromServerUM = UserManager.create(getUserFromResponseEntity(userController.registerUser(mockClient)));
        assertUserRegistrationInfo(mockClient, clientFromServerUM.user());

        // Create and register availability
        final long MOCK_AVAILABILITY_START_TIME = 1702796400000L;
        final long MOCK_AVAILABILITY_END_TIME = 1702839600000L;
        final int MOCK_AVAILABILITY_STATE = Availability.AVAILABLE;
        Availability mockAvailability = getMockAvailability(MOCK_AVAILABILITY_START_TIME, MOCK_AVAILABILITY_END_TIME, MOCK_AVAILABILITY_STATE);
        profFromServerUM.availability(mockAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(profFromServerUM.user()));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;
        List<Availability> rstAv = mRepository.getUserByCPF(profFromServerUM.cpf()).getAgenda().getAvailabilities();
        assert rstAv.size() == 1;
        assertAvailabilityInfo(mockAvailability, rstAv.get(0));

        // Create and register appointment
        final long APPOINTMENT_START = MOCK_AVAILABILITY_START_TIME;
        final long APPOINTMENT_END = MOCK_AVAILABILITY_END_TIME;
        Appointment appointment = new Appointment();
        appointment.setStartTime(APPOINTMENT_START);
        appointment.setEndTime(APPOINTMENT_END);
        Availability clientAvailability = getMockAvailability(0, 0, 0);
        clientAvailability.setAppointment(appointment);
        clientAvailability.setState(Availability.ON_HOLD);
        clientFromServerUM.availability(clientAvailability);

        User professionalForRequest = new User();
        professionalForRequest.setCpf(profFromServerUM.cpf());
        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(clientFromServerUM.user());
        appointmentWrapper.setProfessional(professionalForRequest);

        assert getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));
        User clientFromDB = mRepository.getUserByCPF(clientFromServerUM.cpf());
        List<Availability> clientAvailabilities = clientFromDB.getAgenda().getAvailabilities();
        assert clientAvailabilities.size() == 1;
        User professionalFromDB = mRepository.getUserByCPF(profFromServerUM.cpf());
        List<Availability> profAvailabilities = professionalFromDB.getAgenda().getAvailabilities();
        assert profAvailabilities.size() == 1;
        assertAppointmentInfo(appointment, clientAvailabilities.get(0).getAppointment());
        assertAppointmentInfo(appointment, profAvailabilities.get(0).getAppointment());

        // Update avaiability on request client
        profFromServerUM.availabilities().remove(0);
        profFromServerUM.availability(profAvailabilities.get(0));

        // Cancel appointment by professional
        assert getBooleanFromResponseEntity(userController.cancelAppointment(profFromServerUM.user()));
        clientFromDB = mRepository.getUserByCPF(clientFromServerUM.cpf());
        assert clientFromDB.getAgenda().getAvailabilities().isEmpty();
        professionalFromDB = mRepository.getUserByCPF(profFromServerUM.cpf());
        assert professionalFromDB.getAgenda().getAvailabilities().size() == 1;

        // CLEAR DB ==============================================================================
        User professionalByCPF = mRepository.getUserByCPF(profFromServerUM.cpf());
        User clientByCPF = mRepository.getUserByCPF(clientFromServerUM.cpf());

        mRepository.removeUser(professionalByCPF);
        mRepository.removeUser(clientByCPF);
    }

    private void assertServiceEqualInfo(Service a, Service b) {
        assert a.getName().equals(b.getName());
        assert a.getCategory().equals(b.getCategory());
        assert a.getDescription().equals(b.getDescription());
        Cost costA = a.getCost();
        Cost costB = b.getCost();
        assert costA.getTime().equals(costB.getTime());
        assert costA.getValue().equals(costB.getValue());
    }

    private Service getMockService() {
        Service service = new Service();
        service.setCategory("Lazer");
        service.setName("test service");
        service.setDescription("teste service description");
        Cost cost = new Cost();
        cost.setTime("hour");
        cost.setValue("R$200,00");
        service.setCost(cost);
        return service;
    }

    @Test
    public void registerAvailabilityTest() {
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1702796400000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1702839600000L;
        final int LOCAL_PROFESSIONAL_AVAILABILITY_STATE = Availability.AVAILABLE;

        // Register user
        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        // Register availability
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(LOCAL_PROFESSIONAL_AVAILABILITY_STATE);
        professionalAvailability.setStartTime(LOCAL_PROFESSIONAL_AVAILABILITY_START);
        professionalAvailability.setEndTime(LOCAL_PROFESSIONAL_AVAILABILITY_END);
        professionalFromServerResponse.getAgenda().getAvailabilities().add(professionalAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professionalFromServerResponse));

        // Verify availability
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;
        User byCpf = mRepository.getUserByCPF(professionalFromServerResponse.getCpf());
        assert byCpf != null;
        assert byCpf.getAgenda().getAvailabilities().size() == 1;
        Availability availability = byCpf.getAgenda().getAvailabilities().get(0);
        assert availability != null;
        assert availability.getAppointment() == null;
        assert availability.getState() == LOCAL_PROFESSIONAL_AVAILABILITY_STATE;
        assert availability.getStartTime() == LOCAL_PROFESSIONAL_AVAILABILITY_START;
        assert availability.getEndTime() == LOCAL_PROFESSIONAL_AVAILABILITY_END;
        mRepository.removeUser(byCpf);
    }

    @Test
    public void unregisterAvailabilityWithNoSubscribersTest() {
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1702796400000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1702839600000L;
        final int LOCAL_PROFESSIONAL_AVAILABILITY_STATE = Availability.AVAILABLE;

        // Register user
        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        // Register availability
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(LOCAL_PROFESSIONAL_AVAILABILITY_STATE);
        professionalAvailability.setStartTime(LOCAL_PROFESSIONAL_AVAILABILITY_START);
        professionalAvailability.setEndTime(LOCAL_PROFESSIONAL_AVAILABILITY_END);
        professionalFromServerResponse.getAgenda().getAvailabilities().add(professionalAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professionalFromServerResponse));

        // Verify availability
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;
        User byCpf = mRepository.getUserByCPF(professionalFromServerResponse.getCpf());
        assert byCpf != null;
        assert byCpf.getAgenda().getAvailabilities().size() == 1;
        Availability availability = byCpf.getAgenda().getAvailabilities().get(0);
        assert availability != null;
        assert availability.getAppointment() == null;
        assert availability.getState() == LOCAL_PROFESSIONAL_AVAILABILITY_STATE;
        assert availability.getStartTime() == LOCAL_PROFESSIONAL_AVAILABILITY_START;
        assert availability.getEndTime() == LOCAL_PROFESSIONAL_AVAILABILITY_END;

        Integer unregisterResponseCode = getIntegerFromResponseEntity(userController.unregisterAvailability(professionalFromServerResponse));
        assert unregisterResponseCode == 0;
        byCpf = mRepository.getUserByCPF(professionalFromServerResponse.getCpf());
        assert byCpf.getAgenda().getAvailabilities().isEmpty();
        mRepository.removeUser(byCpf);
    }

    @Test
    public void unregisterAvailabilityWithOneSubscriberTest() {
        // PROFESSIONAL INFO
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        // REGISTER PROFESSIONAL
        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        // CHECK PROFESSIONAL INFO
        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        // PROFESSIONAL AVAILABILITY INFO
        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1702796400000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1702839600000L;
        final int LOCAL_PROFESSIONAL_AVAILABILITY_STATE = Availability.AVAILABLE;

        // REGISTER PROFESSIONAL AVAILABILITY
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(LOCAL_PROFESSIONAL_AVAILABILITY_STATE);
        professionalAvailability.setStartTime(LOCAL_PROFESSIONAL_AVAILABILITY_START);
        professionalAvailability.setEndTime(LOCAL_PROFESSIONAL_AVAILABILITY_END);
        professionalFromServerResponse.getAgenda().getAvailabilities().add(professionalAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professionalFromServerResponse));

        // CHECK AVAILABILITY INFO
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;
        User byCpf = mRepository.getUserByCPF(professionalFromServerResponse.getCpf());
        assert byCpf != null;
        assert byCpf.getAgenda().getAvailabilities().size() == 1;
        Availability availability = byCpf.getAgenda().getAvailabilities().get(0);
        assert availability != null;
        assert availability.getAppointment() == null;
        assert availability.getState() == LOCAL_PROFESSIONAL_AVAILABILITY_STATE;
        assert availability.getStartTime() == LOCAL_PROFESSIONAL_AVAILABILITY_START;
        assert availability.getEndTime() == LOCAL_PROFESSIONAL_AVAILABILITY_END;

        // CLIENT INFO
        final String LOCAL_CLIENT_CPF = "94950949434943";
        final User.UserType LOCAL_CLIENT_TYPE = User.UserType.USER;
        final String LOCAL_CLIENT_NAME = "client name";
        final String LOCAL_CLIENT_USERNAME = "client user name";
        final String LOCAL_CLIENT_BIO = "client user bio";
        final String LOCAL_CLIENT_PASSWORD = "client user password";

        // REGISTER CLIENT
        User localClient = new User();
        localClient.setCpf(LOCAL_CLIENT_CPF);
        localClient.setUserType(LOCAL_CLIENT_TYPE);
        localClient.setName(LOCAL_CLIENT_NAME);
        localClient.setUserName(LOCAL_CLIENT_USERNAME);
        localClient.setBio(LOCAL_CLIENT_BIO);
        localClient.setPassword(LOCAL_CLIENT_PASSWORD);

        // CHECK CLIENT INFO
        User clientFromServerResponse = getUserFromResponseEntity(userController.registerUser(localClient));
        assert clientFromServerResponse != null;
        assert clientFromServerResponse.getCpf().equals(LOCAL_CLIENT_CPF);
        assert clientFromServerResponse.getUserType().equals(LOCAL_CLIENT_TYPE);
        assert clientFromServerResponse.getName().equals(LOCAL_CLIENT_NAME);
        assert clientFromServerResponse.getUserName().equals(LOCAL_CLIENT_USERNAME);
        assert clientFromServerResponse.getBio().equals(LOCAL_CLIENT_BIO);
        assert clientFromServerResponse.getPassword().equals(LOCAL_CLIENT_PASSWORD);
        assert clientFromServerResponse.getTokenString() != null;

        // CLIENT APPOINTMENT INFO
        final long LOCAL_CLIENT_APPOINTMENT_START = LOCAL_PROFESSIONAL_AVAILABILITY_START;
        final long LOCAL_CLIENT_APPOINTMENT_END = LOCAL_PROFESSIONAL_AVAILABILITY_END;

        // CREATE APPOINTMENT
        Appointment appointment = new Appointment();
        appointment.setStartTime(LOCAL_CLIENT_APPOINTMENT_START);
        appointment.setEndTime(LOCAL_CLIENT_APPOINTMENT_END);

        // CREATE CLIENT AVAILABILITY
        Availability clientAvailability = new Availability();
        clientAvailability.setAppointment(appointment);
        clientAvailability.setStartTime(appointment.getStartTime());
        clientAvailability.setEndTime(appointment.getEndTime());

        // REGISTER APPOINTMENT
        clientFromServerResponse.getAgenda().getAvailabilities().add(clientAvailability);
        User professionalForRequest = new User();
        professionalForRequest.setCpf(LOCAL_PROFESSIONAL_CPF);
        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(clientFromServerResponse);
        appointmentWrapper.setProfessional(professionalForRequest);
        boolean booleanFromResponseEntity = getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));
        assert booleanFromResponseEntity;

        User clientByCpf = mRepository.getUserByCPF(LOCAL_CLIENT_CPF);
        User profByCpf = mRepository.getUserByCPF(LOCAL_PROFESSIONAL_CPF);
        assert profByCpf != null;

        List<Availability> clientAvailabilities = clientByCpf.getAgenda().getAvailabilities();
        assert clientAvailabilities.size() == 1;
        Availability availability3 = clientAvailabilities.get(0);
        if (availability3.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                availability3.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
            assert availability3.getState() == Availability.ON_HOLD;
            Appointment clientAppointment = availability3.getAppointment();
            assert clientAppointment != null;
            assert clientAppointment.getSubscriberId() == profByCpf.getId();
            assert availability3.getStartTime() == clientAppointment.getStartTime();
            assert availability3.getEndTime() == clientAppointment.getEndTime();
        } else {
            assert false;
        }

        List<Availability> professionalAv = profByCpf.getAgenda().getAvailabilities();
        assert professionalAv.size() == 1;
        Availability av = professionalAv.get(0);
        if (av.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                av.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
            assert av.getState() == Availability.ON_HOLD;
            Appointment profAppointment = av.getAppointment();
            assert profAppointment != null;
            assert profAppointment.getSubscriberId() == clientByCpf.getId();
            assert av.getStartTime() == profAppointment.getStartTime();
            assert av.getEndTime() == profAppointment.getEndTime();
        } else {
            assert false;
        }

        List<Notification> professionalNotifications = profByCpf.getNotifications();
        assert professionalNotifications != null;
        assert professionalNotifications.size() == 1;

        Notification notification = professionalNotifications.get(0);
        assert notification.getClientId() == clientByCpf.getId();
        assert notification.getTimestamp() != 0;
        assert !notification.getMessage().isEmpty();

        List<Notification> userNotifications = clientByCpf.getNotifications();
        assert userNotifications != null;
        assert userNotifications.isEmpty();

        User byCpf1 = mRepository.getUserByCPF(professionalFromServerResponse.getCpf());
        byCpf1.setToken(professionalFromServerResponse.getTokenString());
        Integer unregisterResponseCode = getIntegerFromResponseEntity(userController.unregisterAvailability(byCpf1));
        assert unregisterResponseCode == 0;
        User prof = mRepository.getUserByCPF(professionalFromServerResponse.getCpf());
        User client = mRepository.getUserByCPF(clientByCpf.getCpf());

        // CHECK UNREGISTER INFO
        assert prof != null;
        assert client != null;
        assert prof.getAgenda().getAvailabilities().isEmpty();
        assert client.getAgenda().getAvailabilities().isEmpty();
        assert client.getNotifications().size() == 1;
        assert prof.getNotifications().size() == 1;

        mRepository.removeUser(prof);
        mRepository.removeUser(client);
    }

    @Test
    public void registerSimpleAppointmentTest() {
        // PROFESSIONAL INFO
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        // Register professional
        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        // CLIENT INFO
        final String LOCAL_CLIENT_CPF = "94950949434943";
        final User.UserType LOCAL_CLIENT_TYPE = User.UserType.USER;
        final String LOCAL_CLIENT_NAME = "client name";
        final String LOCAL_CLIENT_USERNAME = "client user name";
        final String LOCAL_CLIENT_BIO = "client user bio";
        final String LOCAL_CLIENT_PASSWORD = "client user password";

        // Register client
        User localClient = new User();
        localClient.setCpf(LOCAL_CLIENT_CPF);
        localClient.setUserType(LOCAL_CLIENT_TYPE);
        localClient.setName(LOCAL_CLIENT_NAME);
        localClient.setUserName(LOCAL_CLIENT_USERNAME);
        localClient.setBio(LOCAL_CLIENT_BIO);
        localClient.setPassword(LOCAL_CLIENT_PASSWORD);

        User clientFromServerResponse = getUserFromResponseEntity(userController.registerUser(localClient));
        assert clientFromServerResponse != null;
        assert clientFromServerResponse.getCpf().equals(LOCAL_CLIENT_CPF);
        assert clientFromServerResponse.getUserType().equals(LOCAL_CLIENT_TYPE);
        assert clientFromServerResponse.getName().equals(LOCAL_CLIENT_NAME);
        assert clientFromServerResponse.getUserName().equals(LOCAL_CLIENT_USERNAME);
        assert clientFromServerResponse.getBio().equals(LOCAL_CLIENT_BIO);
        assert clientFromServerResponse.getPassword().equals(LOCAL_CLIENT_PASSWORD);
        assert clientFromServerResponse.getTokenString() != null;

        // AVAILABILITY INFO
        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1703664000000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1703707200000L;
        final int LOCAL_PROFESSIONAL_AVAILABILITY_STATE = Availability.AVAILABLE;

        // Register availability
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(LOCAL_PROFESSIONAL_AVAILABILITY_STATE);
        professionalAvailability.setStartTime(LOCAL_PROFESSIONAL_AVAILABILITY_START);
        professionalAvailability.setEndTime(LOCAL_PROFESSIONAL_AVAILABILITY_END);
        professionalFromServerResponse.getAgenda().getAvailabilities().add(professionalAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professionalFromServerResponse));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;
        User professionalFromDb = mRepository.getUserByCPF(professionalFromServerResponse.getCpf());
        assert professionalFromDb != null;
        assert professionalFromDb.getAgenda().getAvailabilities().size() == 1;
        Availability availability1 = professionalFromDb.getAgenda().getAvailabilities().get(0);
        assert availability1.getAppointment() == null;
        assert availability1.getState() == LOCAL_PROFESSIONAL_AVAILABILITY_STATE;
        assert availability1.getStartTime() == LOCAL_PROFESSIONAL_AVAILABILITY_START;
        assert availability1.getEndTime() == LOCAL_PROFESSIONAL_AVAILABILITY_END;

        // Appointment info
        final long LOCAL_CLIENT_APPOINTMENT_START = LOCAL_PROFESSIONAL_AVAILABILITY_START;
        final long LOCAL_CLIENT_APPOINTMENT_END = LOCAL_PROFESSIONAL_AVAILABILITY_END;

        Appointment appointment = new Appointment();
        appointment.setStartTime(LOCAL_CLIENT_APPOINTMENT_START);
        appointment.setEndTime(LOCAL_CLIENT_APPOINTMENT_END);

        // Register appointment
        Availability clientAvailability = new Availability();
        clientAvailability.setAppointment(appointment);
        clientFromServerResponse.getAgenda().getAvailabilities().add(clientAvailability);
        User professionalForRequest = new User();
        professionalForRequest.setCpf(LOCAL_PROFESSIONAL_CPF);
        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(clientFromServerResponse);
        appointmentWrapper.setProfessional(professionalForRequest);
        boolean booleanFromResponseEntity = getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));
        assert booleanFromResponseEntity;

        User clientByCpf = mRepository.getUserByCPF(LOCAL_CLIENT_CPF);
        User profByCpf = mRepository.getUserByCPF(LOCAL_PROFESSIONAL_CPF);
        assert clientByCpf != null;
        assert profByCpf != null;

        List<Availability> clientAvailabilities = clientByCpf.getAgenda().getAvailabilities();
        assert clientAvailabilities.size() == 1;
        Availability availability = clientAvailabilities.get(0);
        if (availability.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                availability.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
            assert availability.getState() == Availability.ON_HOLD;
            Appointment clientAppointment = availability.getAppointment();
            assert clientAppointment != null;
            assert clientAppointment.getSubscriberId() == profByCpf.getId();
            assert availability.getStartTime() == clientAppointment.getStartTime();
            assert availability.getEndTime() == clientAppointment.getEndTime();
        } else {
            assert false;
        }

        List<Availability> professionalAv = profByCpf.getAgenda().getAvailabilities();
        assert professionalAv.size() == 1;
        Availability av = professionalAv.get(0);
        if (av.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                av.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
            assert av.getState() == Availability.ON_HOLD;
            Appointment profAppointment = av.getAppointment();
            assert profAppointment != null;
            assert profAppointment.getSubscriberId() == clientByCpf.getId();
            assert av.getStartTime() == profAppointment.getStartTime();
            assert av.getEndTime() == profAppointment.getEndTime();
        } else {
            assert false;
        }

        List<Notification> professionalNotifications = profByCpf.getNotifications();
        assert professionalNotifications != null;
        assert professionalNotifications.size() == 1;

        Notification notification = professionalNotifications.get(0);
        assert notification.getClientId() == clientByCpf.getId();
        assert notification.getTimestamp() != 0;
        assert !notification.getMessage().isEmpty();

        List<Notification> userNotifications = clientByCpf.getNotifications();
        assert userNotifications != null;
        assert userNotifications.isEmpty();

        mRepository.removeUser(profByCpf);
        mRepository.removeUser(clientByCpf);
    }

    @Test
    public void registerDoubleAppointmentTest() {
        // PROFESSIONAL INFO
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        // Register professional
        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        // CLIENT INFO
        final String LOCAL_CLIENT_CPF = "94950949434943";
        final User.UserType LOCAL_CLIENT_TYPE = User.UserType.USER;
        final String LOCAL_CLIENT_NAME = "client name";
        final String LOCAL_CLIENT_USERNAME = "client user name";
        final String LOCAL_CLIENT_BIO = "client user bio";
        final String LOCAL_CLIENT_PASSWORD = "client user password";

        // Register client
        User localClient = new User();
        localClient.setCpf(LOCAL_CLIENT_CPF);
        localClient.setUserType(LOCAL_CLIENT_TYPE);
        localClient.setName(LOCAL_CLIENT_NAME);
        localClient.setUserName(LOCAL_CLIENT_USERNAME);
        localClient.setBio(LOCAL_CLIENT_BIO);
        localClient.setPassword(LOCAL_CLIENT_PASSWORD);

        User clientFromServerResponse = getUserFromResponseEntity(userController.registerUser(localClient));
        assert clientFromServerResponse != null;
        assert clientFromServerResponse.getCpf().equals(LOCAL_CLIENT_CPF);
        assert clientFromServerResponse.getUserType().equals(LOCAL_CLIENT_TYPE);
        assert clientFromServerResponse.getName().equals(LOCAL_CLIENT_NAME);
        assert clientFromServerResponse.getUserName().equals(LOCAL_CLIENT_USERNAME);
        assert clientFromServerResponse.getBio().equals(LOCAL_CLIENT_BIO);
        assert clientFromServerResponse.getPassword().equals(LOCAL_CLIENT_PASSWORD);
        assert clientFromServerResponse.getTokenString() != null;

        // AVAILABILITY INFO
        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1702886400000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1702915200000L;
        final int LOCAL_PROFESSIONAL_AVAILABILITY_STATE = Availability.AVAILABLE;

        // Register availability
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(LOCAL_PROFESSIONAL_AVAILABILITY_STATE);
        professionalAvailability.setStartTime(LOCAL_PROFESSIONAL_AVAILABILITY_START);
        professionalAvailability.setEndTime(LOCAL_PROFESSIONAL_AVAILABILITY_END);
        professionalFromServerResponse.getAgenda().getAvailabilities().add(professionalAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professionalFromServerResponse));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;

        // APPOINTMENT INFO
        final long LOCAL_CLIENT_APPOINTMENT_START = LOCAL_PROFESSIONAL_AVAILABILITY_START;
        final long LOCAL_CLIENT_APPOINTMENT_END = 1702890000000L;

        Appointment appointment = new Appointment();
        appointment.setStartTime(LOCAL_CLIENT_APPOINTMENT_START);
        appointment.setEndTime(LOCAL_CLIENT_APPOINTMENT_END);

        Availability clientAvailability = new Availability();
        clientAvailability.setAppointment(appointment);

        clientFromServerResponse.getAgenda().getAvailabilities().add(clientAvailability);

        User professionalForRequest = new User();
        professionalForRequest.setCpf(LOCAL_PROFESSIONAL_CPF);

        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(clientFromServerResponse);
        appointmentWrapper.setProfessional(professionalForRequest);

        boolean booleanFromResponseEntity = getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));
        assert booleanFromResponseEntity;

        User clientByCpf = mRepository.getUserByCPF(LOCAL_CLIENT_CPF);
        User profByCpf = mRepository.getUserByCPF(LOCAL_PROFESSIONAL_CPF);

        assert clientByCpf != null;
        assert profByCpf != null;

        List<Availability> clientAvailabilities = clientByCpf.getAgenda().getAvailabilities();
        assert clientAvailabilities.size() == 1;
        Availability availability = clientAvailabilities.get(0);
        if (availability.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                availability.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
            assert availability.getState() == Availability.ON_HOLD;
            Appointment clientAppointment = availability.getAppointment();
            assert clientAppointment != null;
            assert clientAppointment.getSubscriberId() == profByCpf.getId();
            assert availability.getStartTime() == clientAppointment.getStartTime();
            assert availability.getEndTime() == clientAppointment.getEndTime();
        } else {
            assert false;
        }

        List<Availability> professionalAv = profByCpf.getAgenda().getAvailabilities();
        assert professionalAv.size() == 2;
        for (Availability av : professionalAv) {
            if (av.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                    av.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
                assert av.getState() == Availability.ON_HOLD;
                Appointment profAppointment = av.getAppointment();
                assert profAppointment != null;
                assert profAppointment.getSubscriberId() == clientByCpf.getId();
                assert av.getStartTime() == profAppointment.getStartTime();
                assert av.getEndTime() == profAppointment.getEndTime();
            } else {
                final long THIRTY_MINUTES = 1000 * 60 * 30;
                assert av.getState() == Availability.AVAILABLE;
                assert (av.getEndTime() - av.getStartTime()) >= THIRTY_MINUTES;
            }
        }

        List<Notification> professionalNotifications = profByCpf.getNotifications();
        assert professionalNotifications != null;
        assert professionalNotifications.size() == 1;

        Notification notification = professionalNotifications.get(0);
        assert notification.getClientId() == clientByCpf.getId();
        assert notification.getTimestamp() != 0;
        assert !notification.getMessage().isEmpty();

        List<Notification> userNotifications = clientByCpf.getNotifications();
        assert userNotifications != null;
        assert userNotifications.isEmpty();

        mRepository.removeUser(profByCpf);
        mRepository.removeUser(clientByCpf);
    }

    @Test
    public void registerTripleAppointmentTest() {
        // PROFESSIONAL INFO
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        // Register professional
        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        // CLIENT INFO
        final String LOCAL_CLIENT_CPF = "94950949434943";
        final User.UserType LOCAL_CLIENT_TYPE = User.UserType.USER;
        final String LOCAL_CLIENT_NAME = "client name";
        final String LOCAL_CLIENT_USERNAME = "client user name";
        final String LOCAL_CLIENT_BIO = "client user bio";
        final String LOCAL_CLIENT_PASSWORD = "client user password";

        // Register client
        User localClient = new User();
        localClient.setCpf(LOCAL_CLIENT_CPF);
        localClient.setUserType(LOCAL_CLIENT_TYPE);
        localClient.setName(LOCAL_CLIENT_NAME);
        localClient.setUserName(LOCAL_CLIENT_USERNAME);
        localClient.setBio(LOCAL_CLIENT_BIO);
        localClient.setPassword(LOCAL_CLIENT_PASSWORD);

        User clientFromServerResponse = getUserFromResponseEntity(userController.registerUser(localClient));
        assert clientFromServerResponse != null;
        assert clientFromServerResponse.getCpf().equals(LOCAL_CLIENT_CPF);
        assert clientFromServerResponse.getUserType().equals(LOCAL_CLIENT_TYPE);
        assert clientFromServerResponse.getName().equals(LOCAL_CLIENT_NAME);
        assert clientFromServerResponse.getUserName().equals(LOCAL_CLIENT_USERNAME);
        assert clientFromServerResponse.getBio().equals(LOCAL_CLIENT_BIO);
        assert clientFromServerResponse.getPassword().equals(LOCAL_CLIENT_PASSWORD);
        assert clientFromServerResponse.getTokenString() != null;

        // AVAILABILITY INFO
        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1702886400000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1702915200000L;
        final int LOCAL_PROFESSIONAL_AVAILABILITY_STATE = Availability.AVAILABLE;

        // Register availability
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(LOCAL_PROFESSIONAL_AVAILABILITY_STATE);
        professionalAvailability.setStartTime(LOCAL_PROFESSIONAL_AVAILABILITY_START);
        professionalAvailability.setEndTime(LOCAL_PROFESSIONAL_AVAILABILITY_END);
        professionalFromServerResponse.getAgenda().getAvailabilities().add(professionalAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professionalFromServerResponse));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;

        // APPOINTMENT INFO
        final long LOCAL_CLIENT_APPOINTMENT_START = 1702893600000L;
        final long LOCAL_CLIENT_APPOINTMENT_END = 1702897200000L;

        Appointment appointment = new Appointment();
        appointment.setStartTime(LOCAL_CLIENT_APPOINTMENT_START);
        appointment.setEndTime(LOCAL_CLIENT_APPOINTMENT_END);

        Availability clientAvailability = new Availability();
        clientAvailability.setAppointment(appointment);

        clientFromServerResponse.getAgenda().getAvailabilities().add(clientAvailability);

        User professionalForRequest = new User();
        professionalForRequest.setCpf(LOCAL_PROFESSIONAL_CPF);

        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(clientFromServerResponse);
        appointmentWrapper.setProfessional(professionalForRequest);

        boolean booleanFromResponseEntity = getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));
        assert booleanFromResponseEntity;

        User clientByCpf = mRepository.getUserByCPF(LOCAL_CLIENT_CPF);
        User profByCpf = mRepository.getUserByCPF(LOCAL_PROFESSIONAL_CPF);

        assert clientByCpf != null;
        assert profByCpf != null;

        List<Availability> clientAvailabilities = clientByCpf.getAgenda().getAvailabilities();
        assert clientAvailabilities.size() == 1;
        for (Availability availability : clientAvailabilities) {
            if (availability.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                    availability.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
                assert availability.getState() == Availability.ON_HOLD;
                Appointment clientAppointment = availability.getAppointment();
                assert clientAppointment != null;
                assert clientAppointment.getSubscriberId() == profByCpf.getId();
                assert availability.getStartTime() == clientAppointment.getStartTime();
                assert availability.getEndTime() == clientAppointment.getEndTime();
            } else {
                assert false;
            }
        }

        List<Availability> professionalAv = profByCpf.getAgenda().getAvailabilities();
        assert professionalAv.size() == 3;
        for (Availability av : professionalAv) {
            if (av.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                    av.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
                assert av.getState() == Availability.ON_HOLD;
                Appointment profAppointment = av.getAppointment();
                assert profAppointment != null;
                assert profAppointment.getSubscriberId() == clientByCpf.getId();
                assert av.getStartTime() == profAppointment.getStartTime();
                assert av.getEndTime() == profAppointment.getEndTime();
            } else {
                final long THIRTY_MINUTES = 1000 * 60 * 30;
                assert av.getState() == Availability.AVAILABLE;
                assert (av.getEndTime() - av.getStartTime()) >= THIRTY_MINUTES;
            }
        }

        List<Notification> professionalNotifications = profByCpf.getNotifications();
        assert professionalNotifications != null;
        assert professionalNotifications.size() == 1;

        Notification notification = professionalNotifications.get(0);
        assert notification.getClientId() == clientByCpf.getId();
        assert notification.getTimestamp() != 0;
        assert !notification.getMessage().isEmpty();

        List<Notification> userNotifications = clientByCpf.getNotifications();
        assert userNotifications != null;
        assert userNotifications.isEmpty();

        mRepository.removeUser(profByCpf);
        mRepository.removeUser(clientByCpf);
    }

    @Test
    public void registerAppointmentWithBottomSlotMinorThan30Minutes() {
        // PROFESSIONAL INFO
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        // Register professional
        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        // CLIENT INFO
        final String LOCAL_CLIENT_CPF = "94950949434943";
        final User.UserType LOCAL_CLIENT_TYPE = User.UserType.USER;
        final String LOCAL_CLIENT_NAME = "client name";
        final String LOCAL_CLIENT_USERNAME = "client user name";
        final String LOCAL_CLIENT_BIO = "client user bio";
        final String LOCAL_CLIENT_PASSWORD = "client user password";

        // Register client
        User localClient = new User();
        localClient.setCpf(LOCAL_CLIENT_CPF);
        localClient.setUserType(LOCAL_CLIENT_TYPE);
        localClient.setName(LOCAL_CLIENT_NAME);
        localClient.setUserName(LOCAL_CLIENT_USERNAME);
        localClient.setBio(LOCAL_CLIENT_BIO);
        localClient.setPassword(LOCAL_CLIENT_PASSWORD);

        User clientFromServerResponse = getUserFromResponseEntity(userController.registerUser(localClient));
        assert clientFromServerResponse != null;
        assert clientFromServerResponse.getCpf().equals(LOCAL_CLIENT_CPF);
        assert clientFromServerResponse.getUserType().equals(LOCAL_CLIENT_TYPE);
        assert clientFromServerResponse.getName().equals(LOCAL_CLIENT_NAME);
        assert clientFromServerResponse.getUserName().equals(LOCAL_CLIENT_USERNAME);
        assert clientFromServerResponse.getBio().equals(LOCAL_CLIENT_BIO);
        assert clientFromServerResponse.getPassword().equals(LOCAL_CLIENT_PASSWORD);
        assert clientFromServerResponse.getTokenString() != null;

        // AVAILABILITY INFO
        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1702886400000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1702915200000L;
        final int LOCAL_PROFESSIONAL_AVAILABILITY_STATE = Availability.AVAILABLE;

        // Register availability
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(LOCAL_PROFESSIONAL_AVAILABILITY_STATE);
        professionalAvailability.setStartTime(LOCAL_PROFESSIONAL_AVAILABILITY_START);
        professionalAvailability.setEndTime(LOCAL_PROFESSIONAL_AVAILABILITY_END);
        professionalFromServerResponse.getAgenda().getAvailabilities().add(professionalAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professionalFromServerResponse));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;

        // APPOINTMENT INFO
        final long LOCAL_CLIENT_APPOINTMENT_START = 1702911300000L;
        final long LOCAL_CLIENT_APPOINTMENT_END = 1702914900000L;

        Appointment appointment = new Appointment();
        appointment.setStartTime(LOCAL_CLIENT_APPOINTMENT_START);
        appointment.setEndTime(LOCAL_CLIENT_APPOINTMENT_END);

        Availability clientAvailability = new Availability();
        clientAvailability.setAppointment(appointment);

        clientFromServerResponse.getAgenda().getAvailabilities().add(clientAvailability);

        User professionalForRequest = new User();
        professionalForRequest.setCpf(LOCAL_PROFESSIONAL_CPF);

        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(clientFromServerResponse);
        appointmentWrapper.setProfessional(professionalForRequest);

        boolean booleanFromResponseEntity = getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));
        assert booleanFromResponseEntity;

        User clientByCpf = mRepository.getUserByCPF(LOCAL_CLIENT_CPF);
        User profByCpf = mRepository.getUserByCPF(LOCAL_PROFESSIONAL_CPF);

        assert clientByCpf != null;
        assert profByCpf != null;

        List<Availability> clientAvailabilities = clientByCpf.getAgenda().getAvailabilities();
        assert clientAvailabilities.size() == 1;
        for (Availability availability : clientAvailabilities) {
            if (availability.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                    availability.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
                assert availability.getState() == Availability.ON_HOLD;
                Appointment clientAppointment = availability.getAppointment();
                assert clientAppointment != null;
                assert clientAppointment.getSubscriberId() == profByCpf.getId();
                assert availability.getStartTime() == clientAppointment.getStartTime();
                assert availability.getEndTime() == clientAppointment.getEndTime();
            } else {
                assert false;
            }
        }

        List<Availability> professionalAv = profByCpf.getAgenda().getAvailabilities();
        assert professionalAv.size() == 2;
        for (Availability av : professionalAv) {
            if (av.getStartTime() == LOCAL_CLIENT_APPOINTMENT_START &&
                    av.getEndTime() == LOCAL_CLIENT_APPOINTMENT_END) {
                assert av.getState() == Availability.ON_HOLD;
                Appointment profAppointment = av.getAppointment();
                assert profAppointment != null;
                assert profAppointment.getSubscriberId() == clientByCpf.getId();
                assert av.getStartTime() == profAppointment.getStartTime();
                assert av.getEndTime() == profAppointment.getEndTime();
            } else {
                final long THIRTY_MINUTES = 1000 * 60 * 30;
                assert av.getState() == Availability.AVAILABLE;
                assert (av.getEndTime() - av.getStartTime()) >= THIRTY_MINUTES;
            }
        }

        List<Notification> professionalNotifications = profByCpf.getNotifications();
        assert professionalNotifications != null;
        assert professionalNotifications.size() == 1;

        Notification notification = professionalNotifications.get(0);
        assert notification.getClientId() == clientByCpf.getId();
        assert notification.getTimestamp() != 0;
        assert !notification.getMessage().isEmpty();

        List<Notification> userNotifications = clientByCpf.getNotifications();
        assert userNotifications != null;
        assert userNotifications.isEmpty();

        mRepository.removeUser(profByCpf);
        mRepository.removeUser(clientByCpf);
    }

    @Test
    public void registerAppointmentFailTest() {
        // PROFESSIONAL INFO
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        // Register professional
        User localProfessional = new User();
        localProfessional.setCpf(LOCAL_PROFESSIONAL_CPF);
        localProfessional.setUserType(LOCAL_PROFESSIONAL_TYPE);
        localProfessional.setName(LOCAL_PROFESSIONAL_NAME);
        localProfessional.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        localProfessional.setBio(LOCAL_PROFESSIONAL_BIO);
        localProfessional.setPassword(LOCAL_PROFESSIONAL_PASSWORD);

        User professionalFromServerResponse = getUserFromResponseEntity(userController.registerUser(localProfessional));
        assert professionalFromServerResponse != null;
        assert professionalFromServerResponse.getCpf().equals(LOCAL_PROFESSIONAL_CPF);
        assert professionalFromServerResponse.getUserType().equals(LOCAL_PROFESSIONAL_TYPE);
        assert professionalFromServerResponse.getName().equals(LOCAL_PROFESSIONAL_NAME);
        assert professionalFromServerResponse.getUserName().equals(LOCAL_PROFESSIONAL_USERNAME);
        assert professionalFromServerResponse.getBio().equals(LOCAL_PROFESSIONAL_BIO);
        assert professionalFromServerResponse.getPassword().equals(LOCAL_PROFESSIONAL_PASSWORD);
        assert professionalFromServerResponse.getTokenString() != null;

        // CLIENT INFO
        final String LOCAL_CLIENT_CPF = "94950949434943";
        final User.UserType LOCAL_CLIENT_TYPE = User.UserType.USER;
        final String LOCAL_CLIENT_NAME = "client name";
        final String LOCAL_CLIENT_USERNAME = "client user name";
        final String LOCAL_CLIENT_BIO = "client user bio";
        final String LOCAL_CLIENT_PASSWORD = "client user password";

        // Register client
        User localClient = new User();
        localClient.setCpf(LOCAL_CLIENT_CPF);
        localClient.setUserType(LOCAL_CLIENT_TYPE);
        localClient.setName(LOCAL_CLIENT_NAME);
        localClient.setUserName(LOCAL_CLIENT_USERNAME);
        localClient.setBio(LOCAL_CLIENT_BIO);
        localClient.setPassword(LOCAL_CLIENT_PASSWORD);

        User clientFromServerResponse = getUserFromResponseEntity(userController.registerUser(localClient));
        assert clientFromServerResponse != null;
        assert clientFromServerResponse.getCpf().equals(LOCAL_CLIENT_CPF);
        assert clientFromServerResponse.getUserType().equals(LOCAL_CLIENT_TYPE);
        assert clientFromServerResponse.getName().equals(LOCAL_CLIENT_NAME);
        assert clientFromServerResponse.getUserName().equals(LOCAL_CLIENT_USERNAME);
        assert clientFromServerResponse.getBio().equals(LOCAL_CLIENT_BIO);
        assert clientFromServerResponse.getPassword().equals(LOCAL_CLIENT_PASSWORD);
        assert clientFromServerResponse.getTokenString() != null;

        // AVAILABILITY INFO
        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1702886400000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1702890000000L;
        final int LOCAL_PROFESSIONAL_AVAILABILITY_STATE = Availability.AVAILABLE;

        // Register availability
        Availability professionalAvailability = new Availability();
        professionalAvailability.setState(LOCAL_PROFESSIONAL_AVAILABILITY_STATE);
        professionalAvailability.setStartTime(LOCAL_PROFESSIONAL_AVAILABILITY_START);
        professionalAvailability.setEndTime(LOCAL_PROFESSIONAL_AVAILABILITY_END);
        professionalFromServerResponse.getAgenda().getAvailabilities().add(professionalAvailability);
        Integer responseCode = getIntegerFromResponseEntity(userController.registerAvailability(professionalFromServerResponse));
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;

        // APPOINTMENT INFO
        final long LOCAL_CLIENT_APPOINTMENT_START = LOCAL_PROFESSIONAL_AVAILABILITY_START;
        final long LOCAL_CLIENT_APPOINTMENT_END = 1702897200000L;

        Appointment appointment = new Appointment();
        appointment.setStartTime(LOCAL_CLIENT_APPOINTMENT_START);
        appointment.setEndTime(LOCAL_CLIENT_APPOINTMENT_END);

        Availability clientAvailability = new Availability();
        clientAvailability.setAppointment(appointment);

        clientFromServerResponse.getAgenda().getAvailabilities().add(clientAvailability);

        User professionalForRequest = new User();
        professionalForRequest.setCpf(LOCAL_PROFESSIONAL_CPF);

        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(clientFromServerResponse);
        appointmentWrapper.setProfessional(professionalForRequest);

        boolean booleanFromResponseEntity = getBooleanFromResponseEntity(userController.registerAppointment(appointmentWrapper));
        assert !booleanFromResponseEntity;

        User clientByCpf = mRepository.getUserByCPF(LOCAL_CLIENT_CPF);
        User profByCpf = mRepository.getUserByCPF(LOCAL_PROFESSIONAL_CPF);

        assert clientByCpf != null;
        assert profByCpf != null;

        List<Availability> clientAvailabilities = clientByCpf.getAgenda().getAvailabilities();
        assert clientAvailabilities.isEmpty();

        List<Availability> professionalAv = profByCpf.getAgenda().getAvailabilities();
        assert professionalAv.size() == 1;
        for (Availability av : professionalAv) {
            if (av.getStartTime() == LOCAL_PROFESSIONAL_AVAILABILITY_START &&
                    av.getEndTime() == LOCAL_PROFESSIONAL_AVAILABILITY_END) {
                assert av.getState() == Availability.AVAILABLE;
                Appointment profAppointment = av.getAppointment();
                assert profAppointment == null;
            } else {
                assert false;
            }
        }

        List<Notification> professionalNotifications = profByCpf.getNotifications();
        assert professionalNotifications != null;
        assert professionalNotifications.isEmpty();

        List<Notification> userNotifications = clientByCpf.getNotifications();
        assert userNotifications != null;
        assert userNotifications.isEmpty();

        mRepository.removeUser(profByCpf);
        mRepository.removeUser(clientByCpf);
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

    private Availability getMockAvailability(long startTime, long endTime, int state) {
        Availability av = new Availability();
        av.setState(state);
        av.setStartTime(startTime);
        av.setEndTime(endTime);
        return av;
    }

    private void assertAvailabilityInfo(Availability mockAvailability, Availability restoredAvailability) {
        assert mockAvailability != null;
        assert restoredAvailability != null;
        assert restoredAvailability.getAppointment() == null;
        assert mockAvailability.getAppointment() == null;
        assert restoredAvailability.getState() == mockAvailability.getState();
        assert restoredAvailability.getStartTime() == mockAvailability.getStartTime();
        assert restoredAvailability.getEndTime() == mockAvailability.getEndTime();
    }

    private void assertAppointmentInfo(Appointment a, Appointment b) {
        ServSimplesApplication.logi("WILL", "b.getStartTime():" + b.getStartTime());
        ServSimplesApplication.logi("WILL", "b.getEndTime():" + b.getEndTime());
        ServSimplesApplication.logi("WILL", "b.getServiceId():" + b.getServiceId());
        ServSimplesApplication.logi("WILL", "a.getServiceId():" + a.getServiceId());
        assert a.getEndTime() == b.getEndTime();
        assert a.getStartTime() == b.getStartTime();
        assert a.getServiceId() == b.getServiceId();
    }

    private void assertUserRegistrationInfo(User mockUser, User restoredUser) {
        UserManager mockUserManager = UserManager.create(mockUser);
        UserManager restoredUserManager = UserManager.create(restoredUser);
        assert !mockUserManager.isNull();
        assert !restoredUserManager.isNull();

        assert restoredUserManager.cpf().equals(mockUserManager.cpf());
        assert restoredUserManager.type().equals(mockUserManager.type());
        assert restoredUserManager.name().equals(mockUserManager.name());
        assert restoredUserManager.username().equals(mockUserManager.username());
        assert restoredUserManager.bio().equals(mockUserManager.bio());
        assert restoredUserManager.password().equals(mockUserManager.password());
        assert restoredUserManager.tokenString() != null;
    }

    private User getClientUserMock() {
        final String LOCAL_CLIENT_CPF = "94950949434943";
        final User.UserType LOCAL_CLIENT_TYPE = User.UserType.USER;
        final String LOCAL_CLIENT_NAME = "client name";
        final String LOCAL_CLIENT_USERNAME = "client user name";
        final String LOCAL_CLIENT_BIO = "client user bio";
        final String LOCAL_CLIENT_PASSWORD = "client user password";

        User user = new User();
        user.setCpf(LOCAL_CLIENT_CPF);
        user.setUserType(LOCAL_CLIENT_TYPE);
        user.setName(LOCAL_CLIENT_NAME);
        user.setUserName(LOCAL_CLIENT_USERNAME);
        user.setBio(LOCAL_CLIENT_BIO);
        user.setPassword(LOCAL_CLIENT_PASSWORD);
        return user;
    }

    private User getProfessionalUserMock() {
        final String LOCAL_PROFESSIONAL_CPF = "0999288938398";
        final User.UserType LOCAL_PROFESSIONAL_TYPE = User.UserType.PROFESSIONAL;
        final String LOCAL_PROFESSIONAL_NAME = "professional name";
        final String LOCAL_PROFESSIONAL_USERNAME = "professional user name";
        final String LOCAL_PROFESSIONAL_BIO = "professional user bio";
        final String LOCAL_PROFESSIONAL_PASSWORD = "professional user password";

        User user = new User();
        user.setCpf(LOCAL_PROFESSIONAL_CPF);
        user.setUserType(LOCAL_PROFESSIONAL_TYPE);
        user.setName(LOCAL_PROFESSIONAL_NAME);
        user.setUserName(LOCAL_PROFESSIONAL_USERNAME);
        user.setBio(LOCAL_PROFESSIONAL_BIO);
        user.setPassword(LOCAL_PROFESSIONAL_PASSWORD);
        return user;
    }
}