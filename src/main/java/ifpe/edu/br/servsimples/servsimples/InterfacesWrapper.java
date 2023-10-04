package ifpe.edu.br.servsimples.servsimples;

import org.springframework.http.ResponseEntity;

public abstract class InterfacesWrapper {
    public interface IUserValidator {
        ResponseEntity<String> onResult(boolean isValid);
    }
}
