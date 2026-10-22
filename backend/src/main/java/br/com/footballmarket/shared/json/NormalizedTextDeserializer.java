package br.com.footballmarket.shared.json;

import br.com.footballmarket.shared.text.TextNormalizer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

public class NormalizedTextDeserializer extends StdScalarDeserializer<String> {

    public NormalizedTextDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.currentToken() != JsonToken.VALUE_STRING) {
            return (String) context.handleUnexpectedToken(String.class, parser);
        }
        return TextNormalizer.normalize(parser.getText());
    }
}
