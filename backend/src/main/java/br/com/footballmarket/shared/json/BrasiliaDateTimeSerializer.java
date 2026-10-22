package br.com.footballmarket.shared.json;

import br.com.footballmarket.shared.time.BrasiliaTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.OffsetDateTime;

public class BrasiliaDateTimeSerializer extends StdSerializer<OffsetDateTime> {

    public BrasiliaDateTimeSerializer() {
        super(OffsetDateTime.class);
    }

    @Override
    public void serialize(OffsetDateTime value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(BrasiliaTime.format(value));
    }
}
