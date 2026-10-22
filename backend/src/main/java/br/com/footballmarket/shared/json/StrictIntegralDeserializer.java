package br.com.footballmarket.shared.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.function.Function;

/**
 * Inteiros estritos: só aceita número JSON. {@code 50} e {@code 50.0} são válidos;
 * {@code 50.5} e {@code "50"} são recusados (o Jackson, por padrão, truncaria ou converteria).
 */
public final class StrictIntegralDeserializer<T extends Number> extends StdScalarDeserializer<T> {

    private final transient Function<BigDecimal, T> exactConversion;

    private StrictIntegralDeserializer(Class<T> type, Function<BigDecimal, T> exactConversion) {
        super(type);
        this.exactConversion = exactConversion;
    }

    public static StrictIntegralDeserializer<Integer> forInteger() {
        return new StrictIntegralDeserializer<>(Integer.class, BigDecimal::intValueExact);
    }

    public static StrictIntegralDeserializer<Long> forLong() {
        return new StrictIntegralDeserializer<>(Long.class, BigDecimal::longValueExact);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token != JsonToken.VALUE_NUMBER_INT && token != JsonToken.VALUE_NUMBER_FLOAT) {
            return (T) context.handleUnexpectedToken(handledType(), parser);
        }
        BigDecimal value = parser.getDecimalValue();
        try {
            return exactConversion.apply(value);
        } catch (ArithmeticException e) {
            return (T) context.handleWeirdNumberValue(handledType(), value, "deve ser um número inteiro");
        }
    }
}
