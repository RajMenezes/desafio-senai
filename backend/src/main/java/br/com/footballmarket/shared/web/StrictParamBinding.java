package br.com.footballmarket.shared.web;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Conversão estrita de path variables e query params. Por padrão o Spring aceita "01", "+1", "TRUE",
 * "yes" e trata valor vazio como ausente; o enunciado recusa tudo isso.
 * <ul>
 *     <li>Inteiros: positivos, só dígitos, sem sinal e sem zeros à esquerda.</li>
 *     <li>Decimais: {@code 10}, {@code 10.5} ou {@code 10.55}.</li>
 *     <li>Booleanos: exatamente {@code true} ou {@code false}.</li>
 *     <li>Enums: exatamente o nome da constante (case-sensitive).</li>
 *     <li>Valor vazio é sempre recusado.</li>
 * </ul>
 * Falhas viram {@code MethodArgumentTypeMismatchException}: 404 em path variable, 400 em query param.
 * <p>
 * São PropertyEditors (e não Converters) porque, quando um Converter falha, o Spring tenta de novo
 * com os editores padrão, que são permissivos. Editores registrados no binder têm precedência sobre ambos.
 */
@ControllerAdvice
public class
StrictParamBinding {

    private static final Pattern POSITIVE_INTEGER = Pattern.compile("[1-9]\\d*");
    private static final Pattern DECIMAL = Pattern.compile("(0|[1-9]\\d*)(\\.\\d{1,2})?");

    @InitBinder
    void registerStrictEditors(WebDataBinder binder) {
        binder.registerCustomEditor(Integer.class, editor(StrictParamBinding::toInteger));
        binder.registerCustomEditor(Long.class, editor(StrictParamBinding::toLong));
        binder.registerCustomEditor(BigDecimal.class, editor(StrictParamBinding::toDecimal));
        binder.registerCustomEditor(Boolean.class, editor(StrictParamBinding::toBoolean));
        // vale para qualquer enum: o editor só valida o texto; o Spring então faz o Enum.valueOf (case-sensitive)
        binder.registerCustomEditor(Enum.class, editor(StrictParamBinding::requireEnumName));
    }

    static Integer toInteger(String source) {
        requirePositiveInteger(source);
        return Integer.valueOf(source); // NumberFormatException acima de 2.147.483.647
    }

    static Long toLong(String source) {
        requirePositiveInteger(source);
        return Long.valueOf(source);
    }

    static BigDecimal toDecimal(String source) {
        if (!DECIMAL.matcher(source).matches()) {
            throw new IllegalArgumentException("Decimal inválido: " + source);
        }
        return new BigDecimal(source);
    }

    static Boolean toBoolean(String source) {
        return switch (source) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("Booleano inválido: " + source);
        };
    }

    static String requireEnumName(String source) {
        if (source.isEmpty() || !source.equals(source.strip())) {
            throw new IllegalArgumentException("Valor inválido: " + source);
        }
        return source;
    }

    private static void requirePositiveInteger(String source) {
        if (!POSITIVE_INTEGER.matcher(source).matches()) {
            throw new IllegalArgumentException("Inteiro inválido: " + source);
        }
    }

    private static PropertyEditorSupport editor(Function<String, ?> parser) {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(parser.apply(text));
            }
        };
    }
}
