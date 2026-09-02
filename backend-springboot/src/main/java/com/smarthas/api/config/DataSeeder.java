package com.smarthas.api.config;

import com.smarthas.api.domain.*;
import com.smarthas.api.repository.HealthUnitRepository;
import com.smarthas.api.repository.MeasurementRepository;
import com.smarthas.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Popula o banco na primeira execucao com um admin, um paciente demo,
 * algumas medicoes e unidades de saude. Assim o app/dashboard ja abrem com dados.
 *
 * Credenciais:
 *   admin@smarthas.com / admin123   (ADMIN)
 *   paciente@smarthas.com / 123456  (USER)
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MeasurementRepository measurementRepository;
    private final HealthUnitRepository healthUnitRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      MeasurementRepository measurementRepository,
                      HealthUnitRepository healthUnitRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.measurementRepository = measurementRepository;
        this.healthUnitRepository = healthUnitRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // ja populado
        }

        User admin = userRepository.save(new User(
                "Administrador Smart HAS", "admin@smarthas.com",
                passwordEncoder.encode("admin123"), Role.ADMIN));

        User paciente = userRepository.save(new User(
                "Paciente Demonstracao", "paciente@smarthas.com",
                passwordEncoder.encode("123456"), Role.USER));

        seedMeasurement(paciente, 118, 76, "2026-08-25", "08:00", "Em jejum");
        seedMeasurement(paciente, 128, 84, "2026-08-27", "09:30", "Apos caminhada");
        seedMeasurement(paciente, 145, 95, "2026-08-29", "07:15", "Dor de cabeca leve");
        seedMeasurement(paciente, 150, 98, "2026-08-31", "07:00", "Manha agitada");
        seedMeasurement(paciente, 138, 88, "2026-09-01", "22:10", "Antes de dormir");

        healthUnitRepository.save(new HealthUnit(
                "Hospital das Clinicas", "HOSPITAL", -23.5558, -46.6696, "Av. Dr. Eneas de Carvalho Aguiar, 255"));
        healthUnitRepository.save(new HealthUnit(
                "UBS Vila Mariana", "CLINIC", -23.5890, -46.6340, "Rua Sena Madureira, 1000"));
        healthUnitRepository.save(new HealthUnit(
                "Sensor IoT - Praca da Se", "SENSOR", -23.5505, -46.6333, "Praca da Se, s/n"));

        System.out.println(">> Smart HAS: banco populado com dados de demonstracao.");
    }

    private void seedMeasurement(User user, int sys, int dia, String date, String time, String notes) {
        Measurement m = new Measurement();
        m.setUser(user);
        m.setSystolic(sys);
        m.setDiastolic(dia);
        m.setDate(date);
        m.setTime(time);
        m.setNotes(notes);
        measurementRepository.save(m);
    }
}
