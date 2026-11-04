package com.smarthas.api.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Fluxos ponta a ponta da API (HTTP -> seguranca JWT -> service -> regras -> banco H2).
 * O mesmo roteiro e repetido no pipeline como smoke test contra a API implantada com Oracle.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void healthcheckEPublico() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void rotaProtegidaExigeToken() throws Exception {
        mvc.perform(get("/measurements")).andExpect(status().isForbidden());
    }

    @Test
    void loginComSenhaErradaFalha() throws Exception {
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@smarthas.com\",\"password\":\"errada\"}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void medicaoCriticaVoltaComAlertaNaResposta() throws Exception {
        String token = login("paciente@smarthas.com", "123456");
        String body = """
                {"systolic":186,"diastolic":121,"date":"%s","time":"07:30","notes":"teste automatizado"}
                """.formatted(LocalDate.now());

        mvc.perform(post("/measurements").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.classification").value("HYPERTENSION"))
                .andExpect(jsonPath("$.alertSeverity").value("CRITICO"));
    }

    @Test
    void medicaoInvalidaERecusadaPelaValidacao() throws Exception {
        String token = login("paciente@smarthas.com", "123456");
        mvc.perform(post("/measurements").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"systolic\":20,\"diastolic\":80,\"date\":\"2026-11-01\",\"time\":\"07:00\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscaDinamicaEAdminOnly() throws Exception {
        String user = login("paciente@smarthas.com", "123456");
        mvc.perform(get("/admin/measurements/search").header("Authorization", "Bearer " + user))
                .andExpect(status().isForbidden());

        String admin = login("admin@smarthas.com", "admin123");
        mvc.perform(get("/admin/measurements/search").header("Authorization", "Bearer " + admin)
                        .param("classification", "HYPERTENSION").param("orderBy", "SISTOLICA").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.engine").value("JAVA_FALLBACK"))
                .andExpect(jsonPath("$.rows.length()").value(3))
                .andExpect(jsonPath("$.rows[0].classification").value("HYPERTENSION"));
    }

    @Test
    void filtroInvalidoNaBuscaDevolve400() throws Exception {
        String admin = login("admin@smarthas.com", "admin123");
        mvc.perform(get("/admin/measurements/search").header("Authorization", "Bearer " + admin)
                        .param("classification", "QUALQUER"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Classificacao invalida: use NORMAL, ELEVATED ou HYPERTENSION."));
    }

    @Test
    void estatisticaPorDimensaoERelatorio() throws Exception {
        String admin = login("admin@smarthas.com", "admin123");
        mvc.perform(get("/admin/measurements/stats").header("Authorization", "Bearer " + admin)
                        .param("dimension", "CLASSIFICACAO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].group").exists());

        mvc.perform(post("/admin/reports/summary").header("Authorization", "Bearer " + admin).param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generated").isNumber());
    }

    private String login(String email, String password) throws Exception {
        String response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = json.readTree(response);
        return node.get("token").asText();
    }
}
