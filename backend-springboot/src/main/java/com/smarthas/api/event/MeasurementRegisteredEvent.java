package com.smarthas.api.event;

/** Evento de dominio publicado quando uma medicao e criada ou alterada. */
public record MeasurementRegisteredEvent(Long measurementId, Long userId) { }
