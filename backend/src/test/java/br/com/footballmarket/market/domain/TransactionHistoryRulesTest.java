package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.ZoneOffset;
import java.util.List;

import static br.com.footballmarket.market.domain.TransactionFixtures.purchase;
import static br.com.footballmarket.market.domain.TransactionFixtures.sale;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Totais (RN17) e CSV (RN18), com os exemplos 2.3.3 e 2.3.4.
 */
class TransactionHistoryRulesTest {

    @Test
    void summaryOfTheSaleOnly() {
        assertThat(TransactionSummary.of(List.of(sale()))).isEqualTo(new TransactionSummary(
                Money.ZERO, Money.of("35.70"), Money.of("0.30"), Money.of("4.00"), Money.of("3.00")));
    }

    @Test
    void summaryWithoutFilter() {
        assertThat(TransactionSummary.of(List.of(sale(), purchase()))).isEqualTo(new TransactionSummary(
                Money.of("38.85"), Money.of("35.70"), Money.of("2.15"), Money.of("4.00"), Money.of("3.00")));
    }

    @Test
    void summaryWithoutTransactionsIsZero() {
        assertThat(TransactionSummary.of(List.of()))
                .isEqualTo(new TransactionSummary(Money.ZERO, Money.ZERO, Money.ZERO, Money.ZERO, Money.ZERO));
    }

    @Test
    void profitKeepsItsSign() {
        MarketTransaction loss = MarketTransaction.sale(1, 2, "Meia Lua", Money.of("42.50"), Money.ZERO,
                Money.of("8.50"), Money.of("-7.50"), TransactionFixtures.SALE_TIME);

        assertThat(TransactionSummary.of(List.of(sale(), loss)).totalProfit()).isEqualTo(Money.of("-4.50"));
    }

    @Test
    void csvOfTheExample() {
        assertThat(TransactionCsv.write(List.of(sale(), purchase()))).isEqualTo("""
                data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa
                2026-11-05T16:40:00-03:00;VENDA;Meia Lua;40,00;0,30;4,00;3,00;35,70
                2026-11-02T10:15:00-03:00;COMPRA;Meia Lua;37,00;1,85;0,00;;-38,85
                """);
    }

    @Test
    void csvWithoutTransactionsHasOnlyTheHeader() {
        assertThat(TransactionCsv.write(List.of()))
                .isEqualTo("data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa\n");
    }

    @Test
    void csvLinesEndWithLfOnly() {
        String csv = TransactionCsv.write(List.of(sale()));

        assertThat(csv).doesNotContain("\r").endsWith("\n");
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
            "Meia Lua|Meia Lua",
            "Zé \"Foguete\" Lima|\"Zé \"\"Foguete\"\" Lima\"",
            "Ana;Lima|\"Ana;Lima\"",
            "João, o Bom|João, o Bom"
    })
    void namesWithSemicolonOrQuotesAreQuoted(String name, String expected) {
        assertThat(TransactionCsv.escape(name)).isEqualTo(expected);
    }

    @Test
    void datesAreWrittenInBrasiliaTime() {
        MarketTransaction utc = MarketTransaction.purchase(1, 2, "Meia Lua", Money.of("28.00"), Money.ZERO,
                TransactionFixtures.PURCHASE_TIME.withOffsetSameInstant(ZoneOffset.UTC));

        assertThat(TransactionCsv.write(List.of(utc))).contains("2026-11-02T10:15:00-03:00;COMPRA;Meia Lua;28,00;0,00;0,00;;-28,00\n");
    }
}
