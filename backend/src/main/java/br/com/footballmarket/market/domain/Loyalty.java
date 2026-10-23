package br.com.footballmarket.market.domain;

import br.com.footballmarket.shared.error.BadRequestException;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * Fidelidade do contrato: limites na compra e dias restantes com o percentual de multa (RN15).
 * Vale sempre o instante exato do servidor (RN01).
 */
public final class Loyalty {

    static final int MAX_DAYS = 1095;
    private static final long NANOS_PER_DAY = Duration.ofDays(1).toNanos();

    private Loyalty() {
    }

    /**
     * Na compra, o fim da fidelidade é depois do momento da compra e até 1.095 dias depois dele (400).
     */
    public static void requireValidEnd(OffsetDateTime loyaltyUntil, OffsetDateTime now) {
        if (!loyaltyUntil.isAfter(now)) {
            throw new BadRequestException("O campo 'loyalty_until' deve ser uma data futura.");
        }
        if (loyaltyUntil.isAfter(now.plusDays(MAX_DAYS))) {
            throw new BadRequestException("O campo 'loyalty_until' pode ser no máximo " + MAX_DAYS
                    + " dias depois do momento da compra.");
        }
    }

    /**
     * Dias com fração, arredondados para cima; 0 se o fim já passou ou é o próprio momento.
     * Ex.: 116 dias, 17 horas e 35 minutos → 117.
     */
    public static long remainingDays(OffsetDateTime loyaltyUntil, OffsetDateTime now) {
        Duration remaining = Duration.between(now, loyaltyUntil);
        if (remaining.isNegative() || remaining.isZero()) {
            return 0;
        }
        return (remaining.toNanos() + NANOS_PER_DAY - 1) / NANOS_PER_DAY;
    }

    /**
     * 0 dia → 0%; 1 a 30 → 5%; 31 a 180 → 10%; 181 ou mais → 20%.
     */
    public static int penaltyRate(long remainingDays) {
        if (remainingDays == 0) {
            return 0;
        }
        if (remainingDays <= 30) {
            return 5;
        }
        if (remainingDays <= 180) {
            return 10;
        }
        return 20;
    }
}
