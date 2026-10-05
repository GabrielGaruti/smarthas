package com.smarthas.api.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Mapeia boolean <-> NUMBER(1) (0/1), padrao usado nas colunas ST_ do Oracle. */
@Converter
public class BooleanToNumberConverter implements AttributeConverter<Boolean, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Boolean value) {
        return Boolean.TRUE.equals(value) ? 1 : 0;
    }

    @Override
    public Boolean convertToEntityAttribute(Integer value) {
        return value != null && value == 1;
    }
}
