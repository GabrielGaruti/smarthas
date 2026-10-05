package com.smarthas.api.event;

import com.smarthas.api.integration.AlertOutcome;
import com.smarthas.api.integration.ClinicalRulesGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Evento de back-end que aciona a procedure PRC_SHAS_REGISTRAR_ALERTA.
 *
 * Fluxo: POST /measurements (REST) -> MeasurementService grava e publica o evento
 *        -> apos o COMMIT este listener chama o gateway (JDBC) -> procedure Oracle.
 *
 * - AFTER_COMMIT: a procedure so roda quando a leitura ja esta gravada e visivel no banco.
 * - REQUIRES_NEW: o alerta e salvo em transacao propria.
 * - Falhas na regra nao desfazem a medicao do paciente (resiliencia); ficam no log.
 */
@Component
public class MeasurementAlertListener {

    private static final Logger log = LoggerFactory.getLogger(MeasurementAlertListener.class);

    private final ClinicalRulesGateway gateway;

    public MeasurementAlertListener(ClinicalRulesGateway gateway) {
        this.gateway = gateway;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMeasurementRegistered(MeasurementRegisteredEvent event) {
        try {
            AlertOutcome outcome = gateway.registerAlert(event.measurementId());
            if (outcome.generated()) {
                log.info("[{}] Alerta {} ({}) gerado para a medicao {}",
                        gateway.engine(), outcome.alertId(), outcome.severity(), event.measurementId());
            }
        } catch (Exception ex) {
            log.error("Falha ao avaliar alerta da medicao {}: {}", event.measurementId(), ex.getMessage());
        }
    }
}
