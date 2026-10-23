package br.com.footballmarket.player.api;

import br.com.footballmarket.player.application.UpdatePlayerCommand;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.shared.json.NormalizedText;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

/**
 * Corpo do PUT /players/{id}: exige os quatro campos editáveis, mesmo que só um mude (RN08).
 * Indicadores e potencial enviados aqui são ignorados (V7).
 */
public record UpdatePlayerRequest(
        @NotNull @NormalizedText String name,
        @NotNull Position position,
        @NotNull @Range(min = 1, max = 100) Integer agility,
        @NotNull @Range(min = 16, max = 45) Integer age) {

    UpdatePlayerCommand toCommand() {
        return new UpdatePlayerCommand(name, position, agility, age);
    }
}
