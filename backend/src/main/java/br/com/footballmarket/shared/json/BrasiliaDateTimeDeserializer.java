package br.com.footballmarket.shared.json;

import br.com.footballmarket.shared.time.BrasiliaTime;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;

public class BrasiliaDateTimeDeserializer extends StdScalarDeserializer<OffsetDateTime> {

    public BrasiliaDateTimeDeserializer() {
        super(OffsetDateTime.class);
    }

    @Override
    public OffsetDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.currentToken() != JsonToken.VALUE_STRING) {
            return (OffsetDateTime) context.handleUnexpectedToken(OffsetDateTime.class, parser);
        }
        return BrasiliaTime.parse(parser.getText());
    }
}
