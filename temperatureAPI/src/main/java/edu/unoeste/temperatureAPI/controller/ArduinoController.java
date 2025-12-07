package edu.unoeste.temperatureAPI.controller;

import edu.unoeste.temperatureAPI.dto.AlertDTO;
import edu.unoeste.temperatureAPI.dto.RoutineDataDTO;
import edu.unoeste.temperatureAPI.model.SensorLog;
import edu.unoeste.temperatureAPI.repository.SensorLogRepository;
import edu.unoeste.temperatureAPI.service.WhatsappService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/arduino")
public class ArduinoController {

    @Autowired
    private SensorLogRepository repository;

    @Autowired
    private WhatsappService whatsappService;


    @PostMapping("/data")
    public ResponseEntity<String> logRoutineData(@RequestBody RoutineDataDTO dto) {
        SensorLog log = new SensorLog();

        log.setTemp1(dto.getTemp1());
        log.setTemp2(dto.getTemp2());
        log.setTemp3(dto.getTemp3());

        log.setCurrent(dto.getCurrent());
        log.setDoorOpenCount(dto.getDoorOpenCount());
        log.setIsDoorCurrentlyOpen(dto.getIsDoorOpen());

        repository.save(log);
        return ResponseEntity.ok("Dados salvos.");
    }


    @PostMapping("/alert/temperature")
    public ResponseEntity<String> alertTemperature(@RequestBody AlertDTO dto) {
        String msg = "🔥 ALERTA DE TEMPERATURA!\n" +
                "O sensor registrou: " + dto.getValue() + "°C\n" +
                "Verifique o ambiente.";

        whatsappService.sendText(msg);
        return ResponseEntity.ok("Alerta de temperatura enviado.");
    }


    @PostMapping("/alert/current")
    public ResponseEntity<String> alertCurrent(@RequestBody AlertDTO dto) {
        String msg = "⚡ ALERTA DE ENERGIA!\n" +
                "Corrente anormal detectada: " + dto.getValue() + " Amperes\n" +
                "Risco de falha elétrica.";

        whatsappService.sendText(msg);
        return ResponseEntity.ok("Alerta de corrente enviado.");
    }
}