package edu.unoeste.temperatureAPI.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WhatsappMessageDTO {
    private String number;
    private String text;
    private Integer delay;
}