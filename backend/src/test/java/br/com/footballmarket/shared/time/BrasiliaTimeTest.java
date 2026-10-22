package br.com.footballmarket.shared.time;

import br.com.footballmarket.shared.error.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrasiliaTimeTest {

    @Test
    void parsesInputFormat() {
        OffsetDateTime parsed = BrasiliaTime.parse("2027-03-02T10:15-03:00");

        assertThat(parsed).isEqualTo(OffsetDateTime.of(2027, 3, 2, 10, 15, 0, 0, ZoneOffset.ofHours(-3)));
    }

    @Test
    void acceptsLeapDay() {
        assertThat(BrasiliaTime.parse("2028-02-29T00:00-03:00").getDayOfMonth()).isEqualTo(29);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2027-02-30T10:15-03:00",    // dia inexistente
            "2027-02-29T10:15-03:00",    // 2027 não é bissexto
            "2027-03-02T24:00-03:00",    // hora inexistente
            "2027-03-02T10:60-03:00",    // minuto inexistente
            "2027-13-02T10:15-03:00",    // mês inexistente
            "2027-03-02T10:15",          // sem offset
            "2027-03-02T10:15Z",
            "2027-03-02T10:15+00:00",
            "2027-03-02T10:15:00-03:00", // segundos não fazem parte da entrada
            "2027-03-02 10:15-03:00",
            "2027-3-2T10:15-03:00",
            "02/03/2027 10:15",
            ""
    })
    void rejectsInvalidInput(String input) {
        assertThatThrownBy(() -> BrasiliaTime.parse(input)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void formatsOutputWithSecondsInBrasiliaOffset() {
        OffsetDateTime utc = OffsetDateTime.of(2026, 11, 5, 19, 40, 0, 123_000_000, ZoneOffset.UTC);

        assertThat(BrasiliaTime.format(utc)).isEqualTo("2026-11-05T16:40:00-03:00");
    }
}
