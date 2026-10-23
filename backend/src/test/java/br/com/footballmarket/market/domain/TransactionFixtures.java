package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.money.Money;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * A compra (2.3.1) e a venda (2.3.2) do Meia Lua no Águias FC, como gravadas.
 */
public final class TransactionFixtures {

    public static final OffsetDateTime PURCHASE_TIME = OffsetDateTime.of(2026, 11, 2, 10, 15, 0, 0, ZoneOffset.ofHours(-3));
    public static final OffsetDateTime SALE_TIME = OffsetDateTime.of(2026, 11, 5, 16, 40, 0, 0, ZoneOffset.ofHours(-3));

    private TransactionFixtures() {
    }

    public static MarketTransaction purchase() {
        return withId(MarketTransaction.purchase(1, 2, "Meia Lua", Money.of("37.00"), Money.of("1.85"), PURCHASE_TIME), 10);
    }

    public static MarketTransaction sale() {
        return withId(MarketTransaction.sale(1, 2, "Meia Lua", Money.of("40.00"), Money.of("0.30"), Money.of("4.00"),
                Money.of("3.00"), SALE_TIME), 11);
    }

    public static MarketTransaction withId(MarketTransaction transaction, int id) {
        ReflectionTestUtils.setField(transaction, "id", id);
        return transaction;
    }
}
