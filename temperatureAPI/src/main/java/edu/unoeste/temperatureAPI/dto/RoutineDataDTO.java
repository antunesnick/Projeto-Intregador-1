package edu.unoeste.temperatureAPI.dto;
import lombok.Data;

@Data
public class RoutineDataDTO {
    private Double temp1; // Antes era "temperature"
    private Double temp2; // Novo
    private Double temp3; // Novo

    private Double current;
    private Integer doorOpenCount;
    private Boolean isDoorOpen;
}