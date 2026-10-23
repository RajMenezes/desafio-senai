package br.com.footballmarket.player.application;

import br.com.footballmarket.player.domain.Condition;
import br.com.footballmarket.player.domain.Player;
import br.com.footballmarket.player.infra.PlayerRepository;
import br.com.footballmarket.shared.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;

/**
 * Casos de uso do cadastro de jogadores. As regras ficam na entidade {@link Player};
 * aqui ficam a busca, a transação e o relógio.
 */
@Service
@Transactional
public class PlayerService {

    private final PlayerRepository repository;
    private final Clock clock;

    public PlayerService(PlayerRepository repository, Clock clock) {
        this.repository = repository;
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
