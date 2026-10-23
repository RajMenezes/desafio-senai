package br.com.footballmarket.club.application;

import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.domain.ClubSort;
import br.com.footballmarket.club.domain.ValuedClub;
import br.com.footballmarket.club.infra.ClubRepository;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.shared.error.BusinessRuleException;
import br.com.footballmarket.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Casos de uso de clubes. O elenco vem do módulo de jogadores, consultado pelo id do clube.
 */
@Service
@Transactional
public class ClubService {

    private final ClubRepository repository;
    private final PlayerService players;
    private final Clock clock;

    public ClubService(ClubRepository repository, PlayerService players, Clock clock) {
        this.repository = repository;
        this.players = players;
        this.clock = clock;
    }

    /**
     * RN10: formato (400) antes das regras que dependem do que já está gravado (409).
     */
    public ValuedClub create(CreateClubCommand command) {
        Club club = Club.create(command.name(), command.acronym(), OffsetDateTime.now(clock));
        repository.lockForCreation();
        if (repository.count() >= Club.MAX_CLUBS) {
            throw new BusinessRuleException("A liga já tem o máximo de " + Club.MAX_CLUBS + " clubes.");
        }
        if (repository.existsByNameKey(Club.nameKey(club.getName()))) {
            throw new BusinessRuleException("Já existe um clube com o nome '" + club.getName() + "'.");
        }
        if (repository.existsByAcronym(club.getAcronym())) {
            throw new BusinessRuleException("Já existe um clube com a sigla '" + club.getAcronym() + "'.");
        }
        return ValuedClub.of(repository.save(club), List.of());
    }

    @Transactional(readOnly = true)
    public List<ValuedClub> list(ClubSort sort) {
        Map<Integer, List<Player>> squads = players.squadsByClub();
        return repository.findAll().stream()
                .map(club -> ValuedClub.of(club, squads.getOrDefault(club.getId(), List.of())))
                .sorted(sort.ordering())
                .toList();
    }

    /**
     * Clube com o elenco na ordem da RN11.
     */
    @Transactional(readOnly = true)
    public ValuedClub findById(Integer id) {
        Club club = repository.findById(id).orElseThrow(() -> new NotFoundException("Clube não encontrado."));
        return ValuedClub.of(club, players.squad(club.getId()));
    }
}
