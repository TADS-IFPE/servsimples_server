package ifpe.edu.br.servsimples.servsimples.model;

import ifpe.edu.br.servsimples.servsimples.controller.UserController;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Agenda {

    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "agenda_id")
    private Long id;

    @OneToOne(mappedBy = "agenda")
    private User user;

    @OneToMany(fetch=FetchType.EAGER, mappedBy = "agenda", cascade=CascadeType.ALL)
    private List<Event> events = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Agenda() {
    }

    public void setEvent(Event event) {
        events.add(event);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}