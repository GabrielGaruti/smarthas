package com.smarthas.api.integration;

import com.smarthas.api.domain.Measurement;
import com.smarthas.api.domain.User;
import com.smarthas.api.repository.AlertRepository;
import com.smarthas.api.repository.MeasurementRepository;
import com.smarthas.api.repository.UserRepository;
import com.smarthas.api.web.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Regras clinicas no perfil H2 (JavaRulesGateway), que espelham os packages PL/SQL.
 * Os mesmos cenarios sao verificados no Oracle por OraclePlsqlGatewayIntegrationTest.
 */
@SpringBootTest
@ActiveProfiles("test")
class JavaRulesGatewayTest {

    @Autowired ClinicalRulesGateway gateway;
    @Autowired UserRepository users;
    @Autowired MeasurementRepository measurements;
    @Autowired AlertRepository alerts;

    @Test
    void usaOMotorJavaNoPerfilH2() {
        assertThat(gateway.engine()).isEqualTo("JAVA_FALLBACK");
    }

    @Test
    void leituraCriticaGeraAlertaCriticoDeFormaIdempotente() {
        Measurement m = save(newPatient("critico@teste.com"), 186, 121, 0);

        AlertOutcome first = gateway.registerAlert(m.getId());
        AlertOutcome second = gateway.registerAlert(m.getId());

        assertThat(first.severity()).isEqualTo("CRITICO");
        assertThat(second.alertId()).isEqualTo(first.alertId());
    }

    @Test
    void tresLeiturasAltasEntreAsCincoUltimasGeramTendencia() {
        User u = newPatient("tendencia@teste.com");
        save(u, 150, 95, 3);
        save(u, 148, 92, 2);
        Measurement last = save(u, 152, 96, 0);

        assertThat(gateway.registerAlert(last.getId()).severity()).isEqualTo("ALTO");
    }

    @Test
    void leituraNormalNaoGeraAlerta() {
        Measurement m = save(newPatient("normal@teste.com"), 115, 75, 0);
        assertThat(gateway.registerAlert(m.getId()).generated()).isFalse();
    }

    @Test
    void taxaDeControleConsideraSoOPeriodo() {
        User u = newPatient("taxa@teste.com");
        save(u, 120, 80, 1);      // na meta
        save(u, 150, 95, 2);      // fora da meta
        save(u, 160, 100, 60);    // fora do periodo de 30 dias
        assertThat(gateway.controlRate(u.getId(), 30)).isEqualTo(50.0);
    }

    @Test
    void buscaDinamicaCombinaFiltrosEOrdenacao() {
        User u = newPatient("busca@teste.com");
        save(u, 141, 85, 1);
        save(u, 175, 99, 2);
        save(u, 118, 70, 3);

        MeasurementSearchResult r = gateway.searchMeasurements(new MeasurementSearchFilter(
                u.getId(), "HYPERTENSION", null, 30, null, "SISTOLICA", "DESC", 10));

        assertThat(r.total()).isEqualTo(2);
        assertThat(r.rows()).extracting(MeasurementSearchRow::systolic).containsExactly(175, 141);
        assertThat(r.rows()).allMatch(row -> row.patientName().equals("Paciente busca@teste.com"));
    }

    @Test
    void buscaDinamicaRespeitaOLimiteMasInformaOTotal() {
        MeasurementSearchResult r = gateway.searchMeasurements(new MeasurementSearchFilter(
                null, null, null, null, null, "DATA", "DESC", 2));
        assertThat(r.rows()).hasSize(2);
        assertThat(r.total()).isGreaterThan(2);
    }

    @Test
    void buscaDinamicaRecusaOrdenacaoForaDaWhitelist() {
        assertThatThrownBy(() -> gateway.searchMeasurements(new MeasurementSearchFilter(
                null, null, null, null, null, "DT_MEDICAO; DROP TABLE T_SHAS_MEDICAO", "DESC", 10)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Ordenacao invalida");
    }

    @Test
    void estatisticaAgrupaPelaDimensaoPedida() {
        List<DimensionStat> byOrigin = gateway.statsByDimension("ORIGEM", 30);
        assertThat(byOrigin).extracting(DimensionStat::group).contains("MANUAL", "SENSOR");
        assertThat(byOrigin).allMatch(s -> s.measurements() > 0);

        assertThatThrownBy(() -> gateway.statsByDimension("SENHA", 30)).isInstanceOf(ApiException.class);
    }

    @Test
    void resolverAlertasDoPacienteEncerraTodosOsAbertos() {
        User u = newPatient("resolver@teste.com");
        gateway.registerAlert(save(u, 190, 125, 1).getId());
        gateway.registerAlert(save(u, 150, 95, 0).getId());

        assertThat(gateway.resolvePatientAlerts(u.getId())).isEqualTo(2);
        assertThat(alerts.countByUserIdAndStatus(u.getId(), "ABERTO")).isZero();
    }

    @Test
    void relatorioGeraUmRegistroPorPaciente() {
        long patients = users.findAll().stream().filter(x -> x.getRole().name().equals("USER")).count();
        assertThat(gateway.generateSummaryReports(30, null)).isEqualTo(patients);
        assertThatThrownBy(() -> gateway.generateSummaryReports(0, null)).isInstanceOf(ApiException.class);
    }

    private User newPatient(String email) {
        return users.save(new User("Paciente " + email, email, "hash", com.smarthas.api.domain.Role.USER));
    }

    private Measurement save(User u, int sys, int dia, int daysAgo) {
        Measurement m = new Measurement();
        m.setUser(u);
        m.setSystolic(sys);
        m.setDiastolic(dia);
        m.setMeasuredAt(LocalDateTime.now().minusDays(daysAgo).minusMinutes(1));
        return measurements.save(m);
    }
}
