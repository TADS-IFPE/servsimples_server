package ifpe.edu.br.servsimples.servsimples.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Cost {
    private String value;
    private String time;

    public Cost() {
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
