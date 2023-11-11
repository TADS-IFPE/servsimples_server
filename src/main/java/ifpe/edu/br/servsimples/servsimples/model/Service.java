package ifpe.edu.br.servsimples.servsimples.model;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private Cost cost;
    @Column(nullable = false)
    private String name;
    private String category;
    private String description;

    public Service() {
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCost(Cost cost) {
        this.cost = cost;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}