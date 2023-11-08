package ifpe.edu.br.servsimples.servsimples;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class InterfacesWrapper {
    public interface IUserValidator {
        ResponseEntity<String> onResult(boolean isValid);
    }

    public interface ITokenValidation{
        Object onSuccess();
    }

    public interface ServSimplesHTTPConstants {
        HttpStatus USER_EXISTS = HttpStatus.UNPROCESSABLE_ENTITY;
        HttpStatus USER_NOT_EXISTS = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
        HttpStatus USER_INFO_NOT_MATCH = HttpStatus.EXPECTATION_FAILED;
        HttpStatus USER_INVALID = HttpStatus.FORBIDDEN;
        HttpStatus OK = HttpStatus.OK;
        HttpStatus TOKEN_NOT_PRESENT = HttpStatus.MOVED_PERMANENTLY;
        HttpStatus TOKEN_DECRYPT_FAILURE = HttpStatus.SEE_OTHER;
        HttpStatus USERNAME_INVALID = HttpStatus.CONFLICT;
        HttpStatus PASSWORD_INVALID = HttpStatus.PARTIAL_CONTENT;
        HttpStatus TOKEN_EXPIRED = HttpStatus.UPGRADE_REQUIRED;
        HttpStatus FAILURE = HttpStatus.IM_USED;
    }
}
