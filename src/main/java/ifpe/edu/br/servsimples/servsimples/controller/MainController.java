/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.controller;

import com.google.gson.Gson;
import ifpe.edu.br.servsimples.servsimples.InterfacesWrapper;
import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import ifpe.edu.br.servsimples.servsimples.autentication.Token;
import ifpe.edu.br.servsimples.servsimples.managers.AuthManager;
import ifpe.edu.br.servsimples.servsimples.managers.AvailabilityManager;
import ifpe.edu.br.servsimples.servsimples.managers.ServiceManager;
import ifpe.edu.br.servsimples.servsimples.managers.UserManager;
import ifpe.edu.br.servsimples.servsimples.model.Appointment;
import ifpe.edu.br.servsimples.servsimples.model.Availability;
import ifpe.edu.br.servsimples.servsimples.model.Service;
import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.Repository;
import ifpe.edu.br.servsimples.servsimples.repo.ServiceRepo;
import ifpe.edu.br.servsimples.servsimples.repo.UserRepo;
import ifpe.edu.br.servsimples.servsimples.utils.AppointmentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@CrossOrigin("*")
@RestController
public class MainController {

    private static final String TAG = MainController.class.getSimpleName();

    private final ServiceManager mServiceManager;
    private final AuthManager mAuthManager;
    private final AvailabilityManager mAvailabilityManager;
    private final Repository mRepository;

    @Autowired
    public MainController(UserRepo userRepo, ServiceRepo serviceRepo) {
        mRepository = Repository.create(userRepo, serviceRepo);
        mServiceManager = new ServiceManager(serviceRepo);
        mAuthManager = AuthManager.getInstance();
        mAvailabilityManager = new AvailabilityManager();
    }

    @GetMapping("api/test")
    public void lala() {
        Date date = new Date();
        long time = date.getTime();
        ServSimplesApplication.logi(TAG, "now: " + convertToHumanReadable(time));
    }

    @PostMapping("api/register/user/availability")
    public ResponseEntity<String> registerAvailability(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerAvailability: " + getUserInfoString(user) +
                "availability info:" + getAvailabilityInfoToString(user.getAgenda().getAvailabilities().get(0)));

        User restoredUser = mRepository.getUserByCPF(user.getCpf());

