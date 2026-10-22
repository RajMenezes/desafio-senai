package br.com.footballmarket.shared.time;

import br.com.footballmarket.shared.error.BadRequestException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * RN01: toda data/hora usa o horário de Brasília (sempre -03:00, sem horário de verão).
 * <ul>
 *     <li>Entrada: {@code AAAA-MM-DDTHH:MM-03:00} — qualquer outro formato, ou data/hora inexistente, é 400.</li>
 *     <li>Saída: {@code AAAA-MM-DDTHH:MM:SS-03:00}.</li>
 * </ul>
 */
public final class BrasiliaTime {

    public static final ZoneOffset OFFSET = ZoneOffset.ofHours(-3);
    public static final String INPUT_FORMAT_DESCRIPTION = "AAAA-MM-DDTHH:MM-03:00";

    private static final Pattern INPUT_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}-03:00");
    private static final DateTimeFormatter INPUT_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm").withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter OUTPUT_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssxxx");

    private BrasiliaTime() {
    }

    public static OffsetDateTime parse(String text) {
        if (text == null || !INPUT_PATTERN.matcher(text).matches()) {
            throw invalid();
        }
        try {
            String localPart = text.substring(0, text.length() - "-03:00".length());
            return LocalDateTime.parse(localPart, INPUT_FORMATTER).atOffset(OFFSET);
        } catch (DateTimeParseException e) {
            // data ou hora inexistente (ex.: 30/02, 24:00) nunca é ajustada
            throw invalid();
        }
    }

    public static String format(OffsetDateTime dateTime) {
        return dateTime.withOffsetSameInstant(OFFSET).format(OUTPUT_FORMATTER);
    }

    private static BadRequestException invalid() {
        return new BadRequestException(
                "Data e hora inválidas. Use o formato " + INPUT_FORMAT_DESCRIPTION + " com uma data existente.");
    }
}
