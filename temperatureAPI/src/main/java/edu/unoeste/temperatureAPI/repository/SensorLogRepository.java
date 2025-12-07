package edu.unoeste.temperatureAPI.repository;

import edu.unoeste.temperatureAPI.dto.DailySummaryDTO;
import edu.unoeste.temperatureAPI.dto.DoorImpactDTO;
import edu.unoeste.temperatureAPI.model.SensorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorLogRepository extends JpaRepository<SensorLog, Long> {

    // Busca o último registro (Para o Painel em Tempo Real)
    SensorLog findTopByOrderByIdDesc();

    // --- RELATÓRIOS DE MÉDIAS GERAIS ---

    @Query("SELECT AVG(s.temp1) FROM SensorLog s")
    Double findAvgTemp1();

    @Query("SELECT AVG(s.temp2) FROM SensorLog s")
    Double findAvgTemp2();

    @Query("SELECT AVG(s.temp3) FROM SensorLog s")
    Double findAvgTemp3();

    @Query("SELECT AVG(s.current) FROM SensorLog s")
    Double findAverageCurrent();

    @Query("SELECT SUM(s.doorOpenCount) FROM SensorLog s")
    Long findTotalDoorOpens();

    // --- RELATÓRIOS AVANÇADOS (Queries Nativas Atualizadas) ---

    // 1. Resumo Diário (Agora pegando média do Sensor 1 como referência)
    @Query(value = """
        SELECT 
            TO_CHAR(timestamp, 'YYYY-MM-DD') as date,
            AVG(temp_1) as avgTemp,
            MAX(temp_1) as maxTemp,
            AVG(current) as avgCurrent
        FROM sensor_logs 
        GROUP BY TO_CHAR(timestamp, 'YYYY-MM-DD') 
        ORDER BY date DESC
        LIMIT 7
    """, nativeQuery = true)
    List<DailySummaryDTO> getDailySummary();

    // 2. Impacto da Porta (Usando Sensor 1)
    @Query(value = """
        SELECT 
            CASE WHEN is_door_currently_open THEN 'Aberta' ELSE 'Fechada' END as status,
            AVG(temp_1) as avgTemp,
            COUNT(*) as count
        FROM sensor_logs
        GROUP BY is_door_currently_open
    """, nativeQuery = true)
    List<DoorImpactDTO> getDoorImpactAnalysis();

    // 3. Mapa de Calor Horário (Usando Sensor 1)
    @Query(value = """
        SELECT 
            EXTRACT(HOUR FROM timestamp) as hour,
            AVG(temp_1) as avgTemp
        FROM sensor_logs
        GROUP BY EXTRACT(HOUR FROM timestamp)
        ORDER BY avgTemp DESC
    """, nativeQuery = true)
    List<Object[]> getHourlyHeatmap();
}