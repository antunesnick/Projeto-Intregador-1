package edu.unoeste.temperatureAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.http.HttpResponse;

@SpringBootApplication
public class TemperatureApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TemperatureApiApplication.class, args);
	}
}
