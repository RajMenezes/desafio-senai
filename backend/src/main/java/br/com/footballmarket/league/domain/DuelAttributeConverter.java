package br.com.footballmarket.league.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Grava o atributo como na API ({@code "shooting"}).
 */
@Converter
public class DuelAttributeConverter implements AttributeConverter<DuelAttribute, String> {

    @Override
    public String convertToDatabaseColumn(DuelAttribute attribute) {
        return attribute == null ? null : attribute.label();
    }

    @Override
    public DuelAttribute convertToEntityAttribute(String label) {
        return label == null ? null : DuelAttribute.fromLabel(label);
    }
}
