package br.com.footballmarket.shared.text;

import br.com.footballmarket.shared.error.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextNormalizerTest {

    @Test
    void trimsAndCollapsesSpaces() {
        assertThat(TextNormalizer.normalize("  Meia   Zeta ")).isEqualTo("Meia Zeta");
        assertThat(TextNormalizer.normalize("São Paulo")).isEqualTo("São Paulo");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "     "})
    void blankBecomesNull(String raw) {
        assertThat(TextNormalizer.normalize(raw)).isNull();
    }

    @Test
    void nullStaysNull() {
        assertThat(TextNormalizer.normalize(null)).isNull();
    }

    @Test
    void foldIgnoresCaseAndAccents() {
        assertThat(TextNormalizer.fold("João Silva")).isEqualTo("joao silva");
        assertThat(TextNormalizer.fold("ÁÉÍÕÜÇ")).isEqualTo("aeiouc");
        assertThat(TextNormalizer.fold("Meia Lua")).contains(TextNormalizer.fold("LUA"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Meia\tZeta", "Meia\nZeta", "Meia\rZeta", "Meia\u0000Zeta", "Meia\u007FZeta"})
    void rejectsControlCharacters(String raw) {
        assertThatThrownBy(() -> TextNormalizer.normalize(raw)).isInstanceOf(BadRequestException.class);
    }
}
