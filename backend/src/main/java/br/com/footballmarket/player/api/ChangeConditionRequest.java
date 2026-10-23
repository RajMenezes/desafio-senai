package br.com.footballmarket.player.api;

import br.com.footballmarket.player.domain.Condition;
import jakarta.validation.constraints.NotNull;

public record ChangeConditionRequest(@NotNull Condition condition) {
}
