package br.com.footballmarket.club.domain;

import br.com.footballmarket.shared.error.BadRequestException;
import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static br.com.footballmarket.player.domain.PlayerFixtures.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubTest {

    @Test
    void startsWith300InCash() {
        Club club = ClubFixtures.aguias();

        assertThat(club.getName()).isEqualTo("Águias FC");
        assertThat(club.getAcronym()).isEqualTo("AGU");
        assertThat(club.getCash()).isEqualTo(Money.of("300.00"));
        assertThat(club.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void nameKeyIgnoresCaseButKeepsAccents() {
        assertThat(Club.nameKey("Águias FC")).isEqualTo(Club.nameKey("ÁGUIAS fc"));
        assertThat(Club.nameKey("Águias FC")).isNotEqualTo(Club.nameKey("Aguias FC"));
    }

    @ParameterizedTest
    @CsvSource({
            "Ab C, ABC",
            "São Paulo F.C., SPF",
            "Atlético-MG Unidos, AMU",
            "Um Dois Tres Quatro Cinco, UDT"
    })
    void acceptsValidNames(String name, String acronym) {
        assertThat(Club.create(name, acronym, NOW).getName()).isEqualTo(name);
    }

    @Test
    void nameGoesUpTo60Characters() {
        String sixty = "Abcdefghijk ".repeat(4) + "Abcdefghijkl";
        assertThat(sixty).hasSize(60);

        assertThat(Club.create(sixty, "ABC", NOW).getName()).isEqualTo(sixty);
        assertThatThrownBy(() -> Club.create(sixty + "m", "ABC", NOW)).isInstanceOf(BadRequestException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Ab",                               // 2 caracteres
            "Aguias",                           // 1 palavra
            "Um Dois Tres Quatro Cinco Seis",   // 6 palavras
            "Aguias FC 2",                      // dígito
            "Aguias & FC",                      // símbolo
            "Aguias_FC Unidos",
            "Aguias - .",                       // só 1 palavra com letra
    })
    void rejectsInvalidNames(String name) {
        assertThatThrownBy(() -> Club.create(name, "AGU", NOW)).isInstanceOf(BadRequestException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "Águias FC, AGU",
            "Águias FC, AFC",
            "Águias FC, ASC",
            "Cruzeiro Esporte, CES",
            "Ação Clube, ACC",      // ç equivale a c
            ".Bravos FC, BFC",      // a primeira letra do nome, não o primeiro caractere
            "Leões do Norte, LDN"
    })
    void acceptsAcronymsMadeFromTheName(String name, String acronym) {
        assertThat(Club.create(name, acronym, NOW).getAcronym()).isEqualTo(acronym);
    }

    @ParameterizedTest
    @CsvSource({
            "Águias FC, agu",   // minúsculas
            "Águias FC, AG",    // tamanho
            "Águias FC, AGUA",
            "Águias FC, ' AGU'",
            "Águias FC, ÁGU",   // acento
            "Águias FC, A1U",
            "Águias FC, GUI",   // não começa pela primeira letra
            "Águias FC, AUG",   // fora de ordem
            "Águias FC, AGX",   // letra que não está no nome
            "Aaa Bbb, AAA",     // todas iguais
            "Arara Azul, AAA"
    })
    void rejectsInvalidAcronyms(String name, String acronym) {
        assertThatThrownBy(() -> Club.create(name, acronym, NOW)).isInstanceOf(BadRequestException.class);
    }
}
