package br.com.footballmarket.market.application;

import java.time.OffsetDateTime;

public record BuyPlayerCommand(Integer playerId, Integer clubId, OffsetDateTime loyaltyUntil) {
}
