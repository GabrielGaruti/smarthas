package com.smarthas.api.web;

import com.smarthas.api.repository.HealthUnitRepository;
import com.smarthas.api.repository.MeasurementRepository;
import com.smarthas.api.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Controller MVC (server-side) que renderiza uma pagina de visao geral com Thymeleaf.
 * Demonstra o uso de Spring MVC + Thymeleaf exigido pela atividade.
 */
@Controller
public class ViewController {

    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final HealthUnitRepository healthUnitRepository;

    public ViewController(UserRepository userRepository,
                          MeasurementRepository measurementRepository,
                          HealthUnitRepository healthUnitRepository) {
        this.userRepository = userRepository;
        this.measurementRepository = measurementRepository;
        this.healthUnitRepository = healthUnitRepository;
    }

    @GetMapping({"/", "/overview"})
    public String overview(Model model) {
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("measurementCount", measurementRepository.count());
        model.addAttribute("unitCount", healthUnitRepository.count());
        model.addAttribute("recent",
                measurementRepository.findAll(PageRequest.of(0, 8, Sort.by("id").descending())).getContent());
        return "overview";
    }
}
