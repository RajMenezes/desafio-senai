package br.com.footballmarket.club.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Um titular da escalação do clube (RN19). Chave composta por clube e jogador; o jogador é único na tabela.
 */
@Entity
@Table(name = "escalacoes")
@IdClass(LineupEntry.Key.class)
public class LineupEntry {

    @Id
    @Column(name = "clube_id", nullable = false, updatable = false)
    private Integer clubId;

    @Id
    @Column(name = "jogador_id", nullable = false, updatable = false)
    private Integer playerId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected LineupEntry() {
        // JPA
    }

    public LineupEntry(Integer clubId, Integer playerId, OffsetDateTime createdAt) {
        this.clubId = clubId;
        this.playerId = playerId;
        this.createdAt = createdAt;
    }

    public Integer getClubId() {
        return clubId;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static class Key implements Serializable {

        private Integer clubId;
        private Integer playerId;

        protected Key() {
            // JPA
        }

        public Key(Integer clubId, Integer playerId) {
            this.clubId = clubId;
            this.playerId = playerId;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && Objects.equals(clubId, key.clubId) && Objects.equals(playerId, key.playerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clubId, playerId);
        }
    }
}
