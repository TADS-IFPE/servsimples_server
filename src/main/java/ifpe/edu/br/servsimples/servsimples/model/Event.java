package ifpe.edu.br.servsimples.servsimples.model;

import ifpe.edu.br.servsimples.servsimples.repo.Subscriber;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;


@Getter
@Entity
public class Event implements Subscriber {
    @Transient
    public static final int TYPE_PUBLISH = 0;
    @Transient
    public static final int TYPE_SUBSCRIBE = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;
    private int type;
    private String description;
    private Long start;
    private Long end;
    @Column(name = "subscribers")
    @ElementCollection
    private final Map<Long, Boolean> subscribersIds = new HashMap<>();
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_service_id", nullable = false)
    private Service service = new Service();

    public Event() {
    }

    public void setService(Service service) {
        this.service = service;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    @Override
    public void notifySubscribers() {

    }

    @Override
    public void subscribe(User user) {
        subscribersIds.put(user.getId(), true);
    }

    @Override
    public void unsubscribe(User user) {
        subscribersIds.put(user.getId(), false);
    }
}