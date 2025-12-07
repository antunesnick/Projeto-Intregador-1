package edu.unoeste.temperatureAPI.dto;

public interface DailySummaryDTO {
    // Os nomes dos métodos (get...) devem bater com os apelidos (AS ...) na query SQL
    String getDate();          // SQL: TO_CHAR(...) as date
    Double getAvgTemp();       // SQL: AVG(...) as avgTemp
    Double getMaxTemp();       // SQL: MAX(...) as maxTemp
    Double getAvgCurrent();    // SQL: AVG(...) as avgCurrent
}