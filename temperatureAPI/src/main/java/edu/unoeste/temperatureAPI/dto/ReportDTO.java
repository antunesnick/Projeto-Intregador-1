package edu.unoeste.temperatureAPI.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportDTO {
    private Double avgTemp1;
    private Double avgTemp2;
    private Double avgTemp3;
    private Double averageCurrent;
    private Long totalDoorOpens;
}