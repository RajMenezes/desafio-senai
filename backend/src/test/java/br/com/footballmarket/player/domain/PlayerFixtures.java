package br.com.footballmarket.player.domain;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Jogadores prontos para os testes. O "Meia Lua" é o jogador dos exemplos do enunciado (overall 75, valor base 40,00).
 */
public final class PlayerFixtures {

    public static final OffsetDateTime NOW = OffsetDateTime.of(2026, 11, 5, 16, 40, 0, 0, ZoneOffset.ofHours(-3));
    public static final Attributes MEIA_LUA_ATTRIBUTES = new Attributes(70, 68, 82, 79, 55, 64);

    private PlayerFixtures() {
    }

    public static Player meiaLua() {
        return Player.register("Meia Lua", Position.MEI, 85, 25, MEIA_LUA_ATTRIBUTES, 86, NOW);
    }

    /**
     * Jogador com os atributos do Meia Lua; o potencial respeita a regra dos 33 anos ou mais.
     */
    public static Player player(String name, Position position, int age) {
        return Player.register(name, position, 70, age, MEIA_LUA_ATTRIBUTES, age >= 33 ? 82 : 86, NOW);
    }

    public static Player withCondition(Player player, Condition condition) {
        player.changeCondition(condition, NOW);
        return player;
    }

    public static Player withId(Player player, int id) {
        ReflectionTestUtils.setField(player, "id", id);
        return player;
    }

    /**
     * Enquanto a compra (Etapa 5) não existe, o vínculo com o clube é simulado.
     */
    public static Player inClub(Player player, int clubId) {
        ReflectionTestUtils.setField(player, "clubId", clubId);
        return player;
    }
}
