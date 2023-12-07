package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import ifpe.edu.br.servsimples.servsimples.model.Agenda;
import ifpe.edu.br.servsimples.servsimples.model.Appointment;
import ifpe.edu.br.servsimples.servsimples.model.Availability;
import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.utils.AppointmentWrapper;

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
     *
     *  Regras:
     *      Fim do período não pode ser menor ou igual ao início
     *      Não pode haver outra disponibilidade no mesmo intervalo
     *
     * @param agenda a qual deseja-se adicionar a disponibilidade
     * @param newAvailability que deseja-se adicionar
     *
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


    public boolean isAppointmentValid(AppointmentWrapper appointmentWrapper) {
        try {
            Availability availability = appointmentWrapper.getProfessional().getAgenda().getAvailabilities().get(0);
            Appointment appointment = appointmentWrapper.getClient().getAgenda().getAvailabilities().get(0).getAppointment();
            return !hasConflict(
                    appointment.getStartTime(),
                    appointment.getEndTime(),
                    availability.getStartTime(),
                    availability.getEndTime());
        } catch (Exception e) {
            ServSimplesApplication.logi(TAG, "appointment ERROR " + e.getMessage());
            return false;
        }
    }

    private boolean hasConflict(long appointmentStart, long appointmentEnd,
                                long availabilityStart, long availabilityEnd) {
//        return !(appointmentEnd <= availabilityStart ||
//                appointmentStart >= availabilityEnd); // TODO testar comportamento
        return !(appointmentStart >= availabilityStart &&
                appointmentEnd <= availabilityEnd &&
                appointmentStart < appointmentEnd);
    }

    public AppointmentWrapper performAppointmentRegistration(AppointmentWrapper appointmentTransactionWrapper,
                                                             AppointmentWrapper restoredUsersWrapper) {
        User restoredProfessional = restoredUsersWrapper.getProfessional();
        User restoredClient = restoredUsersWrapper.getClient();

        Availability transactionAvailability = appointmentTransactionWrapper.getProfessional().getAgenda().getAvailabilities().get(0);
        Appointment transactionAppointment = appointmentTransactionWrapper.getClient().getAgenda().getAvailabilities().get(0).getAppointment();

        if (restoredClient == null || restoredProfessional == null ||
                transactionAppointment == null || transactionAvailability == null) return null;

        AppointmentWrapper resultAppointmentWrapper = new AppointmentWrapper();

        List<Availability> restoredProfessionalAvailabilities = restoredProfessional.getAgenda().getAvailabilities();
        if (restoredProfessionalAvailabilities.isEmpty()) {
            ServSimplesApplication.logi(TAG, "Professional user has no availability");
            return null;
        }
        for (Availability iterationAvailability : restoredProfessionalAvailabilities) {
            if (hasAvailabilityMatch(iterationAvailability, transactionAvailability)) {
                ServSimplesApplication.logi(TAG, "found professional availability");
                if (hasConflict(transactionAppointment.getStartTime(), transactionAppointment.getEndTime(),
                        iterationAvailability.getStartTime(), iterationAvailability.getEndTime())) return null;
                ServSimplesApplication.logi(TAG, "no conflict found");

                long originalAvailabilityStart = iterationAvailability.getStartTime();
                long originalAvailabilityEnd = iterationAvailability.getEndTime();

                if (isAppointmentTimeEqualAvailability(transactionAppointment, originalAvailabilityStart, originalAvailabilityEnd)) {
                    ServSimplesApplication.logi(TAG, "appointment time slot equals availability time slot");
                    transactionAppointment.setSubscriberId(restoredClient.getId()); // add client as subscriber
                    iterationAvailability.setAppointment(transactionAppointment);
                    iterationAvailability.setState(Availability.ON_HOLD);
                    resultAppointmentWrapper.setProfessional(restoredProfessional);
                    restoredClient.getAgenda().getAvailabilities().add(iterationAvailability);
                    resultAppointmentWrapper.setClient(restoredClient);
                    return resultAppointmentWrapper;
                } else {
                    ServSimplesApplication.logi(TAG, "appointment time slot not equals availability time slot");

                }
            }
            ServSimplesApplication.logi(TAG, "availability not match");
            return null;
        }
        ServSimplesApplication.logi(TAG, "no availability found");
        return null;
    }

    private boolean hasAvailabilityMatch(Availability iterationAvailability, Availability transactionAvailability) {
        return iterationAvailability.getStartTime() == transactionAvailability.getStartTime() &&
                iterationAvailability.getEndTime() == transactionAvailability.getEndTime();
    }

    private boolean isAppointmentTimeEqualAvailability(Appointment transactionAppointment,
                                                       long originalAvailabilityStart,
                                                       long originalAvailabilityEnd) {
        return transactionAppointment.getStartTime() == originalAvailabilityStart &&
                transactionAppointment.getEndTime() == originalAvailabilityEnd;
    }
}
