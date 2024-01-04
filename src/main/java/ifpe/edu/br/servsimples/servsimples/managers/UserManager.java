/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.autentication.Token;
import ifpe.edu.br.servsimples.servsimples.model.*;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class UserManager extends Manager {

    public static final int VALID_USER = 0;
    public static final int ERROR_NAME = 1;
    public static final int ERROR_USERNAME = 2;
    public static final int ERROR_PASSWORD = 3;
    public static final int ERROR_CPF = 4;
    public static final int USER_EXISTS = 5;
    public static final int USER_NULL = 6;
    public static final int USER_NOT_ALLOWED = 7;
    public static final int PASSWORD_MISMATCH = 8;
    public static final int USERNAME_MISMATCH = 9;
    public static final int USER_NOT_EXISTS = 10;
    public static final int MISSING_LOGIN_INFO = 11;
    public static final int LOGIN_INFO_LENGTH_ERROR = 12;
    public static final int USER_INFO_DUPLICATED = 13;
    public static final int DUPLICATED_INFO = 14;
    private static final String TAG = UserManager.class.getSimpleName();

    private final User user;

    private UserManager(User user) {
        this.user = user;
    }

    public static UserManager create(User user) {
        return new UserManager(user);
    }

    public int getRegisterValidationCode() {
        if (user == null) return USER_NULL;
        if (user.getUserName() == null || user.getUserName().isEmpty() || user.getUserName().isBlank() /*|| user.getUserName().length() != 64*/) {
            return ERROR_USERNAME;
        }
        if (user.getPassword() == null || user.getPassword().isEmpty() || user.getPassword().isBlank()/* || user.getPassword().length() != 64*/) {
            return ERROR_PASSWORD;
        }
        if (user.getCpf() == null || user.getCpf().isEmpty() || user.getCpf().isBlank()/* || user.getCpf().length() != 64*/) {
            return ERROR_CPF;
        }
        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            return ERROR_NAME;
        }
        if (user.getPassword().equals(user.getUserName()) || user.getCpf().equals(user.getUserName()) ||
                user.getCpf().equals(user.getPassword())) {
            return USER_INFO_DUPLICATED;
        }
        return VALID_USER;
    }

    public int loginValidationCode() {
        if (user == null) return USER_NULL;
        String userName = user.getUserName();
        String password = user.getPassword();
        if (userName == null || userName.isEmpty() || userName.isBlank() ||
                password == null || password.isEmpty() || password.isBlank()) {
            return MISSING_LOGIN_INFO;
        }
        if (userName.equals(password)) return DUPLICATED_INFO;
        return VALID_USER;
    }

    /**
     * @return the first availability in the list
     */
    public Availability availability() {
        if (isNull()) return null;
        return user.getAgenda().getAvailabilities().get(0);
    }

    public void availability(Availability availability) {
        if (isNull()) return;
        user.getAgenda().getAvailabilities().add(availability);
    }

    public User user() {
        return user;
    }

    public void token(Token token) {
        this.user.setToken(token.getEncryptedToken());
    }

    public String cpf() {
        return user == null ? null : user.getCpf();
    }

    public void cpf(String cpf) {
        if (user == null) return;
        user.setCpf(cpf);
    }

    public String name() {
        return user == null ? null : user.getName();
    }

    public void name(String name) {
        if (user == null) return;
        user.setName(name);
    }

    public User.UserType type() {
        return user == null ? null : user.getUserType();
    }

    public void type(User.UserType type) {
        if (user == null) return;
        user.setUserType(type);
    }

    public String username() {
        return user == null ? null : user.getUserName();
    }

    public void username(String username) {
        if (user == null) return;
        user.setUserName(username);
    }

    public boolean isNull() {
        return this.user == null;
    }

    public String tokenString() {
        return user == null ? null : user.getTokenString();
    }

    public String bio() {
        return user == null ? null : user.getBio();
    }

    public void bio(String bio) {
        if (user == null) return;
        user.setBio(bio);
    }

    public String password() {
        return user == null ? null : user.getPassword();
    }

    public void password(String password) {
        if (user == null) return;
        user.setPassword(password);
    }

    public void service(Service service) {
        if (user == null) return;
        user.getServices().add(service);
    }

    /**
     * @return the first Service in the list or null if empty or
     * if user is null
     */
    public Service service() {
        return user == null ? null : user.getServices().get(0);
    }

    public void updateService(Service newService) {
        if (user == null || newService == null) return;
        for (Service s : user.getServices()) {
            if (Objects.equals(s.getId(), newService.getId())) {
                s.setCost(newService.getCost());
                s.setName(newService.getName());
                s.setCategory(newService.getCategory());
                s.setDescription(newService.getDescription());
            }
        }
    }

    public void removeService(Service service) {
        if (user == null || service == null) return;
        user.getServices().removeIf(s -> Objects.equals(s.getId(), service.getId()));
    }

    public Agenda agenda() {
        if (isNull()) return null;
        return user.getAgenda();
    }

    /**
     * @return the first Appointment in the list or Null
     */
    @Nullable
    public Appointment appointment() {
        if (user == null) return null;
        Availability availability = user.getAgenda().getAvailabilities().get(0);
        return availability == null ? null : availability.getAppointment();
    }

    public List<Availability> availabilities() {
        if (isNull()) return null;
        return user.getAgenda().getAvailabilities();
    }

    public long id() {
        if (user == null) return -1;
        return user.getId();
    }

    public void notification(Notification notification) {
        if (isNull()) return;
        this.user.getNotifications().add(notification);
    }

    public Notification notification(){
        if (isNull()) return null;
        if (user.getNotifications().isEmpty()) return null;
        return user.getNotifications().get(0);
    }

    public void sortAvailabilities() {
        user.getAgenda().getAvailabilities().sort(Comparator.comparingLong(Availability::getStartTime));
        sortNotification(); // TODO FIX ME for the love of god
    }

    public void sortNotification() {
        user.getNotifications().sort(Comparator.comparingLong(Notification::getTimestamp).reversed());
    }

    public List<Appointment> appointments() {
        if (isNull()) return null;
        List<Availability> availabilities = user.getAgenda().getAvailabilities();
        List<Appointment> appointments = new ArrayList<>();
        for (Availability a : availabilities) {
            if (a.getAppointment() != null) {
                appointments.add(a.getAppointment());
            }
        }
        return appointments.isEmpty() ? null : appointments;
    }

    public boolean removeAppointment(Appointment appointment) {
        if (isNull()) return false;
        List<Availability> availabilities = user.getAgenda().getAvailabilities();
        for (Availability a : availabilities) {
            if (a.getState() != Availability.AVAILABLE) {
                if (a.getAppointment() == null) return false;
                if (isAppointmentsEqual(a.getAppointment(), appointment)) {
                    a.setAppointment(null);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAppointmentsEqual(Appointment a, Appointment b) {
        return a.getSubscriberId() == b.getSubscriberId() &&
                a.getStartTime() == b.getStartTime() &&
                a.getEndTime() == b.getEndTime();
    }

    public List<Service> services() {
        if (isNull()) return null;
        return user.getServices();
    }

    public List<Notification> notifications() {
        if (isNull()) return null;
        return user.getNotifications();
    }
}