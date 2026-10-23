package br.com.footballmarket.club.application;

import br.com.footballmarket.club.infra.ClubRepository;
import br.com.footballmarket.player.application.ClubDirectory;
import org.springframework.stereotype.Component;

/**
 * Responde ao módulo de jogadores se um clube existe (ex.: filtro {@code club_id} de {@code GET /players}).
 */
@Component
class ClubDirectoryAdapter implements ClubDirectory {

    private final ClubRepository repository;

    ClubDirectoryAdapter(ClubRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean exists(Integer clubId) {
        return repository.existsById(clubId);
    }
}
