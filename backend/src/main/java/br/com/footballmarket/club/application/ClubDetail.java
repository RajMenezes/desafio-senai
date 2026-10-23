package br.com.footballmarket.club.application;

import br.com.footballmarket.club.application.SquadContracts.SquadContract;
import br.com.footballmarket.club.domain.ValuedClub;

import java.util.Map;

/**
 * Clube com valores, elenco e o contrato ativo de cada jogador do elenco (2.2.2).
 */
public record ClubDetail(ValuedClub club, Map<Integer, SquadContract> contractsByPlayer) {

    public ClubDetail {
        contractsByPlayer = Map.copyOf(contractsByPlayer);
    }
}
