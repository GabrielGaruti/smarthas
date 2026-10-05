package com.smarthas.api.config;

import com.smarthas.api.domain.*;
import com.smarthas.api.integration.ClinicalRulesGateway;
import com.smarthas.api.repository.HealthUnitRepository;
import com.smarthas.api.repository.MeasurementRepository;
import com.smarthas.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Popula o banco H2 na primeira execucao com os MESMOS dados de demonstracao do
 * script database/oracle/04_carga_dados.sql (datas relativas a hoje).
 * No perfil "oracle" os dados ja vem dos scripts SQL, entao este seeder nao faz nada
 * (a tabela de usuarios ja esta preenchida).
 *
 * Credenciais:
 *   admin@smarthas.com / admin123   (ADMIN)
 *   paciente@smarthas.com / 123456  (USER)  - os demais pacientes tambem usam 123456
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final HealthUnitRepository healthUnitRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClinicalRulesGateway gateway;

    public DataSeeder(UserRepository userRepository,
                      MeasurementRepository measurementRepository,
                      HealthUnitRepository healthUnitRepository,
                      PasswordEncoder passwordEncoder,
                      ClinicalRulesGateway gateway) {
        this.userRepository = userRepository;
        this.measurementRepository = measurementRepository;
        this.healthUnitRepository = healthUnitRepository;
        this.passwordEncoder = passwordEncoder;
        this.gateway = gateway;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // ja populado (H2 de execucao anterior ou scripts Oracle)
        }

        String pwd = passwordEncoder.encode("123456");
        userRepository.save(new User("Administrador Smart HAS", "admin@smarthas.com",
                passwordEncoder.encode("admin123"), Role.ADMIN));
        User paciente = userRepository.save(new User("Paciente Demonstracao", "paciente@smarthas.com", pwd, Role.USER));
        User maria = userRepository.save(new User("Maria Aparecida Souza", "maria.souza@smarthas.com", pwd, Role.USER));
        User joao = userRepository.save(new User("Joao Carlos Lima", "joao.lima@smarthas.com", pwd, Role.USER));
        User ana = userRepository.save(new User("Ana Beatriz Rocha", "ana.rocha@smarthas.com", pwd, Role.USER));
        userRepository.save(new User("Roberto Ferreira", "roberto.ferreira@smarthas.com", pwd, Role.USER)); // sem leituras

        List<Measurement> seeded = new ArrayList<>();
        seeded.add(manual(paciente, 12, "08:00", 118, 76, "Em jejum"));
        seeded.add(manual(paciente, 10, "09:30", 128, 84, "Apos caminhada"));
        seeded.add(manual(paciente, 8, "07:15", 145, 95, "Dor de cabeca leve"));
        seeded.add(manual(paciente, 6, "07:00", 150, 98, "Manha agitada"));
        seeded.add(manual(paciente, 4, "22:10", 138, 88, "Antes de dormir"));
        seeded.add(manual(paciente, 2, "07:10", 152, 96, "Esqueceu a medicacao"));
        seeded.add(manual(maria, 9, "08:20", 116, 74, null));
        seeded.add(manual(maria, 5, "08:05", 119, 78, null));
        seeded.add(manual(maria, 1, "08:15", 121, 79, "Pouco sono"));
        seeded.add(manual(joao, 7, "19:40", 162, 101, "Stress no trabalho"));
        seeded.add(manual(joao, 3, "20:00", 184, 118, "Tontura e visao turva"));
        seeded.add(manual(ana, 6, "10:00", 132, 85, null));
        seeded.add(manual(ana, 2, "10:30", 126, 82, null));
        // leituras "de sensor" (no Oracle sao geradas por PRC_SHAS_SIMULAR_LEITURAS_SENSOR)
        seeded.add(sensor(paciente, 11, "06:45", 134, 86, 74));
        seeded.add(sensor(paciente, 5, "06:50", 147, 93, 81));
        seeded.add(sensor(maria, 3, "07:30", 112, 72, 66));

        // avalia cada leitura com a mesma regra da procedure (gera os alertas historicos)
        seeded.forEach(m -> gateway.registerAlert(m.getId()));

        healthUnitRepository.save(new HealthUnit(
                "Hospital das Clinicas", "HOSPITAL", -23.5558, -46.6696, "Av. Dr. Eneas de Carvalho Aguiar, 255"));
        healthUnitRepository.save(new HealthUnit(
                "UBS Vila Mariana", "CLINIC", -23.5890, -46.6340, "Rua Sena Madureira, 1000"));
        healthUnitRepository.save(new HealthUnit(
                "Sensor IoT - Praca da Se", "SENSOR", -23.5505, -46.6333, "Praca da Se, s/n"));
        healthUnitRepository.save(new HealthUnit(
                "Hospital Regional de Osasco", "HOSPITAL", -23.5325, -46.7917, "Rua Ari Barroso, 355 - Osasco"));

        System.out.println(">> Smart HAS: banco H2 populado com dados de demonstracao (motor: " + gateway.engine() + ").");
    }

    private Measurement manual(User user, int daysAgo, String time, int sys, int dia, String notes) {
        return save(user, daysAgo, time, sys, dia, null, notes, Measurement.SOURCE_MANUAL);
    }

    private Measurement sensor(User user, int daysAgo, String time, int sys, int dia, int hr) {
        return save(user, daysAgo, time, sys, dia, hr, "Leitura automatica do dispositivo", Measurement.SOURCE_SENSOR);
    }

    private Measurement save(User user, int daysAgo, String time, int sys, int dia,
                             Integer hr, String notes, String source) {
        Measurement m = new Measurement();
        m.setUser(user);
        m.setSystolic(sys);
        m.setDiastolic(dia);
        m.setHeartRate(hr);
        m.setMeasuredAt(LocalDateTime.of(LocalDate.now().minusDays(daysAgo), LocalTime.parse(time)));
        m.setNotes(notes);
        m.setSource(source);
        return measurementRepository.save(m);
    }
}
