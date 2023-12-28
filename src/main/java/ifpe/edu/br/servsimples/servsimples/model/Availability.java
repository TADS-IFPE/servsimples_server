/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.aop.target.LazyInitTargetSource;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Availability {

    @Transient
    public static final int AVAILABLE = 0;
    @Transient
    public static final int ON_HOLD = 1;
    @Transient
    public static final int UNAVAILABLE = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time")
    private long startTime;

    @Column(name = "end_time")
    private long endTime;

    @Column(name = "availability_state", nullable = false)
    private int state;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "availability_appointment", referencedColumnName = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "agenda_id")
    private Agenda agenda;
}