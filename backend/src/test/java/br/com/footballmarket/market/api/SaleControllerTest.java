package br.com.footballmarket.market.api;

import br.com.footballmarket.FixedClockConfig;
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
import br.com.footballmarket.market.infra.ContractRepository;
import br.com.footballmarket.market.infra.TransactionRepository;
import br.com.footballmarket.player.application.ClubDirectory;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.application.StarterRemoval;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.PlayerFixtures;
import br.com.footballmarket.player.infra.PlayerRepository;
import br.com.footballmarket.shared.config.JacksonConfig;
import br.com.footballmarket.shared.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static br.com.footballmarket.club.domain.ClubFixtures.aguias;
import static br.com.footballmarket.club.domain.ClubFixtures.withCash;
import static br.com.footballmarket.club.domain.ClubFixtures.withId;
import static br.com.footballmarket.player.domain.PlayerFixtures.NOW;
import static br.com.footballmarket.player.domain.PlayerFixtures.inClub;
import static br.com.footballmarket.player.domain.PlayerFixtures.meiaLua;
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
 * Venda com HTTP, serviços e domínio reais; só os repositórios são simulados.
 * O relógio está no momento do exemplo 2.3.2 (05/11/2026, 16h40).
 */
@WebMvcTest(MarketController.class)
@Import({PurchaseService.class, SaleService.class, PlayerService.class, ClubService.class, JacksonConfig.class,
        FixedClockConfig.class})
class SaleControllerTest {

    private static final OffsetDateTime PURCHASE_TIME = OffsetDateTime.of(2026, 11, 2, 10, 15, 0, 0, ZoneOffset.ofHours(-3));
    private static final OffsetDateTime LOYALTY_UNTIL = OffsetDateTime.of(2027, 3, 2, 10, 15, 0, 0, ZoneOffset.ofHours(-3));

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
    private Contract contract;

    @BeforeEach
    void givenTheExampleScenario() {
        // Meia Lua de volta a APTO (40,00) no Águias FC, com caixa 261,15 e o contrato da compra 2.3.1
        meiaLua = PlayerFixtures.withId(inClub(meiaLua(), 1), 2);
        aguias = withCash(withId(aguias(), 1), "261.15");
        contract = Contract.start(2, 1, Money.of("37.00"), LOYALTY_UNTIL, PURCHASE_TIME);
        when(playerRepository.findActiveForUpdate(2)).thenReturn(Optional.of(meiaLua));
        when(clubRepository.findByIdForUpdate(1)).thenReturn(Optional.of(aguias));
        when(contractRepository.findByPlayerIdAndStatus(2, ContractStatus.ATIVO)).thenReturn(Optional.of(contract));
        when(transactionRepository.save(any(MarketTransaction.class))).thenAnswer(invocation -> {
            MarketTransaction transaction = invocation.getArgument(0);
            ReflectionTestUtils.setField(transaction, "id", 11);
            return transaction;
        });
    }

    @Test
    void sellReproducesTheExampleOfTheSpec() throws Exception {
        mvc.perform(post("/api/v1/players/2/sell"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"id": 11, "club_id": 1, "player_id": 2, "player_name": "Meia Lua", "type": "VENDA",
                         "value": 40.00, "fee": 0.30, "penalty": 4.00, "profit": 3.00, "cash_change": 35.70,
                         "created_at": "2026-11-05T16:40:00-03:00",
                         "purchase_value": 37.00, "remaining_days": 117, "penalty_rate": 10,
                         "contract_status": "RESCINDIDO", "club_cash": 296.85}
                        """, true));

        assertThat(aguias.getCash()).isEqualTo(Money.of("296.85"));
        assertThat(meiaLua.hasClub()).isFalse();
        assertThat(meiaLua.getUpdatedAt()).isEqualTo(NOW);
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.RESCINDIDO);
        assertThat(contract.getEndedAt()).isEqualTo(NOW);
        verify(lineupService).removeStarter(2);
    }

    @Test
    void fulfilledLoyaltyEndsTheContractWithoutPenalty() throws Exception {
        ReflectionTestUtils.setField(contract, "loyaltyUntil", NOW);

        mvc.perform(post("/api/v1/players/2/sell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining_days").value(0))
                .andExpect(jsonPath("$.penalty_rate").value(0))
                .andExpect(jsonPath("$.penalty").value(0.00))
                .andExpect(jsonPath("$.cash_change").value(39.70))
                .andExpect(jsonPath("$.contract_status").value("ENCERRADO"));
    }

    @Test
    void lossHasNoCommissionAndANegativeProfit() throws Exception {
        ReflectionTestUtils.setField(contract, "purchaseValue", Money.of("50.00"));

        mvc.perform(post("/api/v1/players/2/sell"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profit").value(-10.00))
                .andExpect(jsonPath("$.fee").value(0.00))
                .andExpect(jsonPath("$.cash_change").value(36.00));
    }

    @Test
    void bodyIsIgnored() throws Exception {
        mvc.perform(post("/api/v1/players/2/sell").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.club_id").value(1));
    }

    @Test
    void freePlayerCannotBeSold() throws Exception {
        ReflectionTestUtils.setField(meiaLua, "clubId", null);

        mvc.perform(post("/api/v1/players/2/sell"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").isNotEmpty());
        verify(transactionRepository, never()).save(any());
        assertThat(aguias.getCash()).isEqualTo(Money.of("261.15"));
        verify(lineupService, never()).removeStarter(any());
    }

    @Test
    void unknownOrDeletedPlayerIsNotFound() throws Exception {
        mvc.perform(post("/api/v1/players/9/sell"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
        mvc.perform(post("/api/v1/players/0/sell")).andExpect(status().isNotFound());
        verify(transactionRepository, never()).save(any());
    }
}
