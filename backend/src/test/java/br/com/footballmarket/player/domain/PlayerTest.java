package br.com.footballmarket.player.domain;

import br.com.footballmarket.shared.error.BadRequestException;
import br.com.footballmarket.shared.error.BusinessRuleException;
import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;

import static br.com.footballmarket.player.domain.PlayerFixtures.MEIA_LUA_ATTRIBUTES;
import static br.com.footballmarket.player.domain.PlayerFixtures.NOW;
import static br.com.footballmarket.player.domain.PlayerFixtures.inClub;
import static br.com.footballmarket.player.domain.PlayerFixtures.meiaLua;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerTest {

    private static final OffsetDateTime LATER = NOW.plusDays(1);

    @Nested
    class Register {

        @Test
        void startsFreeAndFitWithBaseValueFromRegistration() {
            Player player = meiaLua();

            assertThat(player.getCondition()).isEqualTo(Condition.APTO);
            assertThat(player.getClubId()).isNull();
            assertThat(player.getDeletedAt()).isNull();
            assertThat(player.overall()).isEqualTo(75);
            assertThat(player.getBaseValue()).isEqualTo(Money.of("40.00"));
            assertThat(player.marketValue()).isEqualTo(Money.of("40.00"));
            assertThat(player.getCreatedAt()).isEqualTo(NOW);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Ana",                      // uma palavra só
                "Ana -",                    // "-" não é palavra (não tem letra)
                "Ana 2",                    // dígito
                "Jo4o Silva",
                "Ab"                        // menos de 3 caracteres
        })
        void rejectsInvalidNames(String name) {
            assertThatThrownBy(() -> register(name, 50, 25, MEIA_LUA_ATTRIBUTES, 86))
                    .isInstanceOf(BadRequestException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Ana Lima", "Ana-Maria Souza", "J. Silva", "Zé É", "Ñandú Ölçek"})
        void acceptsValidNames(String name) {
            assertThatCode(() -> register(name, 50, 25, MEIA_LUA_ATTRIBUTES, 86)).doesNotThrowAnyException();
        }

        @Test
        void nameLengthCountsCodePoints() {
            String hundredCodePoints = "𝔸" + "a".repeat(48) + " " + "b".repeat(50); // "𝔸" ocupa 2 chars em UTF-16
            assertThat(hundredCodePoints.codePointCount(0, hundredCodePoints.length())).isEqualTo(100);

            assertThatCode(() -> register(hundredCodePoints, 50, 25, MEIA_LUA_ATTRIBUTES, 86))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> register(hundredCodePoints + "c", 50, 25, MEIA_LUA_ATTRIBUTES, 86))
                    .isInstanceOf(BadRequestException.class);
        }

        @ParameterizedTest(name = "agilidade {0}, idade {1}")
        @CsvSource({"86, 35", "100, 45"})
        void veteransHaveAgilityCap(int agility, int age) {
            Attributes attributes = new Attributes(70, 68, 82, 79, 55, 64);
            assertThatThrownBy(() -> register("Ana Lima", agility, age, attributes, 82))
                    .isInstanceOf(BadRequestException.class);
        }

        @ParameterizedTest(name = "agilidade {0}, idade {1}")
        @CsvSource({"85, 35", "100, 34"})
        void agilityCapBoundaries(int agility, int age) {
            Attributes attributes = new Attributes(70, 68, 82, 79, 55, 64);
            assertThatCode(() -> register("Ana Lima", agility, age, attributes, 82)).doesNotThrowAnyException();
        }

        @Test
        void attributesSumUpToFourHundredEighty() {
            assertThatCode(() -> register("Ana Lima", 50, 25, new Attributes(80, 80, 80, 80, 80, 80), 80))
                    .doesNotThrowAnyException();
            assertThatThrownBy(() -> register("Ana Lima", 50, 25, new Attributes(81, 80, 80, 80, 80, 80), 81))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        void potentialIsNeverBelowHighestAttribute() {
            assertThatThrownBy(() -> register("Ana Lima", 50, 25, MEIA_LUA_ATTRIBUTES, 81))
                    .isInstanceOf(BadRequestException.class);
            assertThatCode(() -> register("Ana Lima", 50, 25, MEIA_LUA_ATTRIBUTES, 82))
                    .doesNotThrowAnyException();
        }

        @Test
        void fromThirtyThreePotentialEqualsHighestAttribute() {
            assertThatThrownBy(() -> register("Ana Lima", 50, 33, MEIA_LUA_ATTRIBUTES, 83))
                    .isInstanceOf(BadRequestException.class);
            assertThatCode(() -> register("Ana Lima", 50, 33, MEIA_LUA_ATTRIBUTES, 82))
                    .doesNotThrowAnyException();
            assertThatCode(() -> register("Ana Lima", 50, 32, MEIA_LUA_ATTRIBUTES, 83))
                    .doesNotThrowAnyException();
        }

        private Player register(String name, int agility, int age, Attributes attributes, int potential) {
            return Player.register(name, Position.MEI, agility, age, attributes, potential, NOW);
        }
    }

    @Nested
    class UpdateProfile {

        @Test
        void changesOnlyEditableFieldsAndKeepsBaseValue() {
            Player player = meiaLua();

            player.updateProfile("Meia Lua Nova", Position.ZAG, 70, 33, LATER);

            assertThat(player.getName()).isEqualTo("Meia Lua Nova");
            assertThat(player.getPosition()).isEqualTo(Position.ZAG);
            assertThat(player.getAgility()).isEqualTo(70);
            assertThat(player.getAge()).isEqualTo(33);
            assertThat(player.getBaseValue()).isEqualTo(Money.of("40.00"));
            assertThat(player.getPotential()).isEqualTo(86); // PUT que leva a 33+ não mexe no potencial
            assertThat(player.overall()).isNotEqualTo(75);    // overall acompanha a posição nova
            assertThat(player.getUpdatedAt()).isEqualTo(LATER);
        }

        @Test
        void playerInClubCannotChangePosition() {
            Player player = inClub(meiaLua(), 1);

            assertThatThrownBy(() -> player.updateProfile("Meia Lua", Position.ATA, 85, 25, LATER))
                    .isInstanceOf(BusinessRuleException.class);
            assertThatCode(() -> player.updateProfile("Meia Lua", Position.MEI, 60, 26, LATER))
                    .doesNotThrowAnyException();
        }

        @Test
        void ageCannotDecrease() {
            Player player = meiaLua();

            assertThatThrownBy(() -> player.updateProfile("Meia Lua", Position.MEI, 85, 24, LATER))
                    .isInstanceOf(BusinessRuleException.class);
            assertThatCode(() -> player.updateProfile("Meia Lua", Position.MEI, 85, 25, LATER))
                    .doesNotThrowAnyException();
        }

        @Test
        void validatesNameAndVeteranAgility() {
            Player player = meiaLua();

            assertThatThrownBy(() -> player.updateProfile("Meia", Position.MEI, 85, 25, LATER))
                    .isInstanceOf(BadRequestException.class);
            assertThatThrownBy(() -> player.updateProfile("Meia Lua", Position.MEI, 86, 35, LATER))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    @Nested
    class ChangeCondition {

        @Test
        void changesMarketValue() {
            Player player = meiaLua();

            player.changeCondition(Condition.MAL_CONDICIONADO, LATER);

            assertThat(player.getCondition()).isEqualTo(Condition.MAL_CONDICIONADO);
            assertThat(player.marketValue()).isEqualTo(Money.of("37.00"));
            assertThat(player.getUpdatedAt()).isEqualTo(LATER);
        }

        @Test
        void sameConditionIsAcceptedWithoutChange() {
            Player player = meiaLua();

            player.changeCondition(Condition.APTO, LATER);

            assertThat(player.getUpdatedAt()).isEqualTo(NOW);
        }

        @Test
        void sickCannotGoStraightToFit() {
            Player player = meiaLua();
            player.changeCondition(Condition.DOENTE, LATER);

            assertThatThrownBy(() -> player.changeCondition(Condition.APTO, LATER))
                    .isInstanceOf(BusinessRuleException.class);

            player.changeCondition(Condition.MAL_CONDICIONADO, LATER);
            player.changeCondition(Condition.APTO, LATER);
            assertThat(player.getCondition()).isEqualTo(Condition.APTO);
        }
    }

    @Nested
    class Delete {

        @Test
        void marksFreePlayerAsDeleted() {
            Player player = meiaLua();

            player.delete(LATER);

            assertThat(player.getDeletedAt()).isEqualTo(LATER);
        }

        @Test
        void playerInClubCannotBeDeleted() {
            Player player = inClub(meiaLua(), 1);

            assertThatThrownBy(() -> player.delete(LATER)).isInstanceOf(BusinessRuleException.class);
            assertThat(player.getDeletedAt()).isNull();
        }
    }
}
