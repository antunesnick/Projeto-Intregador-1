package edu.unoeste.temperatureAPI.controller;

import edu.unoeste.temperatureAPI.dto.DashboardDTO;
import edu.unoeste.temperatureAPI.dto.ReportDTO;
import edu.unoeste.temperatureAPI.model.SensorLog;
import edu.unoeste.temperatureAPI.repository.SensorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private SensorLogRepository repository;

    @GetMapping("/summary")
    public ResponseEntity<ReportDTO> getSummaryReport() {

        // Buscando as novas médias
        Double t1 = repository.findAvgTemp1();
        Double t2 = repository.findAvgTemp2();
        Double t3 = repository.findAvgTemp3();
        Double curr = repository.findAverageCurrent();
        Long opens = repository.findTotalDoorOpens();

        // Tratando nulos
        t1 = (t1 != null) ? t1 : 0.0;
        t2 = (t2 != null) ? t2 : 0.0;
        t3 = (t3 != null) ? t3 : 0.0;
        curr = (curr != null) ? curr : 0.0;
        opens = (opens != null) ? opens : 0L;

        ReportDTO report = ReportDTO.builder()
                .avgTemp1(Math.round(t1 * 100.0) / 100.0)
                .avgTemp2(Math.round(t2 * 100.0) / 100.0)
                .avgTemp3(Math.round(t3 * 100.0) / 100.0)
                .averageCurrent(Math.round(curr * 100.0) / 100.0)
                .totalDoorOpens(opens)
                .build();

        return ResponseEntity.ok(report);
    }


    @GetMapping("/latest")
    public ResponseEntity<DashboardDTO> getLatestData() {
        SensorLog log = repository.findTopByOrderByIdDesc();
        // ... validação de null ...

        DashboardDTO dto = DashboardDTO.builder()
                .temp1(log.getTemp1())
                .temp2(log.getTemp2())
                .temp3(log.getTemp3())
                .current(log.getCurrent())
                .isDoorOpen(log.getIsDoorCurrentlyOpen())
                // ...
                .build();

        return ResponseEntity.ok(dto);
    }
}
