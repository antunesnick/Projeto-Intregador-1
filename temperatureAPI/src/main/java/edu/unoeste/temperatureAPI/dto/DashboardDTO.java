package edu.unoeste.temperatureAPI.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardDTO {
    private Double temp1;
    private Double temp2;
    private Double temp3;
    private Double current;
    private Boolean isDoorOpen;
    private String lastUpdate;  // Hora formatada (opcional, mas útil)
}