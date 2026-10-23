package br.com.footballmarket.club.infra;

import br.com.footballmarket.club.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ClubRepository extends JpaRepository<Club, Integer> {

    boolean existsByNameKey(String nameKey);

    boolean existsByAcronym(String acronym);

    /**
     * Serializa as criações de clube até o fim da transação, para que duas criações simultâneas não
     * passem juntas do limite de 20 clubes. Leituras continuam liberadas. Nome e sigla repetidos
     * também são barrados pelas restrições UNIQUE do banco.
     */
    @Modifying
    @Query(value = "LOCK TABLE clubes IN SHARE ROW EXCLUSIVE MODE", nativeQuery = true)
    void lockForCreation();
}
