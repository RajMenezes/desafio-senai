package br.com.footballmarket.shared.config;

import br.com.footballmarket.shared.json.BrasiliaDateTimeDeserializer;
import br.com.footballmarket.shared.json.BrasiliaDateTimeSerializer;
import br.com.footballmarket.shared.json.MoneySerializer;
import br.com.footballmarket.shared.json.StrictIntegralDeserializer;
import br.com.footballmarket.shared.money.Money;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;

/**
 * JSON estrito, conforme as convenções do enunciado:
 * <ul>
 *     <li>campos em snake_case; campos desconhecidos ou calculados são ignorados sem erro;</li>
 *     <li>sem conversões implícitas: {@code "15"} não vira número, {@code 15} não vira texto,
 *     {@code "true"} não vira booleano, {@code 1} não vira enum;</li>
 *     <li>inteiros não aceitam parte fracionária ({@code 50.0} é aceito como 50);</li>
 *     <li>datas no formato da RN01 e valores monetários com 2 casas.</li>
 * </ul>
 */
@Configuration
public class JacksonConfig {

    @Bean
    Jackson2ObjectMapperBuilderCustomizer strictJsonCustomizer() {
        return builder -> builder
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .featuresToDisable(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        DeserializationFeature.ACCEPT_FLOAT_AS_INT,
                        MapperFeature.ALLOW_COERCION_OF_SCALARS)
                .featuresToEnable(
                        DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS,
                        DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                        DeserializationFeature.FAIL_ON_TRAILING_TOKENS,
                        JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .deserializerByType(Integer.class, StrictIntegralDeserializer.forInteger())
                .deserializerByType(int.class, StrictIntegralDeserializer.forInteger())
                .deserializerByType(Long.class, StrictIntegralDeserializer.forLong())
                .deserializerByType(long.class, StrictIntegralDeserializer.forLong())
                .deserializerByType(OffsetDateTime.class, new BrasiliaDateTimeDeserializer())
                .serializerByType(OffsetDateTime.class, new BrasiliaDateTimeSerializer())
                .serializerByType(Money.class, new MoneySerializer())
                .postConfigurer(JacksonConfig::disableScalarCoercions);
    }

    private static void disableScalarCoercions(ObjectMapper mapper) {
        mapper.coercionConfigFor(LogicalType.Textual)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
        mapper.coercionConfigFor(LogicalType.Float)
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
        mapper.coercionConfigFor(LogicalType.Boolean)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail);
        mapper.coercionConfigFor(LogicalType.Enum)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);
    }
}
