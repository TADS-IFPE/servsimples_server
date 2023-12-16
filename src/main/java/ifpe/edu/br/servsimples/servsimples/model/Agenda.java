/*
 * Dispositivos Móveis - IFPE 2023
 * Author: Willian Santos
 * Project: ServSimplesApp
 */
package ifpe.edu.br.servsimples.servsimples.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Agenda {

    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "agenda_id")
    private Long id;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "agenda_events", nullable = false)
    private List<Event> events = new ArrayList<>();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "agenda_availabilities")
    private List<Availability> availabilities = new ArrayList<>();
    @OneToOne(mappedBy = "agenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private User user;

    public Agenda() {
    }

    public void addAvailability(Availability availability){
        if (!availabilities.contains(availability)) {
            availabilities.add(availability);
        }
    }
}