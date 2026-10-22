package br.com.footballmarket.shared.json;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um campo de texto livre do corpo (ex.: nome) para ser normalizado na desserialização.
 * Texto que fica vazio vira {@code null}, e portanto conta como ausente.
 *
 * @see br.com.footballmarket.shared.text.TextNormalizer
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonDeserialize(using = NormalizedTextDeserializer.class)
public @interface NormalizedText {
}
