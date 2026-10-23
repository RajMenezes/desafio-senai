package br.com.footballmarket.player.api;

import br.com.footballmarket.player.application.RegisterPlayerCommand;
import br.com.footballmarket.player.domain.Attributes;
import br.com.footballmarket.player.domain.Position;
import br.com.footballmarket.shared.json.NormalizedText;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

/**
 * Corpo do POST /players. Campos calculados ou controlados pelo sistema (id, condition, club_id,
 * base_value, overall, market_value) não existem aqui e, se enviados, são ignorados (V7).
 */
public record RegisterPlayerRequest(
        @NotNull @NormalizedText String name,
        @NotNull Position position,
        @NotNull @Range(min = 1, max = 100) Integer agility,
        @NotNull @Range(min = 16, max = 45) Integer age,
        @NotNull @Range(min = 1, max = 99) Integer pace,
        @NotNull @Range(min = 1, max = 99) Integer shooting,
        @NotNull @Range(min = 1, max = 99) Integer passing,
        @NotNull @Range(min = 1, max = 99) Integer dribbling,
        @NotNull @Range(min = 1, max = 99) Integer defending,
        @NotNull @Range(min = 1, max = 99) Integer physical,
        @NotNull @Range(min = 1, max = 99) Integer potential) {

    RegisterPlayerCommand toCommand() {
        Attributes attributes = new Attributes(pace, shooting, passing, dribbling, defending, physical);
        return new RegisterPlayerCommand(name, position, agility, age, attributes, potential);
    }
}
