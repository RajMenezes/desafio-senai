package br.com.footballmarket.market.api;

import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.application.LineupService;
import br.com.footballmarket.club.application.SquadContracts;
import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.infra.ClubRepository;
import br.com.footballmarket.market.application.PurchaseService;
import br.com.footballmarket.market.application.SaleService;
import br.com.footballmarket.market.domain.Contract;
import br.com.footballmarket.market.domain.ContractStatus;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionType;
import br.com.footballmarket.market.infra.ContractRepository;
import br.com.footballmarket.market.infra.TransactionRepository;
import br.com.footballmarket.player.application.ClubDirectory;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.application.StarterRemoval;
import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.PlayerFixtures;
import br.com.footballmarket.player.infra.PlayerRepository;
import br.com.footballmarket.shared.config.JacksonConfig;
import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static br.com.footballmarket.club.domain.ClubFixtures.aguias;
import static br.com.footballmarket.club.domain.ClubFixtures.withCash;
import static br.com.footballmarket.club.domain.ClubFixtures.withId;
import static br.com.footballmarket.player.domain.PlayerFixtures.inClub;
import static br.com.footballmarket.player.domain.PlayerFixtures.meiaLua;
import static br.com.footballmarket.player.domain.PlayerFixtures.withCondition;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Compra com HTTP, serviços e domínio reais; só os repositórios são simulados.
 * O relógio está no momento do exemplo 2.3.1 (02/11/2026, 10h15).
 */
@WebMvcTest(MarketController.class)
@Import({PurchaseService.class, SaleService.class, PlayerService.class, ClubService.class, JacksonConfig.class,
        MarketControllerTest.PurchaseClock.class})
class MarketControllerTest {

    static final OffsetDateTime PURCHASE_TIME = OffsetDateTime.of(2026, 11, 2, 10, 15, 0, 0, ZoneOffset.ofHours(-3));
    private static final String BUY_AGUIAS = "{\"club_id\": 1, \"loyalty_until\": \"2027-03-02T10:15-03:00\"}";

    @TestConfiguration
    static class PurchaseClock {
        @Bean
        Clock clock() {
            return Clock.fixed(PURCHASE_TIME.toInstant(), ZoneOffset.ofHours(-3));
        }
    }

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private PlayerRepository playerRepository;

    @MockitoBean
    private ClubRepository clubRepository;

    @MockitoBean
    private ContractRepository contractRepository;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private StarterRemoval starterRemoval;

    @MockitoBean
    private LineupService lineupService;

    @MockitoBean
    private ClubDirectory clubDirectory;

    @MockitoBean
    private SquadContracts squadContracts;

    private Player meiaLua;
    private Club aguias;

