package br.com.footballmarket.market.api;

import br.com.footballmarket.market.domain.TransactionType;
import br.com.footballmarket.shared.error.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTypeParamTest {

    @Test
    void absentMeansAllTypes() {
        assertThat(TransactionTypeParam.parse(null)).containsExactlyInAnyOrder(TransactionType.values());
    }

    @Test
    void acceptsOneOrBothTypes() {
        assertThat(TransactionTypeParam.parse("VENDA")).containsExactly(TransactionType.VENDA);
        assertThat(TransactionTypeParam.parse("COMPRA,VENDA")).containsExactlyInAnyOrder(TransactionType.values());
        assertThat(TransactionTypeParam.parse("VENDA,COMPRA")).containsExactlyInAnyOrder(TransactionType.values());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "venda", "COMPRA,venda", "COMPRA,COMPRA", "COMPRA,", ",VENDA", "COMPRA, VENDA",
            "COMPRA;VENDA", "TROCA", " VENDA"})
    void rejectsAnythingElse(String raw) {
        assertThatThrownBy(() -> TransactionTypeParam.parse(raw)).isInstanceOf(BadRequestException.class);
    }
}
