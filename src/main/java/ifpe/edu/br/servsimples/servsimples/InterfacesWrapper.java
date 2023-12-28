/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples;

import ifpe.edu.br.servsimples.servsimples.managers.UserManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class InterfacesWrapper {
    public interface IUserValidator {
        ResponseEntity<String> onResult(boolean isValid);
    }

    public interface ITokenValidation{
        Object onSuccess();

    }
    public interface IAvailabilityCallback {
        void onSuccess(UserManager userManager);
    }
    public interface ServSimplesHTTPConstants {
        HttpStatus USER_EXISTS = HttpStatus.UNPROCESSABLE_ENTITY; // 422
        HttpStatus USER_NOT_EXISTS = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE; // 416
        HttpStatus USER_INFO_NOT_MATCH = HttpStatus.EXPECTATION_FAILED; //417
        HttpStatus USER_INVALID = HttpStatus.FORBIDDEN; //403
        HttpStatus OK = HttpStatus.OK; //200
        HttpStatus TOKEN_NOT_PRESENT = HttpStatus.MOVED_PERMANENTLY; //301
        HttpStatus TOKEN_DECRYPT_FAILURE = HttpStatus.SEE_OTHER; //303
        HttpStatus USERNAME_INVALID = HttpStatus.CONFLICT; //409
        HttpStatus PASSWORD_INVALID = HttpStatus.PARTIAL_CONTENT; //206
        HttpStatus TOKEN_EXPIRED = HttpStatus.UPGRADE_REQUIRED; //426
        HttpStatus FAILURE = HttpStatus.IM_USED; //226
        HttpStatus USER_NOT_ALLOWED = HttpStatus.UNAUTHORIZED; //401

        HttpStatus SERVICE_INVALID = HttpStatus.TOO_EARLY; //425
        HttpStatus AVAILABILITY_INVALID = HttpStatus.ALREADY_REPORTED; // 208
    }
}
