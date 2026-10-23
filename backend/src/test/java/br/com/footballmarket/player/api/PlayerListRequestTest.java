package br.com.footballmarket.player.api;

import br.com.footballmarket.player.application.PlayerQuery;
import br.com.footballmarket.player.domain.PlayerSort;
import br.com.footballmarket.shared.error.BadRequestException;
import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlayerListRequestTest {

    private static PlayerListRequest withSearch(String search) {
        return new PlayerListRequest(search, null, null, null, null, null, null, null, null, null, null, null);
    }

    private static PlayerListRequest withSort(String sort) {
        return new PlayerListRequest(null, null, null, null, null, null, null, null, null, sort, null, null);
    }

    private static PlayerListRequest withAges(Integer minAge, Integer maxAge) {
        return new PlayerListRequest(null, null, null, null, null, minAge, maxAge, null, null, null, null, null);
    }

    private static PlayerListRequest withValues(String minValue, String maxValue) {
        return new PlayerListRequest(null, null, null, null, null, null, null,
                minValue == null ? null : new BigDecimal(minValue),
                maxValue == null ? null : new BigDecimal(maxValue), null, null, null);
    }

    @Test
    void absentParamsUseTheDefaults() {
        PlayerQuery query = withSearch(null).toQuery();

        assertThat(query.sort()).containsExactly(PlayerSort.NAME);
        assertThat(query.page()).isEqualTo(1);
        assertThat(query.limit()).isEqualTo(10);
        assertThat(query.search()).isNull();
        assertThat(query.minValue()).isNull();
    }

    @Test
    void searchIsNormalizedBeforeMeasuring() {
        assertThat(withSearch("  meia   lua ").toQuery().search()).isEqualTo("meia lua");
        assertThat(withSearch(" ab ").toQuery().search()).isEqualTo("ab");
        assertThat(withSearch("x".repeat(100)).toQuery().search()).hasSize(100);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "a", " a ", "meia\tlua"})
    void rejectsEmptyShortOrControlCharacterSearch(String search) {
        assertThatThrownBy(() -> withSearch(search).toQuery()).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsSearchLongerThan100Characters() {
        assertThatThrownBy(() -> withSearch("x".repeat(101)).toQuery()).isInstanceOf(BadRequestException.class);
    }

    @Test
    void parsesUpToTwoSortCriteriaInOrder() {
        assertThat(withSort("position,value_desc").toQuery().sort())
                .containsExactly(PlayerSort.POSITION, PlayerSort.VALUE_DESC);
        assertThat(withSort("age_asc").toQuery().sort()).containsExactly(PlayerSort.AGE_ASC);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "POSITION", "value", "name,", ",name", "name, age_asc", "value_asc,value_desc",
            "name,name", "age_asc,age_desc", "name,age_asc,position", "price"})
    void rejectsInvalidSort(String sort) {
        assertThatThrownBy(() -> withSort(sort).toQuery()).isInstanceOf(BadRequestException.class);
    }

    @Test
    void ageRangeIsInclusiveAndOpenEndedWhenOneLimitIsMissing() {
        PlayerQuery query = withAges(16, 45).toQuery();
        assertThat(query.minAge()).isEqualTo(16);
        assertThat(query.maxAge()).isEqualTo(45);
        assertThat(withAges(30, 30).toQuery().maxAge()).isEqualTo(30);
        assertThat(withAges(null, 20).toQuery().minAge()).isNull();
    }

    @Test
    void rejectsAgesOutOfRangeOrInverted() {
        assertThatThrownBy(() -> withAges(15, null).toQuery()).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> withAges(null, 46).toQuery()).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> withAges(30, 29).toQuery()).isInstanceOf(BadRequestException.class);
    }

    @Test
    void valueRangeBecomesMoney() {
        PlayerQuery query = withValues("0", "550").toQuery();
        assertThat(query.minValue()).isEqualTo(Money.ZERO);
        assertThat(query.maxValue()).isEqualTo(Money.of("550.00"));
        assertThat(withValues("10.5", "10.5").toQuery().minValue()).isEqualTo(Money.of("10.50"));
    }

    @Test
    void rejectsValuesOutOfRangeOrInverted() {
        assertThatThrownBy(() -> withValues("550.01", null).toQuery()).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> withValues(null, "551").toQuery()).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> withValues("40.01", "40").toQuery()).isInstanceOf(BadRequestException.class);
    }

    @Test
    void freeTrueContradictsClubButFreeFalseDoesNot() {
        assertThatThrownBy(() -> new PlayerListRequest(null, null, null, true, 1, null, null, null, null, null, null, null)
                .toQuery()).isInstanceOf(BadRequestException.class);

        PlayerQuery query = new PlayerListRequest(null, null, null, false, 1, null, null, null, null, null, null, null)
                .toQuery();
        assertThat(query.free()).isFalse();
        assertThat(query.clubId()).isEqualTo(1);
    }

    @Test
    void limitGoesUpTo50() {
        assertThat(new PlayerListRequest(null, null, null, null, null, null, null, null, null, null, 3, 50)
                .toQuery().limit()).isEqualTo(50);
        assertThatThrownBy(() -> new PlayerListRequest(null, null, null, null, null, null, null, null, null, null, 1, 51)
                .toQuery()).isInstanceOf(BadRequestException.class);
    }
}
