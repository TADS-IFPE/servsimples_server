package ifpe.edu.br.servsimples.servsimples.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Agenda {

    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "agenda_id")
    private Long id;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "agenda_events", nullable = false)
    private List<Event> events = new ArrayList<>();

    public Agenda() {
    }

    public void setEvent(Event event) {
        events.add(event);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}