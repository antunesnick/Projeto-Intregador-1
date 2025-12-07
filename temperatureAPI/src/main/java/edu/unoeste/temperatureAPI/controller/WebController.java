package edu.unoeste.temperatureAPI.controller;

import edu.unoeste.temperatureAPI.model.SensorLog;
import edu.unoeste.temperatureAPI.repository.SensorLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller // Note que é @Controller, não @RestController
public class WebController {

    @Autowired
    private SensorLogRepository repository;

    @GetMapping("/painel")
    public ModelAndView abrirPainel() {
        ModelAndView mv = new ModelAndView("index"); // Nome do arquivo HTML

        // Busca o último dado salvo no banco
        SensorLog ultimoDado = repository.findTopByOrderByIdDesc();

        if (ultimoDado != null) {
            // Manda os dados do banco para o HTML (Thymeleaf)
            mv.addObject("tempA", ultimoDado.getTemp1());
            mv.addObject("corrente", ultimoDado.getCurrent());
            // Como você só tem 1 sensor de temperatura no banco por enquanto, 
            // vamos repetir ou usar lógica futura para B e C
            mv.addObject("tempB", ultimoDado.getTemp2());
            mv.addObject("tempC", ultimoDado.getTemp3());
        } else {
            // Se o banco estiver vazio
            mv.addObject("tempA", "--");
            mv.addObject("corrente", "--");
        }

        return mv;
    }
}