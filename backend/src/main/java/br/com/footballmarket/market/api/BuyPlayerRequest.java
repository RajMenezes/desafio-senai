package br.com.footballmarket.market.api;

import br.com.footballmarket.market.application.BuyPlayerCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/**
 * Corpo do POST /players/{id}/buy. {@code loyalty_until} chega no formato da RN01 (conferido na desserialização);
 * a faixa de datas depende do momento da compra e é conferida no serviço.
 */
public record BuyPlayerRequest(
        @NotNull @Min(1) Integer clubId,
        @NotNull OffsetDateTime loyaltyUntil) {

    BuyPlayerCommand toCommand(Integer playerId) {
        return new BuyPlayerCommand(playerId, clubId, loyaltyUntil);
    }
}
