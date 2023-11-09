package ifpe.edu.br.servsimples.servsimples.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_agenda_id", referencedColumnName = "agenda_id", nullable = false)
    private final Agenda agenda = new Agenda();
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_wallet_id", referencedColumnName = "wallet_id", nullable = false)
    private final Wallet wallet = new Wallet();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_notifications_id", nullable = false)
    private final List<Notification> notifications = new ArrayList<>();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_services_id", nullable = false)
    private final List<Service> services = new ArrayList<>();
    @Column(nullable = false)
    private String name;
    @Column(name = "cpf", nullable = false, unique = true)
    private String cpf;
    @Column(nullable = false)
    private String userName;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType = UserType.USER;
    @Transient
    private String token;

    public enum UserType {
        USER, PROFESSIONAL, ADMIN
    }

    /** This method *MUST* be called when getting token
     * instead a #getToken() method
     *
     *
     * @return the token in String format
     */
    public String getTokenString() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void addService(Service service) {
        this.services.add(service);
    }

    public void removeService(Service service) {
        this.services.remove(service);
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setCpf(String CPF) {
        this.cpf = CPF;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}