package br.com.footballmarket.club.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Grava a formação como no enunciado ({@code "2-1-1"}), e não pelo nome da constante.
 */
@Converter
public class FormationConverter implements AttributeConverter<Formation, String> {

    @Override
    public String convertToDatabaseColumn(Formation formation) {
        return formation == null ? null : formation.label();
    }

    @Override
    public Formation convertToEntityAttribute(String label) {
        return label == null ? null : Formation.fromLabel(label)
                .orElseThrow(() -> new IllegalStateException("Formação desconhecida no banco: " + label));
    }
}
