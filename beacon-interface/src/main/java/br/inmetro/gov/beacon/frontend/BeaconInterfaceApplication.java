package br.inmetro.gov.beacon.frontend;

import br.inmetro.gov.beacon.frontend.interfac.infra.AppUri;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableRabbit
public class BeaconInterfaceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeaconInterfaceApplication.class, args);
	}

	@EventListener(ContextRefreshedEvent.class)
	public void contextRefreshedEvent() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	@Bean
	public AppUri appUri(HttpServletRequest request){
		return new AppUri(request);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.setConnectTimeout(Duration.ofMillis(700))
				.setReadTimeout(Duration.ofMillis(700))
				.build();
	}

}
