/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples;

import com.fasterxml.jackson.databind.ObjectMapper;
import ifpe.edu.br.servsimples.servsimples.controller.MainController;
import ifpe.edu.br.servsimples.servsimples.managers.AvailabilityManager;
import ifpe.edu.br.servsimples.servsimples.model.Appointment;
import ifpe.edu.br.servsimples.servsimples.model.Availability;
import ifpe.edu.br.servsimples.servsimples.model.Notification;
import ifpe.edu.br.servsimples.servsimples.model.User;
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
    private final UserRepo userRepo;
    private final MainController userController;
    private final ObjectMapper objectMapper;

    @Autowired
    public UserControllerTest(UserRepo userRepo, ServiceRepo serviceRepo, ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.objectMapper = objectMapper;
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

        User restoredUser = userRepo.findByCpf(LOCAL_PROFESSIONAL_CPF);
        userRepo.delete(restoredUser);
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
        assert responseCode == AvailabilityManager.AVAILABILITY_VALID;

        User byCpf = userRepo.findByCpf(professionalFromServerResponse.getCpf());
        userRepo.delete(byCpf);
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
        final long LOCAL_PROFESSIONAL_AVAILABILITY_START = 1702796400000L;
        final long LOCAL_PROFESSIONAL_AVAILABILITY_END = 1702839600000L;
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
        final long LOCAL_CLIENT_APPOINTMENT_END = LOCAL_PROFESSIONAL_AVAILABILITY_END;

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

        User clientByCpf = userRepo.findByCpf(LOCAL_CLIENT_CPF);
        User profByCpf = userRepo.findByCpf(LOCAL_PROFESSIONAL_CPF);

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
        assert professionalAv.size() == 1;
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
                assert false;
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

        userRepo.delete(profByCpf);
        userRepo.delete(clientByCpf);
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

        User clientByCpf = userRepo.findByCpf(LOCAL_CLIENT_CPF);
        User profByCpf = userRepo.findByCpf(LOCAL_PROFESSIONAL_CPF);

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

        userRepo.delete(profByCpf);
        userRepo.delete(clientByCpf);
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

        User clientByCpf = userRepo.findByCpf(LOCAL_CLIENT_CPF);
        User profByCpf = userRepo.findByCpf(LOCAL_PROFESSIONAL_CPF);

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

        userRepo.delete(profByCpf);
        userRepo.delete(clientByCpf);
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

        User clientByCpf = userRepo.findByCpf(LOCAL_CLIENT_CPF);
        User profByCpf = userRepo.findByCpf(LOCAL_PROFESSIONAL_CPF);

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

        userRepo.delete(profByCpf);
        userRepo.delete(clientByCpf);
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

        User clientByCpf = userRepo.findByCpf(LOCAL_CLIENT_CPF);
        User profByCpf = userRepo.findByCpf(LOCAL_PROFESSIONAL_CPF);

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

        userRepo.delete(profByCpf);
        userRepo.delete(clientByCpf);
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
}