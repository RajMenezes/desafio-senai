package br.com.footballmarket.market.domain;

/**
 * ATIVO enquanto o jogador está no clube; na venda, RESCINDIDO com multa e ENCERRADO sem multa (RN15).
 */
public enum ContractStatus {
    ATIVO,
    ENCERRADO,
    RESCINDIDO
}
