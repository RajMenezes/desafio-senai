package br.com.footballmarket.player.application;

import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.PlayerSort;
import br.com.footballmarket.player.infra.PlayerRepository;
import br.com.footballmarket.shared.error.NotFoundException;
import br.com.footballmarket.shared.money.Money;
import br.com.footballmarket.shared.pagination.PageResult;
import br.com.footballmarket.shared.text.TextNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Casos de uso do cadastro de jogadores. As regras ficam na entidade {@link Player};
 * aqui ficam a busca, a transação e o relógio.
 */
@Service
@Transactional
public class PlayerService {

    private final PlayerRepository repository;
    private final ClubDirectory clubs;
    private final Clock clock;

    public PlayerService(PlayerRepository repository, ClubDirectory clubs, Clock clock) {
        this.repository = repository;
        this.clubs = clubs;
        this.clock = clock;
    }

    public Player register(RegisterPlayerCommand command) {
        Player player = Player.register(command.name(), command.position(), command.agility(), command.age(),
                command.attributes(), command.potential(), now());
        return repository.save(player);
    }

    @Transactional(readOnly = true)
    public Player findById(Integer id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Jogador não encontrado."));
    }

    /**
     * RN07: o banco aplica os filtros de colunas gravadas; a busca sem acentos, a faixa de valor de mercado
     * (calculado, RN05) e a ordenação ficam aqui, e a paginação vem por último.
     */
    @Transactional(readOnly = true)
    public PageResult<Player> list(PlayerQuery query) {
        if (query.clubId() != null && !clubs.exists(query.clubId())) {
            throw new NotFoundException("Clube não encontrado.");
        }
        String search = query.search() == null ? null : TextNormalizer.fold(query.search());
        List<Player> matching = repository.findAll(PlayerSpecifications.matching(query)).stream()
                .filter(player -> search == null || TextNormalizer.fold(player.getName()).contains(search))
                .filter(player -> withinValueRange(player.marketValue(), query))
                .sorted(PlayerSort.ordering(query.sort()))
                .toList();
        return PageResult.slice(matching, query.page(), query.limit());
    }

    /**
     * Elenco do clube na ordem da RN11 (posição, nome e id).
     */
    @Transactional(readOnly = true)
    public List<Player> squad(Integer clubId) {
        return repository.findByClubId(clubId).stream().sorted(PlayerSort.squadOrdering()).toList();
    }

    /**
     * Elencos de todos os clubes de uma vez, para a listagem de clubes não fazer uma consulta por clube.
     */
    @Transactional(readOnly = true)
    public Map<Integer, List<Player>> squadsByClub() {
        return repository.findByClubIdIsNotNull().stream().collect(Collectors.groupingBy(Player::getClubId));
    }

    private static boolean withinValueRange(Money marketValue, PlayerQuery query) {
        return (query.minValue() == null || !marketValue.isLessThan(query.minValue()))
                && (query.maxValue() == null || !marketValue.isGreaterThan(query.maxValue()));
    }

    public Player update(Integer id, UpdatePlayerCommand command) {
        Player player = findById(id);
        player.updateProfile(command.name(), command.position(), command.agility(), command.age(), now());
        return player;
    }

    public Player changeCondition(Integer id, Condition condition) {
        Player player = findById(id);
        player.changeCondition(condition, now());
        // Etapa 8: titular que passa a DOENTE sai da escalação (RN19)
        return player;
    }

    public void delete(Integer id) {
        findById(id).delete(now());
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }
}
