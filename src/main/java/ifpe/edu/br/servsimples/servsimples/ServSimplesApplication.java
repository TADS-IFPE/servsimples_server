package ifpe.edu.br.servsimples.servsimples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServSimplesApplication {
    private static final Logger logger = LoggerFactory.getLogger(ServSimplesApplication.class);
    public static final String MAIN_TAG = "ServSimplesApp";
    public static void logi(String tag, String message){
        logger.info("[" + MAIN_TAG + "] : ["+ tag + "] :" + message);
    }
    public static void main(String[] args) {
        SpringApplication.run(ServSimplesApplication.class, args);
    }
}