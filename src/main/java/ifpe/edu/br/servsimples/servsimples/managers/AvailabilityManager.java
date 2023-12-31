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
import ifpe.edu.br.servsimples.servsimples.model.User;
import ifpe.edu.br.servsimples.servsimples.repo.Repository;
import ifpe.edu.br.servsimples.servsimples.utils.AppointmentWrapper;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class AvailabilityManager extends Manager {

    public static final int AVAILABILITY_CONFLICT = -10;
    public static final int AVAILABILITY_VALID = 10;
    public static final int AVAILABILITY_END_EQUALS_BEGIN = 11;
    public static final int AVAILABILITY_END_SMALLER_BEGIN = 21;
    public static final int AVAILABILITY_INVALID = 21;
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


    public AppointmentWrapper performAppointmentRegistration(@NonNull Appointment incomingAppointment,
                                                             @NonNull UserManager rstClientMgr,
                                                             @NonNull UserManager rstProfessionalMgr) {
        if (incomingAppointment == null || rstClientMgr.isNull() || rstProfessionalMgr.isNull()) return null;
        List<Availability> restProfAvailabilities = rstProfessionalMgr.availabilities();
        if (restProfAvailabilities == null || restProfAvailabilities.isEmpty()) {
            ServSimplesApplication.logi(TAG, "Professional user has no availability");
            return null;
        }
        for (Availability iterationAvailability : restProfAvailabilities) {
            if (iterationAvailability.getState() == Availability.AVAILABLE) {
                if (availabilityMatch(iterationAvailability, incomingAppointment)) {
                    ServSimplesApplication.logi(TAG, "found professional availability time slot");
                    incomingAppointment.setSubscriberId(rstClientMgr.id());

                    List<Availability> resultSet = handleAppointmentRegistration(iterationAvailability, incomingAppointment);
                    if (resultSet == null || resultSet.isEmpty()) return null;
                    rstProfessionalMgr.availabilities().remove(iterationAvailability);
                    NotificationManager notManager = NotificationManager.create(NotificationManager.APPOINTMENT_INCOMING);
                    notManager.clientId(rstClientMgr.id());
                    rstProfessionalMgr.notification(notManager.notification());
                    for (Availability resultAvailability : resultSet) {
                        rstProfessionalMgr.availability(resultAvailability);
                    }

                    Appointment clientAppointment = new Appointment();
                    clientAppointment.setStartTime(incomingAppointment.getStartTime());
                    clientAppointment.setEndTime(incomingAppointment.getEndTime());
                    clientAppointment.setSubscriberId(rstProfessionalMgr.id());
                    clientAppointment.setServiceId(incomingAppointment.getServiceId());

                    Availability clientAvailability = new Availability();
                    clientAvailability.setState(Availability.ON_HOLD);
                    clientAvailability.setStartTime(incomingAppointment.getStartTime());
                    clientAvailability.setEndTime(incomingAppointment.getEndTime());
                    clientAvailability.setAppointment(clientAppointment);
                    rstClientMgr.availability(clientAvailability);
                    rstClientMgr.sortAvailabilities();
                    rstProfessionalMgr.sortAvailabilities();
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

    public int handleRemoveAvailability(UserManager professionalUserMgr,
                                        Availability availability,
                                        Repository repo) {
        ServSimplesApplication.logi(TAG, "handleRemoveAvailability");
        List<Availability> professionalAvailabilities = professionalUserMgr.availabilities();
        if (professionalAvailabilities.isEmpty()) return -9848873;
        for (Availability profIterationAvail : professionalAvailabilities) {
            if (profIterationAvail.getStartTime() == availability.getStartTime() &&
                    profIterationAvail.getEndTime() == availability.getEndTime() &&
                    profIterationAvail.getState() == availability.getState()) {
                ServSimplesApplication.logi(TAG, "found availability");
                if (profIterationAvail.getState() == Availability.AVAILABLE) {
                    ServSimplesApplication.logi(TAG, "availability has no appointment");
                    professionalAvailabilities.remove(profIterationAvail);
                    professionalUserMgr.sortAvailabilities();
                    repo.updateUser(professionalUserMgr.user());
                    return 0;
                } else {
                    ServSimplesApplication.logi(TAG, "availability has a appointment registered");
                    UserManager clientMgr = UserManager.create(repo.getUserById(profIterationAvail.getAppointment().getSubscriberId()));
                    if (clientMgr.isNull()) return -1;
                    List<Availability> clientAvailabilities = clientMgr.availabilities();
                    for (Availability clientIterationAvail : clientAvailabilities) {
                        if (clientIterationAvail.getState() != Availability.AVAILABLE) {
                            long professionalId = clientIterationAvail.getAppointment().getSubscriberId();
                            if (professionalId == professionalUserMgr.id()) {
                                ServSimplesApplication.logi(TAG, "appointment found on client");
                                clientAvailabilities.remove(clientIterationAvail);
                                professionalAvailabilities.remove(profIterationAvail);

                                // notification
                                NotificationManager nm = NotificationManager.create(NotificationManager.APPOINTMENT_CANCELLING);
                                nm.professionalId(professionalUserMgr.id());
                                clientMgr.notification(nm.notification());
                                clientMgr.sortAvailabilities();
                                repo.updateUser(clientMgr.user());
                                professionalUserMgr.sortAvailabilities();
                                repo.updateUser(professionalUserMgr.user());
                                return 0;
                            }
                        }
                    }
                }
            }
        }
        ServSimplesApplication.logi(TAG, "no availability found");
        return -76948;
    }

    public boolean handleCancelAppointment(UserManager userMgr,
                                           Availability availability,
                                           Repository repository) {
        ServSimplesApplication.logi(TAG, "handleCancelAppointment");
        if (availability.getAppointment() == null) return false;
        ServSimplesApplication.logi(TAG, "appointment not null");
        List<Availability> userAvailabilities = userMgr.availabilities();
        if (userAvailabilities.isEmpty()) return false;
        ServSimplesApplication.logi(TAG, "user availabilities:" + userAvailabilities.size());
        for (Availability userAvailability : userAvailabilities) {
            if (userAvailability.getState() != Availability.AVAILABLE) {
                ServSimplesApplication.logi(TAG, "availability state:" + userAvailability.getState());
                Appointment userAppointment = userAvailability.getAppointment();
                if (userAppointment == null) return false;
                ServSimplesApplication.logi(TAG, "appointment not null");
                if (isAppointmentsEqual(availability.getAppointment(), userAppointment)) {
                    if (userMgr.type() == User.UserType.PROFESSIONAL) {
                        ServSimplesApplication.logi(TAG, "use type: professional");
                        UserManager restoredClientMgr = UserManager.create(repository.getUserById(userAppointment.getSubscriberId()));
                        if (restoredClientMgr.isNull()) return false;
                        ServSimplesApplication.logi(TAG, "client not null");
                        // notificar cliente
                        NotificationManager notificationManager = NotificationManager.create(NotificationManager.APPOINTMENT_CANCELLING);
                        restoredClientMgr.notification(notificationManager.notification());

                        List<Availability> clientAvailabilities = restoredClientMgr.availabilities();
                        for (Availability clientAvailability : clientAvailabilities) {
                            if (clientAvailability.getState() != Availability.AVAILABLE) {
                                if (clientAvailability.getAppointment().getSubscriberId() == userMgr.id() &&
                                        clientAvailability.getAppointment().getStartTime() == userAppointment.getStartTime()) {
                                    // remover disponibilidade do cliente
                                    clientAvailabilities.remove(clientAvailability);
                                    restoredClientMgr.sortAvailabilities();
                                    // atualizar cliente
                                    restoredClientMgr.sortAvailabilities();
                                    repository.updateUser(restoredClientMgr.user());
                                    userAvailability.setAppointment(null);
                                    // remover appointment do profissional
                                    userAvailability.setState(Availability.AVAILABLE);
                                    // mudar estado da disponibilidade para disponivel
                                    userMgr.sortAvailabilities();
                                    repository.updateUser(userMgr.user());
                                    // atualizar profissional
                                    return true;
                                }
                            }
                        }
                        return false;
                    } else if (userMgr.type() == User.UserType.USER) {
                        ServSimplesApplication.logi(TAG, "use type: client");
                        UserManager restoredProfMgr = UserManager.create(repository.getUserById(userAppointment.getSubscriberId()));
                        if (restoredProfMgr.isNull()) return false;
                        // notificar profissional
                        NotificationManager notificationManager = NotificationManager.create(NotificationManager.APPOINTMENT_CANCELLING);
                        restoredProfMgr.notification(notificationManager.notification());
                        List<Availability> profAvailabilities = restoredProfMgr.availabilities();
                        if (profAvailabilities.isEmpty()) return false;
                        for (Availability profAvailability : profAvailabilities) {
                            if (profAvailability.getState() != Availability.AVAILABLE) {
                                if (profAvailability.getAppointment().getSubscriberId() == userMgr.id() &&
                                        profAvailability.getAppointment().getStartTime() == userAppointment.getStartTime()) {
                                    // remover appointment do profissional
                                    profAvailability.setAppointment(null);
                                    // mudar estado da disponibilidade para disponivel
                                    profAvailability.setState(Availability.AVAILABLE);
                                    // atualizar profissional
                                    restoredProfMgr.sortAvailabilities();
                                    repository.updateUser(restoredProfMgr.user());
                                }
                            }
                        }
                        // remover disponibilidade do cliente
                        userAvailabilities.remove(userAvailability);
                        // atualizar cliente
                        userMgr.sortAvailabilities();
                        repository.updateUser(userMgr.user());
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private boolean isAppointmentsEqual(Appointment a, Appointment b) {
        return a.getStartTime() == b.getStartTime() &&
                a.getEndTime() == b.getEndTime();
    }
}