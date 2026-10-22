package br.com.footballmarket.shared.json;

import br.com.footballmarket.shared.money.Money;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Escreve {@link Money} como número JSON com 2 casas (ex.: {@code 300.00}).
 */
public class MoneySerializer extends StdSerializer<Money> {

    public MoneySerializer() {
        super(Money.class);
    }

    @Override
    public void serialize(Money value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeNumber(value.amount());
    }
}
