package ifpe.edu.br.servsimples.servsimples.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Long id;
    private String balance;
    private String creditCard;

    public boolean pay() {
        return false;
    }

    public Wallet() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

}