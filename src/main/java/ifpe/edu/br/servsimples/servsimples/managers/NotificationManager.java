/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.model.Notification;

import java.util.Date;

public class NotificationManager {
    public static final int APPOINTMENT_INCOMING = 0;
    private Notification notification;

    private NotificationManager(int type) {
        switch (type) {
            case APPOINTMENT_INCOMING -> createAppointmentNotification();
        }
    }

    public static NotificationManager create(int notificationType) {
        return new NotificationManager(notificationType);
    }

    private void createAppointmentNotification() {
        Notification n = new Notification();
        n.setMessage("Você recebeu uma solicitação de serviço");
        n.setTimestamp(new Date().getTime());
        this.notification = n;
    }

    public Notification notification() {
        if (isNull()) return null;
        return notification;
    }

    public void notification(Notification notification) {
        if (isNull()) return;
        this.notification = notification;
    }

    public boolean isNull() {
        return notification == null;
    }

    public long clientId() {
        if (isNull()) return -1;
        return notification.getClientId();
    }

    public void clientId(long id) {
        if (isNull()) return;
        notification.setClientId(id);
    }

    public long professionalId() {
        if (isNull()) return -1;
        return notification.getProfessionalId();
    }

    public void professionalId(long id) {
        if (isNull()) return;
        notification.setProfessionalId(id);
    }
}
