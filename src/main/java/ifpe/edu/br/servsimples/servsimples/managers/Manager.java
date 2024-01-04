/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.managers;

import com.google.gson.Gson;
import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class Manager {

    private String TAG = Manager.class.getSimpleName();

    protected ResponseEntity<String> getResponseEntityFrom(HttpStatus status, Object object) {
        try {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new Gson().toJson(object));
        } catch (Exception e) {
            ServSimplesApplication.logi(TAG, "deu merda: " + e.getMessage());
            return null;
        }
    }
}