        if (restoredUser == null) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }

        if (restoredUser.getUserType() != User.UserType.PROFESSIONAL) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_ALLOWED,
                    getErrorMessageByCode(UserManager.USER_NOT_ALLOWED));
        }

        int tokenValidationCode = mAuthManager.getTokenValidationCode(restoredUser, user.getTokenString());
        return mAuthManager.handleTokenValidation(() -> {
            UserManager manager = UserManager.create(user);


            Availability newAvailability = user.getAgenda().getAvailabilities().get(0);
            int availabilityValidationCode =
                    mAvailabilityManager.getNewAvailabilityValidationCode(restoredUser.getAgenda(), newAvailability);
            if (availabilityValidationCode == AvailabilityManager.AVAILABILITY_VALID) {
                restoredUser.getAgenda().addAvailability(newAvailability);
                mRepository.updateUser(restoredUser);
            }
            return availabilityValidationCode;
        }, tokenValidationCode);
    }

    @PostMapping("api/register/user/appointment")
    public ResponseEntity<String> registerAppointment(@RequestBody AppointmentWrapper appointmentWrapper) {
        ServSimplesApplication.logi(TAG, "[registerAppointment] user info:" + getUserInfoString(appointmentWrapper.getClient())
                + " Appointment info:" + getAppointmentInfoString(appointmentWrapper.getClient().getAgenda().getAvailabilities().get(0).getAppointment()));

        User restoredClientUser = mRepository.getUserByCPF(appointmentWrapper.getClient().getCpf());
        User restoredProfessionalUser = mRepository.getUserByCPF(appointmentWrapper.getProfessional().getCpf());

        if (restoredClientUser == null || restoredProfessionalUser == null) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }

        int tokenValidationCode = mAuthManager.getTokenValidationCode(restoredClientUser,
                appointmentWrapper.getClient().getTokenString());

        return mAuthManager.handleTokenValidation(() -> {
            if (mAvailabilityManager.isAppointmentValid(appointmentWrapper)) {
                ServSimplesApplication.logi(TAG, "Appointment is valid");
                AppointmentWrapper restoredUsersWrapper = new AppointmentWrapper();
                restoredUsersWrapper.setProfessional(restoredProfessionalUser);
                restoredUsersWrapper.setClient(restoredClientUser);
                AppointmentWrapper result
                        = mAvailabilityManager.performAppointmentRegistration(appointmentWrapper, restoredUsersWrapper);
                if (result != null) {
                    mRepository.updateUser(result.getProfessional());
                    mRepository.updateUser(result.getClient());
                    return true;
                }
                ServSimplesApplication.logi(TAG, "result wrapper is null");
                return false;
            }
            ServSimplesApplication.logi(TAG, "Appointment is not valid");
            return false;
        }, tokenValidationCode);
    }

    @PostMapping("api/register/user")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerUser: " + getUserInfoString(user));
        UserManager userManager = UserManager.create(user);
        int validationCode = userManager.getRegisterValidationCode();
        if (validationCode == UserManager.VALID_USER) {
            User restoredUser = mRepository.getUserByCPF(user.getCpf());
            if (restoredUser != null) {
                return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_EXISTS,
                        getErrorMessageByCode(UserManager.USER_EXISTS));
            }
            mRepository.saveUser(userManager.user());
            userManager.token(mAuthManager.createTokenForUser(userManager.user()));
            return getResponseEntityFrom(HttpStatus.OK, userManager.user());
        }
        return getResponseEntityFrom(HttpStatus.FORBIDDEN,
                getErrorMessageByCode(validationCode));
    }

    @PostMapping("api/login")
    public ResponseEntity<String> login(@RequestBody User transactionUser) {
        ServSimplesApplication.logi(TAG, "login:" + getUserInfoString(transactionUser));
        UserManager tranUserMgr = UserManager.create(transactionUser);
        int validationCode = tranUserMgr.loginValidationCode();
        if (validationCode == UserManager.VALID_USER) {
            UserManager restUserMgr = UserManager.create(mRepository.getUserByUsername(tranUserMgr.username()));
            if (restUserMgr.userIsNull()) {
                return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                        getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
            }
            int loginValidationCode = mAuthManager.getLoginValidationCode(tranUserMgr.user(), restUserMgr.user());
            if (loginValidationCode == AuthManager.USER_INFO_NOT_MATCH) {
                return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_INFO_NOT_MATCH,
                        getErrorMessageByCode(AuthManager.USER_INFO_NOT_MATCH));
            }
            tranUserMgr.token(mAuthManager.createTokenForUser(tranUserMgr.user()));
            tranUserMgr.cpf(restUserMgr.cpf());
            tranUserMgr.name(restUserMgr.name());
            tranUserMgr.type(restUserMgr.type());
            return getResponseEntityFrom(HttpStatus.OK, tranUserMgr.user());
        }
        return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_INVALID,
                getErrorMessageByCode(validationCode));
    }

    @PostMapping("api/get/user")
    public ResponseEntity<String> getUSer(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "getUSer:" + getUserInfoString(user));
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        int tokenValidationCode = mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString());
        return mAuthManager.handleTokenValidation(restUserMgr::user, tokenValidationCode);
    }

    @PostMapping("api/unregister/user")
    public ResponseEntity<String> unregisterUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "unregisterUser");
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        int tokenValidationCode = mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString());
        return mAuthManager.handleTokenValidation(() -> {
            mRepository.removeUser(restUserMgr.user());
            return null;
        }, tokenValidationCode);
    }

    @PostMapping("api/update/user")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "updateUser: " + getUserInfoString(user));
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        int tokenValidationCode = mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString());
        return mAuthManager.handleTokenValidation(() -> {
            restUserMgr.username(tranUserMgr.username());
            restUserMgr.bio(tranUserMgr.bio());
            restUserMgr.password(tranUserMgr.password());
            restUserMgr.type(tranUserMgr.type());
            restUserMgr.name(tranUserMgr.name());
            mRepository.updateUser(restUserMgr.user());
            tranUserMgr.token(mAuthManager.createTokenForUser(tranUserMgr.user()));
            return tranUserMgr.user();
        }, tokenValidationCode);
    }

    @PostMapping("api/get/service/categories")
    public ResponseEntity<String> getCategories(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "getCategories:" + getUserInfoString(user));
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        if (restUserMgr.type() != User.UserType.PROFESSIONAL) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_ALLOWED,
                    getErrorMessageByCode(UserManager.USER_NOT_ALLOWED));
        }
        int tokenValidationCode = mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString());
        return mAuthManager.handleTokenValidation(this::getMockCategories, tokenValidationCode);
    }

    @PostMapping("api/register/service")
    public ResponseEntity<String> registerService(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "registerService:" + getUserInfoString(user) +
                " service info:" + getServiceInfoFromUserString(user));
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        if (restUserMgr.type() != User.UserType.PROFESSIONAL) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_ALLOWED,
                    getErrorMessageByCode(UserManager.USER_NOT_ALLOWED));
        }

        int serviceValidationCode = mServiceManager.getServiceValidationCode(tranUserMgr.service());
        if (serviceValidationCode != ServiceManager.SERVICE_VALID) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.SERVICE_INVALID,
                    getErrorMessageByCode(serviceValidationCode));
        }

        int tokenValidationCode = mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString());
        return mAuthManager.handleTokenValidation(() -> {
            restUserMgr.service(tranUserMgr.service());
            mRepository.updateUser(restUserMgr.user());
            return tranUserMgr.user();
        }, tokenValidationCode);
    }

    @PostMapping("api/update/service")
    public ResponseEntity<String> updateService(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "updateService:" + getUserInfoString(user) +
                " service info:" + getServiceInfoFromUserString(user));
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        if (restUserMgr.type() != User.UserType.PROFESSIONAL) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_ALLOWED,
                    getErrorMessageByCode(UserManager.USER_NOT_ALLOWED));
        }

        int serviceValidationCode = mServiceManager.getServiceValidationCode(tranUserMgr.service());
        if (serviceValidationCode != ServiceManager.SERVICE_VALID) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.SERVICE_INVALID,
                    getErrorMessageByCode(serviceValidationCode));
        }

        int tokenValidationCode = mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString());

        return mAuthManager.handleTokenValidation(() -> {
            restUserMgr.updateService(tranUserMgr.service());
            mRepository.updateUser(restUserMgr.user());
            return tranUserMgr.user();
        }, tokenValidationCode);
    }

    @PostMapping("api/unregister/service")
    public ResponseEntity<String> unregisterService(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "unregisterService:" + getUserInfoString(user) +
                " service info:" + getServiceInfoFromUserString(user));
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        if (restUserMgr.type() != User.UserType.PROFESSIONAL) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_ALLOWED,
                    getErrorMessageByCode(UserManager.USER_NOT_ALLOWED));
        }

        int serviceValidationCode = mServiceManager.getServiceValidationCode(tranUserMgr.service());
        if (serviceValidationCode != ServiceManager.SERVICE_VALID) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.SERVICE_INVALID,
                    getErrorMessageByCode(serviceValidationCode));
        }

        int tokenValidationCode = mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString());

        return mAuthManager.handleTokenValidation(() -> {
            restUserMgr.removeService(tranUserMgr.service());
            mRepository.updateUser(restUserMgr.user());
            return tranUserMgr.user();
        }, tokenValidationCode);
    }

    @PostMapping("api/get/service/by/category")
    public ResponseEntity<String> getAllServicesByCategory(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "getAllServicesByCategory:" + getUserInfoString(user));
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        return mAuthManager.handleTokenValidation(() ->
                        mServiceManager.getAllServicesByCategory(tranUserMgr.service().getCategory()),
                mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString()));
    }

    @PostMapping("api/get/user/by/service")
    public ResponseEntity<String> getUserFromService(@RequestBody User user) {
        ServSimplesApplication.logi(TAG, "getUserFromService:" + getUserInfoString(user)
                + " service info:" + getServiceInfoFromUserString(user));
        UserManager tranUserMgr = UserManager.create(user);
        UserManager restUserMgr = UserManager.create(mRepository.getUserByCPF(tranUserMgr.cpf()));
        if (restUserMgr.userIsNull()) {
            return getResponseEntityFrom(InterfacesWrapper.ServSimplesHTTPConstants.USER_NOT_EXISTS,
                    getErrorMessageByCode(UserManager.USER_NOT_EXISTS));
        }
        return mAuthManager.handleTokenValidation(
                () -> {
                    User userByService = mRepository.getUserByService(tranUserMgr.service());
                    User responseUser = new User();
                    responseUser.setBio(userByService.getBio());
                    responseUser.setName(userByService.getName());
                    return responseUser;
                },
                mAuthManager.getTokenValidationCode(restUserMgr.user(), tranUserMgr.tokenString()));
    }

    private List<String> getMockCategories() {
        return new ArrayList<>(Arrays.asList(
                "Saúde", "Educação", "Lazer"
        ));
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
            case UserManager.VALID_USER -> "USER VALID";
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

    private String getUserInfoString(User user) {
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

    private String getServiceInfoFromUserString(User user) {
        if (user == null) {
            return "user is null";
        }
        List<Service> services = user.getServices();
        if (services.isEmpty()) {
            return "no service found";
        }
        Service service = services.get(0);
        if (service == null) {
            return "service is null";
        }
        String response = "";
        response += "id:" + service.getId();
        response += " name:" + service.getName();
        response += " description:" + service.getDescription();
        response += " category:" + service.getCategory();
        response += " cost value:" + service.getCost().getValue();
        response += " cost time:" + service.getCost().getTime();

        return response;
    }

    private String getAvailabilityInfoToString(Availability availability) {
        if (availability == null) return " availability is null";
        return " Availability: [start time:" + availability.getStartTime() + " end time:" + availability.getEndTime() +
                " state:" + availability.getState() + " " + getAppointmentInfoString(availability.getAppointment()) +
                "]";
    }

    public static String convertToHumanReadable(long epochMillis) {
        Date date = new Date(epochMillis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
        return sdf.format(date);
    }

    private String getAppointmentInfoString(Appointment appointment) {
        if (appointment == null) return " Appointment is null";
        return " Appointment: [start time:" + appointment.getStartTime() + " end time:" + appointment.getEndTime() +
                " subscriber id:" + appointment.getSubscriberId() + "]";

    }
}