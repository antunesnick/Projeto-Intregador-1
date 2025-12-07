package edu.unoeste.temperatureAPI.dto;
import lombok.Data;

@Data
public class AlertDTO {
    private Double value; // Valor que causou o alerta (Temp ou Corrente)
    private String message; // Opcional, se o Arduino quiser mandar texto
}