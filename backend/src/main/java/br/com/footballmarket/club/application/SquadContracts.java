package br.com.footballmarket.club.application;

import br.com.footballmarket.shared.money.Money;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Contratos ativos do elenco, como o clube os mostra (RN11). Implementada pelo módulo de mercado,
 * para que a dependência vá num sentido só (mercado → clube).
 */
public interface SquadContracts {

    /**
     * @return contrato ativo de cada jogador do clube, pelo id do jogador
     */
    Map<Integer, SquadContract> activeByPlayer(Integer clubId);

    /**
     * Dias restantes e percentual de multa calculados no momento da consulta (RN15), nunca gravados.
     */
    record SquadContract(Money purchaseValue, OffsetDateTime loyaltyUntil, long remainingDays, int penaltyRate) {
    }
}