    @BeforeEach
    void givenTheExampleScenario() {
        // Meia Lua MAL_CONDICIONADO vale 37,00; o Águias FC tem 300,00
        meiaLua = PlayerFixtures.withId(
                withCondition(meiaLua(), Condition.MAL_CONDICIONADO), 2);
        aguias = withId(aguias(), 1);
        when(playerRepository.findActiveForUpdate(2)).thenReturn(Optional.of(meiaLua));
        when(clubRepository.findByIdForUpdate(1)).thenReturn(Optional.of(aguias));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(MarketTransaction.class))).thenAnswer(invocation -> {
            MarketTransaction transaction = invocation.getArgument(0);
            ReflectionTestUtils.setField(transaction, "id", 10);
            return transaction;
        });
    }

    private ResultActions buy(int playerId, String body) throws Exception {
        return mvc.perform(post("/api/v1/players/" + playerId + "/buy")
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private void verifyNothingSaved() {
        verify(contractRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void buyReproducesTheExampleOfTheSpec() throws Exception {
        buy(2, BUY_AGUIAS)
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id": 10, "club_id": 1, "player_id": 2, "player_name": "Meia Lua", "type": "COMPRA",
                         "value": 37.00, "fee": 1.85, "penalty": 0.00, "profit": null, "cash_change": -38.85,
                         "created_at": "2026-11-02T10:15:00-03:00",
                         "contract": {"purchase_value": 37.00, "loyalty_until": "2027-03-02T10:15:00-03:00",
                                      "status": "ATIVO"},
                         "club_cash": 261.15}
                        """, true));

        assertThat(aguias.getCash()).isEqualTo(Money.of("261.15"));
        assertThat(meiaLua.getClubId()).isEqualTo(1);
        assertThat(meiaLua.getUpdatedAt()).isEqualTo(PURCHASE_TIME);
        verify(contractRepository).save(any(Contract.class));
    }

    @Test
    void contractKeepsThePriceWithoutTheFee() throws Exception {
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract contract = invocation.getArgument(0);
            assertThat(contract.getPurchaseValue()).isEqualTo(Money.of("37.00"));
            assertThat(contract.getStatus()).isEqualTo(ContractStatus.ATIVO);
            assertThat(contract.getStartedAt()).isEqualTo(PURCHASE_TIME);
            assertThat(contract.getEndedAt()).isNull();
            return contract;
        });

        buy(2, BUY_AGUIAS).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
            "{\"club_id\": 1}",
            "{\"club_id\": null, \"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
            "{\"club_id\": 0, \"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
            "{\"club_id\": -1, \"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
            "{\"club_id\": \"1\", \"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
            "{\"club_id\": 1.5, \"loyalty_until\": \"2027-03-02T10:15-03:00\"}",
            "{\"club_id\": 1, \"loyalty_until\": \"2027-03-02T10:15\"}",
            "{\"club_id\": 1, \"loyalty_until\": \"2027-03-02T10:15:00-03:00\"}",
            "{\"club_id\": 1, \"loyalty_until\": \"2027-03-02T10:15Z\"}",
            "{\"club_id\": 1, \"loyalty_until\": \"2027-02-30T10:15-03:00\"}",
            "{\"club_id\": 1, \"loyalty_until\": \"2027-03-02T24:00-03:00\"}",
            "{\"club_id\": 1, \"loyalty_until\": \"2026-11-02T10:15-03:00\"}",  // o próprio momento da compra
            "{\"club_id\": 1, \"loyalty_until\": \"2026-11-01T10:15-03:00\"}",  // passado
            "{\"club_id\": 1, \"loyalty_until\": \"2029-11-01T10:16-03:00\"}",  // 1.095 dias e 1 minuto
            "{\"clubId\": 1, \"loyaltyUntil\": \"2027-03-02T10:15-03:00\"}",
            "[]"
    })
    void buyRejectsInvalidBodies(String body) throws Exception {
        buy(2, body)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
        verifyNothingSaved();
    }

    @Test
    void loyaltyCanEndExactly1095DaysAfterThePurchase() throws Exception {
        buy(2, "{\"club_id\": 1, \"loyalty_until\": \"2029-11-01T10:15-03:00\"}").andExpect(status().isOk());
    }

    @Test
    void unknownPlayerOrClubIsNotFound() throws Exception {
        buy(9, BUY_AGUIAS).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").isNotEmpty());
        buy(2, BUY_AGUIAS.replace("\"club_id\": 1", "\"club_id\": 7")).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/players/abc/buy").contentType(MediaType.APPLICATION_JSON).content(BUY_AGUIAS))
                .andExpect(status().isNotFound());
        verifyNothingSaved();
    }

    @Test
    void playerInAClubIsConflict() throws Exception {
        inClub(meiaLua, 5);

        buy(2, BUY_AGUIAS).andExpect(status().isConflict()).andExpect(jsonPath("$.message").isNotEmpty());
        verifyNothingSaved();
        assertThat(aguias.getCash()).isEqualTo(Money.of("300.00"));
    }

    @Test
    void clubCannotBuyBackAPlayerItSold() throws Exception {
        when(transactionRepository.existsByClubIdAndPlayerIdAndType(1, 2, TransactionType.VENDA)).thenReturn(true);

        buy(2, BUY_AGUIAS).andExpect(status().isConflict());
        verifyNothingSaved();
    }

    @Test
    void fullSquadIsConflict() throws Exception {
        when(playerRepository.countByClubId(1)).thenReturn(12L);

        buy(2, BUY_AGUIAS).andExpect(status().isConflict());
        verifyNothingSaved();
    }

    @Test
    void elevenPlayersStillHaveRoom() throws Exception {
        when(playerRepository.countByClubId(1)).thenReturn(11L);

        buy(2, BUY_AGUIAS).andExpect(status().isOk());
    }

    @Test
    void costMayEqualHalfTheCashButNotPassIt() throws Exception {
        // custo 38,85: limite exato com caixa 77,70; recusado com 77,69 (limite 38,845)
        withCash(aguias, "77.69");
        buy(2, BUY_AGUIAS)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("O custo da compra (38,85) passa do limite de 50% do caixa (38,845)."));
        verifyNothingSaved();
        assertThat(meiaLua.hasClub()).isFalse();

        withCash(aguias, "77.70");
        buy(2, BUY_AGUIAS)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.club_cash").value(38.85));
    }

    @Test
    void formatErrorsComeBeforeNotFound() throws Exception {
        buy(9, "{\"club_id\": 1, \"loyalty_until\": \"2026-11-01T10:15-03:00\"}").andExpect(status().isBadRequest());
    }
}
