package ifpe.edu.br.servsimples.servsimples.managers;

import ifpe.edu.br.servsimples.servsimples.ServSimplesApplication;
import ifpe.edu.br.servsimples.servsimples.model.Agenda;
import ifpe.edu.br.servsimples.servsimples.model.Appointment;
import ifpe.edu.br.servsimples.servsimples.model.Availability;
import ifpe.edu.br.servsimples.servsimples.utils.AppointmentWrapper;
import lombok.NonNull;

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


    public boolean isAppointmentWrapperValid(AppointmentWrapper appointmentWrapper) {
        try {
            Availability availability = appointmentWrapper.getProfessional().getAgenda().getAvailabilities().get(0);
            Appointment appointment = appointmentWrapper.getClient().getAgenda().getAvailabilities().get(0).getAppointment();
            if (!(availability.getState() == Availability.AVAILABLE)) return false;
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
        return !(appointmentStart >= availabilityStart &&
                appointmentEnd <= availabilityEnd &&
                appointmentStart < appointmentEnd);
    }

    private boolean hasAvailabilityMatch(Availability iterationAvailability, Availability transactionAvailability) {
        return iterationAvailability.getStartTime() == transactionAvailability.getStartTime() &&
                iterationAvailability.getEndTime() == transactionAvailability.getEndTime();
    }

    private boolean isAppointmentEqualWithAvailabilityTimeSlot(UserManager tranClientMgr,
                                                               UserManager tranProfessionalMgr) {
        Appointment appointment = tranClientMgr.appointment();
        Availability availability = tranProfessionalMgr.availability();
        if (appointment == null || availability == null) return false;
        return appointment.getStartTime() == availability.getStartTime() &&
                appointment.getEndTime() == availability.getEndTime();
    }

    public AppointmentWrapper performAppointmentRegistration(@NonNull UserManager tranClientMgr,
                                                             @NonNull UserManager tranProfessionalMgr,
                                                             @NonNull UserManager rstClientMgr,
                                                             @NonNull UserManager rstProfessionalMgr) {
        if (tranClientMgr.isNull() || tranProfessionalMgr.isNull() || rstClientMgr.isNull() || rstProfessionalMgr.isNull())
            return null;

        List<Availability> restProfAvailabilities = rstProfessionalMgr.availabilities();
        if (restProfAvailabilities == null || restProfAvailabilities.isEmpty()) {
            ServSimplesApplication.logi(TAG, "Professional user has no availability");
            return null;
        }
        for (Availability profAvailIteration : restProfAvailabilities) {
            if (hasAvailabilityMatch(profAvailIteration, tranProfessionalMgr.availability())) {
                ServSimplesApplication.logi(TAG, "found professional availability");
                if (profAvailIteration.getState() != Availability.AVAILABLE) return null;
                if (hasAvailabilityConflict(tranProfessionalMgr, tranClientMgr)) return null;
                ServSimplesApplication.logi(TAG, "no conflict found");

                Appointment appointment = tranClientMgr.appointment();
                if (appointment == null) return null;

                Appointment newAppointment = new Appointment();
                newAppointment.setStartTime(appointment.getStartTime());
                newAppointment.setEndTime(appointment.getEndTime());
                newAppointment.setSubscriberId(rstClientMgr.id());

                if (isAppointmentEqualWithAvailabilityTimeSlot(tranClientMgr, tranProfessionalMgr)) {
                    ServSimplesApplication.logi(TAG, "appointment time slot equals availability time slot");

                    profAvailIteration.setAppointment(newAppointment);
                    profAvailIteration.setState(Availability.ON_HOLD);

                    Appointment clientAppointment = new Appointment();
                    clientAppointment.setSubscriberId(rstProfessionalMgr.id());
                    clientAppointment.setStartTime(profAvailIteration.getStartTime());
                    clientAppointment.setEndTime(profAvailIteration.getEndTime());

                    Availability availability = new Availability();
                    availability.setStartTime(profAvailIteration.getStartTime());
                    availability.setEndTime(profAvailIteration.getEndTime());
                    availability.setAppointment(clientAppointment);
                    availability.setState(Availability.ON_HOLD);

                    rstClientMgr.availability(availability);

                    AppointmentWrapper appointmentWrapper = new AppointmentWrapper();

                    appointmentWrapper.setClient(rstClientMgr.user());
                    appointmentWrapper.setProfessional(rstProfessionalMgr.user());
                    return appointmentWrapper;
                } else {
                    ServSimplesApplication.logi(TAG, "appointment time slot not equals availability time slot");
                    // TODO fazer essa parte aqui hahaha
                }
            }
        }
        ServSimplesApplication.logi(TAG, "availability not match");
        return null;
    }

    private boolean hasAvailabilityConflict(UserManager tranProfessionalMgr, UserManager tranClientMgr) {
        Appointment appointment = tranClientMgr.appointment();
        Availability availability = tranProfessionalMgr.availability();
        if (appointment == null || availability == null) return false;
        return !(appointment.getStartTime() >= availability.getStartTime() &&
                appointment.getEndTime() <= availability.getEndTime() &&
                appointment.getStartTime() < appointment.getEndTime());
    }
}