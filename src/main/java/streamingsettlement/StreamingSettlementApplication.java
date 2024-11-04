package streamingsettlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StreamingSettlementApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamingSettlementApplication.class, args);
	}

}
