/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import ifpe.edu.br.servsimples.servsimples.model.Agenda;
import ifpe.edu.br.servsimples.servsimples.model.Appointment;
import ifpe.edu.br.servsimples.servsimples.model.Availability;
import ifpe.edu.br.servsimples.servsimples.utils.AppointmentWrapper;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityManager {

    public static final int AVAILABILITY_CONFLICT = -1;
    public static final int AVAILABILITY_VALID = 0;
    public static final int AVAILABILITY_END_EQUALS_BEGIN = 1;
    public static final int AVAILABILITY_END_SMALLER_BEGIN = 2;
    private final String TAG = AvailabilityManager.class.getSimpleName();

    /**
     * Esse método verifica se é possível adicionar uma
     * nova disponibilidade na agenda
     * <p>
     * Regras:
     * Fim do período não pode ser menor ou igual ao início
     * Não pode haver outra disponibilidade no mesmo intervalo
     *
     * @param agenda          a qual deseja-se adicionar a disponibilidade
     * @param newAvailability que deseja-se adicionar
     * @return um código de validação
     */
    public int getNewAvailabilityValidationCode(Agenda agenda,
                                                Availability newAvailability) {
        long startTime = newAvailability.getStartTime();
        long endTime = newAvailability.getEndTime();

        if (startTime == endTime) return AVAILABILITY_END_EQUALS_BEGIN;
        if (startTime > endTime) return AVAILABILITY_END_SMALLER_BEGIN;

        for (Availability iterationAvailability : agenda.getAvailabilities()) {
            long iaBegin = iterationAvailability.getStartTime();
            long iaEnd = iterationAvailability.getEndTime();

            if (startTime >= iaBegin && startTime <= iaEnd || endTime <= iaEnd && endTime >= iaBegin) {
                return AVAILABILITY_CONFLICT;
            }
        }
        return AVAILABILITY_VALID;
    }

    private boolean availabilityMatch(Availability iterationAvailability, Appointment appointment) {
        final long THIRTY_MINUTES = 1000 * 60 * 30;
        if (appointment.getEndTime() - appointment.getStartTime() <= THIRTY_MINUTES) return false;
        return appointment.getStartTime() >= iterationAvailability.getStartTime() &&
                appointment.getEndTime() <= iterationAvailability.getEndTime();
    }


    public AppointmentWrapper performAppointmentRegistration(@NonNull Appointment appointment,
                                                             @NonNull UserManager rstClientMgr,
                                                             @NonNull UserManager rstProfessionalMgr) {
        if (appointment == null || rstClientMgr.isNull() || rstProfessionalMgr.isNull()) return null;
        List<Availability> restProfAvailabilities = rstProfessionalMgr.availabilities();
        if (restProfAvailabilities == null || restProfAvailabilities.isEmpty()) {
            ServSimplesApplication.logi(TAG, "Professional user has no availability");
            return null;
        }
        for (Availability iterationAvailability : restProfAvailabilities) {
            if (iterationAvailability.getState() == Availability.AVAILABLE) {
                if (availabilityMatch(iterationAvailability, appointment)) {
                    ServSimplesApplication.logi(TAG, "found professional availability time slot");
                    appointment.setSubscriberId(rstClientMgr.id());

                    List<Availability> resultSet = handleAppointmentRegistration(iterationAvailability, appointment);
                    if (resultSet == null || resultSet.isEmpty()) return null;
                    rstProfessionalMgr.availabilities().remove(iterationAvailability);
                    NotificationManager notManager = NotificationManager.create(NotificationManager.APPOINTMENT_INCOMING);
                    notManager.clientId(rstClientMgr.id());
                    rstProfessionalMgr.notification(notManager.notification());
                    for (Availability resultAvailability : resultSet) {
                        rstProfessionalMgr.availability(resultAvailability);
                    }

                    Appointment clientAppointment = new Appointment();
                    clientAppointment.setStartTime(appointment.getStartTime());
                    clientAppointment.setEndTime(appointment.getEndTime());
                    clientAppointment.setSubscriberId(rstProfessionalMgr.id());

                    Availability clientAvailability = new Availability();
                    clientAvailability.setState(Availability.ON_HOLD);
                    clientAvailability.setStartTime(appointment.getStartTime());
                    clientAvailability.setEndTime(appointment.getEndTime());
                    clientAvailability.setAppointment(clientAppointment);
                    rstClientMgr.availability(clientAvailability);
                    return getAppointmentWrapper(rstClientMgr, rstProfessionalMgr);
                }
            }
        }
        ServSimplesApplication.logi(TAG, "availability not match");
        return null;
    }

    private AppointmentWrapper getAppointmentWrapper(UserManager rstClientMgr, UserManager rstProfessionalMgr) {
        if (rstClientMgr == null || rstClientMgr.isNull() || rstProfessionalMgr == null || rstProfessionalMgr.isNull()) {
            return null;
        }
        AppointmentWrapper appointmentWrapper = new AppointmentWrapper();
        appointmentWrapper.setClient(rstClientMgr.user());
        appointmentWrapper.setProfessional(rstProfessionalMgr.user());
        return appointmentWrapper;
    }

    private List<Availability> handleAppointmentRegistration(Availability availability,
                                                             Appointment appointment) {
        if (availability == null || appointment == null) return null;

        final long THIRTY_MINUTES = 1000 * 60 * 30;
        ArrayList<Availability> availabilities = new ArrayList<>();

        Availability mainAvailability = new Availability();
        mainAvailability.setAppointment(appointment);
        mainAvailability.setStartTime(appointment.getStartTime());
        mainAvailability.setEndTime(appointment.getEndTime());
        mainAvailability.setState(Availability.ON_HOLD);
        availabilities.add(mainAvailability);

        if ((appointment.getStartTime() - availability.getStartTime()) >= THIRTY_MINUTES) {
            ServSimplesApplication.logi(TAG, "The upper time slot higher than 30 minutes");
            // The upper time slot higher than 30 minutes, then creates a new availability with the remaining time
            Availability upperAvailability = new Availability();
            upperAvailability.setState(Availability.AVAILABLE);
            upperAvailability.setStartTime(availability.getStartTime());
            upperAvailability.setEndTime(appointment.getStartTime() - 1);
            availabilities.add(upperAvailability);
        }

        if ((availability.getEndTime() - appointment.getEndTime()) >= THIRTY_MINUTES) {
            ServSimplesApplication.logi(TAG, "The bottom time slot higher than 30 minutes");
            // The bottom time slot higher than 30 minutes, then creates a new availability with the remaining time
            Availability bottomAvailability = new Availability();
            bottomAvailability.setState(Availability.AVAILABLE);
            bottomAvailability.setStartTime(appointment.getEndTime() + 1);
            bottomAvailability.setEndTime(availability.getEndTime());
            availabilities.add(bottomAvailability);
        }
        return availabilities;
    }
}