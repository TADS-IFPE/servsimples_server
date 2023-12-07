/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.utils;

import ifpe.edu.br.servsimples.servsimples.model.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppointmentWrapper {
    private User client;
    private User professional;
}