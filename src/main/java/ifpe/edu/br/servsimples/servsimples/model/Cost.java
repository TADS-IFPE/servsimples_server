package ifpe.edu.br.servsimples.servsimples.model;

import jakarta.persistence.Embeddable;

//@Embeddable
public class Cost {
    private String value;
    private String time;

    public Cost() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
