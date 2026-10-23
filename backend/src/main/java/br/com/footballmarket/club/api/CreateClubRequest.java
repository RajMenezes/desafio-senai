package br.com.footballmarket.club.api;

import br.com.footballmarket.club.application.CreateClubCommand;
import br.com.footballmarket.shared.json.NormalizedText;
import jakarta.validation.constraints.NotNull;

/**
 * Corpo do POST /clubs. {@code cash} e os valores calculados não existem aqui e, se enviados, são ignorados (V7).
 * A sigla não passa pela V4 (que vale para nomes e busca): {@code " AGU"} é recusada.
 */
public record CreateClubRequest(
        @NotNull @NormalizedText String name,
        @NotNull String acronym) {

    CreateClubCommand toCommand() {
        return new CreateClubCommand(name, acronym);
    }
}
