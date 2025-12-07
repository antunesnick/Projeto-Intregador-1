package edu.unoeste.temperatureAPI.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Service
public class WhatsappService {

    private final RestClient restClient;

    @Value("${evolution.api.instance}")
    private String instanceName;

    @Value("${evolution.api.token}")
    private String apiKey;

    @Value("${evolution.api.admin-number}")
    private String adminNumber;


    public WhatsappService(@Value("${evolution.api.url}") String baseUrl) {
        // Configura uma fábrica simples (timeout e HTTP 1.1)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory) // <--- ADICIONE ISTO
                .build();
    }
    public void sendText(String text) {
        try {
            var body = Map.of(
                    "number", adminNumber,
                    "text", text,
                    "delay", 1200
            );

            System.out.println("TENTANDO ENVIAR PARA: " + adminNumber);
            System.out.println("CONTEÚDO: " + body);
            System.out.println(restClient.post()
                    .uri("/message/sendText/{instance}", instanceName)
                    .header("apikey", apiKey)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .retrieve()
                    .toBodilessEntity());



            System.out.println("Mensagem enviada: " + text);
        } catch (Exception e) {
            System.err.println("Erro ao enviar WhatsApp: " + e.getMessage());
        }
    }
}