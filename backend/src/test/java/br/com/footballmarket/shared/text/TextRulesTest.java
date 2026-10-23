package br.com.footballmarket.shared.text;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TextRulesTest {

    private static Stream<String> sorted(String... names) {
        return Stream.of(names).sorted(TextRules.ALPHABETICAL);
    }

    @Test
    void alphabeticalIgnoresCaseAndAccents() {
        assertThat(sorted("Gama", "Épsilon", "Delta")).containsExactly("Delta", "Épsilon", "Gama");
        assertThat(sorted("bruno", "Álvaro", "Alan")).containsExactly("Alan", "Álvaro", "bruno");
        assertThat(sorted("Danilo", "Çarlos", "Bia")).containsExactly("Bia", "Çarlos", "Danilo");
    }

    @Test
    void spaceComesBeforeHyphenAndHyphenBeforeLetters() {
        assertThat(sorted("Ana-Maria", "Anabela", "Ana Lima")).containsExactly("Ana Lima", "Ana-Maria", "Anabela");
    }

    @Test
    void prefixComesFirst() {
        assertThat(sorted("Ana Lima", "Ana")).containsExactly("Ana", "Ana Lima");
    }

    @Test
    void namesThatFoldToTheSameTextTie() {
        assertThat(TextRules.ALPHABETICAL.compare("Águias FC", "Aguias FC")).isZero();
        assertThat(TextRules.ALPHABETICAL.compare("ÁGUIAS FC", "águias fc")).isZero();
    }

    @Test
    void comparesCodePointsNotUtf16Units() {
        // U+FF41 (ａ) < U+1D400 (𝐀), mas em UTF-16 o 𝐀 começa com 0xD835, que é menor que 0xFF41
        assertThat(TextRules.compareCodePoints("ａ", "𝐀")).isNegative();
    }

    @Test
    void wordsAndLength() {
        assertThat(TextRules.wordCount("Águias - F.C.")).isEqualTo(2);
        assertThat(TextRules.length("Zé 😀")).isEqualTo(4);
    }
}
