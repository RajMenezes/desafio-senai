package br.com.footballmarket.club.application;

import br.com.footballmarket.club.domain.Club;
import br.com.footballmarket.club.domain.LineupEntry;
import br.com.footballmarket.club.domain.LineupRules;
import br.com.footballmarket.club.infra.LineupRepository;
import br.com.footballmarket.player.application.PlayerService;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.domain.PlayerSort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Escalação do clube (RN19). Escalar não muda o caixa.
 */
@Service
@Transactional
public class LineupService {

    private final ClubService clubs;
    private final PlayerService players;
    private final LineupRepository lineups;
    private final Clock clock;

    public LineupService(ClubService clubs, PlayerService players, LineupRepository lineups, Clock clock) {
        this.clubs = clubs;
        this.players = players;
        this.lineups = lineups;
        this.clock = clock;
    }

    /**
     * Substitui a escalação inteira. Clube e jogadores inexistentes (404) antes das regras (409).
     * Os jogadores são travados antes do clube, a ordem da compra e da venda, para que nenhum deles
     * seja vendido ou fique DOENTE entre a conferência e a gravação.
     */
    public Lineup replace(Integer clubId, SetLineupCommand command) {
        clubs.requireExists(clubId);
        List<Player> starters = players.findAllForUpdate(command.playerIds());
        Club club = clubs.findForUpdate(clubId);
        LineupRules.requireValid(club, command.formation(), starters);

        OffsetDateTime now = OffsetDateTime.now(clock);
        lineups.deleteAllOfClub(clubId);
        lineups.saveAll(starters.stream().map(starter -> new LineupEntry(clubId, starter.getId(), now)).toList());
        club.changeFormation(command.formation());
        return new Lineup(clubId, club.getFormation(), sortedAsSquad(starters));
    }

    @Transactional(readOnly = true)
    public Lineup find(Integer clubId) {
        Club club = clubs.findClub(clubId);
        List<Integer> starterIds = lineups.findByClubId(clubId).stream().map(LineupEntry::getPlayerId).toList();
        return new Lineup(clubId, club.getFormation(), sortedAsSquad(players.findAllById(starterIds)));
    }

    /**
     * RN19, item 5: o titular vendido ou DOENTE sai da escalação; a formação continua a mesma.
     * Sem efeito para quem não é titular.
     */
    public void removeStarter(Integer playerId) {
        lineups.deleteByPlayer(playerId);
    }

    private static List<Player> sortedAsSquad(List<Player> starters) {
        return starters.stream().sorted(PlayerSort.squadOrdering()).toList();
    }
}
