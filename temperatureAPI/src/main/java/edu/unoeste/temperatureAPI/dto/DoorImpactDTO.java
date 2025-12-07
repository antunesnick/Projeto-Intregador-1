package edu.unoeste.temperatureAPI.dto;

public interface DoorImpactDTO {
    String getStatus();        // SQL: ... as status ('Aberta' ou 'Fechada')
    Double getAvgTemp();       // SQL: AVG(...) as avgTemp
    Long getCount();           // SQL: COUNT(*) as count
}