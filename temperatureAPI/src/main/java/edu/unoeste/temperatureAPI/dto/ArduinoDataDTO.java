package edu.unoeste.temperatureAPI.dto;

import lombok.Data;

@Data
public class ArduinoDataDTO {
    private Double temperature;
    private Double voltage;
    private Boolean doorOpen;
}