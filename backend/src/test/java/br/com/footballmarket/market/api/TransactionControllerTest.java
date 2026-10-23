package br.com.footballmarket.market.api;

import br.com.footballmarket.FixedClockConfig;
import br.com.footballmarket.club.application.ClubService;
import br.com.footballmarket.club.application.SquadContracts;
import br.com.footballmarket.club.infra.ClubRepository;
import br.com.footballmarket.market.application.TransactionHistoryService;
import br.com.footballmarket.market.domain.MarketTransaction;
import br.com.footballmarket.market.domain.TransactionType;
import br.com.footballmarket.market.infra.TransactionRepository;
import br.com.footballmarket.player.application.ClubDirectory;
import br.com.footballmarket.player.application.PlayerService;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static br.com.footballmarket.market.domain.TransactionFixtures.PURCHASE_TIME;
import static br.com.footballmarket.market.domain.TransactionFixtures.purchase;
import static br.com.footballmarket.market.domain.TransactionFixtures.sale;
import static br.com.footballmarket.player.domain.PlayerFixtures.meiaLua;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Histórico e exportação com HTTP, serviços e domínio reais; os repositórios são simulados
 * (os filtros no banco são conferidos em {@code MarketApiIntegrationTest}).
 */
@WebMvcTest(TransactionController.class)
@Import({TransactionHistoryService.class, ClubService.class, PlayerService.class, JacksonConfig.class,
        FixedClockConfig.class})
class TransactionControllerTest {

    private static final Set<TransactionType> ALL = EnumSet.allOf(TransactionType.class);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TransactionRepository transactionRepository;

    @MockitoBean
    private ClubRepository clubRepository;

    @MockitoBean
    private PlayerRepository playerRepository;

    @MockitoBean
    private ClubDirectory clubDirectory;

    @MockitoBean
    private SquadContracts squadContracts;

    @BeforeEach
    void givenTheExampleClub() {
        when(clubRepository.existsById(1)).thenReturn(true);
        when(transactionRepository.findHistory(1, ALL)).thenReturn(List.of(sale(), purchase()));
        when(transactionRepository.findHistory(1, EnumSet.of(TransactionType.VENDA))).thenReturn(List.of(sale()));
    }

    @Test
    void historyFilteredBySaleReproducesTheExample() throws Exception {
        mvc.perform(get("/api/v1/clubs/1/transactions").param("type", "VENDA"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"data": [{"id": 11, "club_id": 1, "player_id": 2, "player_name": "Meia Lua", "type": "VENDA",
                                   "value": 40.00, "fee": 0.30, "penalty": 4.00, "profit": 3.00, "cash_change": 35.70,
                                   "created_at": "2026-11-05T16:40:00-03:00"}],
                         "summary": {"total_spent": 0.00, "total_received": 35.70, "total_fees": 0.30,
                                     "total_penalties": 4.00, "total_profit": 3.00}}
                        """, true));
    }

    @Test
    void historyWithoutFilterBringsBothAndTheirTotals() throws Exception {
        mvc.perform(get("/api/v1/clubs/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].type").value("VENDA"))
                .andExpect(jsonPath("$.data[1].type").value("COMPRA"))
                .andExpect(jsonPath("$.data[1].profit").isEmpty())
                .andExpect(jsonPath("$.data[1].cash_change").value(-38.85))
                .andExpect(jsonPath("$.summary.total_spent").value(38.85))
                .andExpect(jsonPath("$.summary.total_received").value(35.70))
                .andExpect(jsonPath("$.summary.total_fees").value(2.15))
                .andExpect(jsonPath("$.summary.total_penalties").value(4.00))
                .andExpect(jsonPath("$.summary.total_profit").value(3.00));
    }

    @Test
    void playerFilterOfAPlayerWithoutTransactionsIsEmptyWithZeros() throws Exception {
        when(playerRepository.findByIdAndDeletedAtIsNull(5)).thenReturn(Optional.of(PlayerFixtures.withId(meiaLua(), 5)));

        mvc.perform(get("/api/v1/clubs/1/transactions").param("player_id", "5"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"data": [], "summary": {"total_spent": 0.00, "total_received": 0.00, "total_fees": 0.00,
                                                 "total_penalties": 0.00, "total_profit": 0.00}}
                        """, true));
        verify(transactionRepository).findHistoryOfPlayer(1, ALL, 5);
    }

    @Test
    void unknownOrDeletedFilterPlayerIsNotFound() throws Exception {
        mvc.perform(get("/api/v1/clubs/1/transactions").param("player_id", "9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
        mvc.perform(get("/api/v1/clubs/1/transactions/export").param("player_id", "9"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void unknownClubIsNotFound() throws Exception {
        mvc.perform(get("/api/v1/clubs/7/transactions")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/clubs/0/transactions")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/clubs/7/transactions/export").accept(MediaType.parseMediaType("text/csv")))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"type=", "type=venda", "type=COMPRA,COMPRA", "type=COMPRA,%20VENDA", "type=TROCA",
            "player_id=0", "player_id=01", "player_id=abc", "player_id="})
    void invalidFiltersAreBadRequestEvenForTheExport(String queryString) throws Exception {
        for (String path : List.of("/api/v1/clubs/1/transactions?", "/api/v1/clubs/1/transactions/export?")) {
            mvc.perform(get(URI.create(path + queryString)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").isNotEmpty());
        }
        verify(transactionRepository, never()).findHistory(anyInt(), any());
    }

    @Test
    void filterFormatComesBeforeUnknownClub() throws Exception {
        mvc.perform(get("/api/v1/clubs/7/transactions").param("type", "venda")).andExpect(status().isBadRequest());
    }

    @Test
    void exportReproducesTheExampleAsUtf8Csv() throws Exception {
        when(transactionRepository.findHistory(1, ALL)).thenReturn(List.of(sale(), purchase()));

        byte[] body = mvc.perform(get("/api/v1/clubs/1/transactions/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"transacoes-clube-1.csv\""))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(new String(body, StandardCharsets.UTF_8)).isEqualTo("""
                data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa
                2026-11-05T16:40:00-03:00;VENDA;Meia Lua;40,00;0,30;4,00;3,00;35,70
                2026-11-02T10:15:00-03:00;COMPRA;Meia Lua;37,00;1,85;0,00;;-38,85
                """);
        assertThat(body[0]).isEqualTo((byte) 'd'); // sem BOM
    }

    @Test
    void exportKeepsAccentsInUtf8() throws Exception {
        when(transactionRepository.findHistory(1, EnumSet.of(TransactionType.COMPRA))).thenReturn(List.of(
                MarketTransaction.purchase(1, 3, "Zé \"Foguete\" Lima", Money.of("28.00"), Money.ZERO, PURCHASE_TIME)));

        byte[] body = mvc.perform(get("/api/v1/clubs/1/transactions/export").param("type", "COMPRA"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(new String(body, StandardCharsets.UTF_8))
                .endsWith("2026-11-02T10:15:00-03:00;COMPRA;\"Zé \"\"Foguete\"\" Lima\";28,00;0,00;0,00;;-28,00\n");
    }

    @Test
    void exportWithoutTransactionsHasOnlyTheHeader() throws Exception {
        when(clubRepository.existsById(2)).thenReturn(true);

        mvc.perform(get("/api/v1/clubs/2/transactions/export"))
                .andExpect(status().isOk())
                .andExpect(content().string("data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa\n"));
    }
}
