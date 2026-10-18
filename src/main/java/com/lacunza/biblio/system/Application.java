package com.lacunza.biblio.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Sin @EnableScheduling, los @Scheduled de ReservationService y LoanService nunca se ejecutan
// (reservas vencidas sin marcar como EXPIRED, notificaciones sin enviar).
@SpringBootApplication
@EnableScheduling
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
