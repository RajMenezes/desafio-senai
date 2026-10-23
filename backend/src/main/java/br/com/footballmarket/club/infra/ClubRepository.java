package br.com.footballmarket.club.infra;

import br.com.footballmarket.club.domain.Club;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClubRepository extends JpaRepository<Club, Integer> {

    /**
     * {@code SELECT ... FOR UPDATE}: compras e vendas do mesmo clube esperam umas pelas outras,
     * para que caixa e limite de elenco sejam conferidos sobre o valor atual.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Club c where c.id = :id")
    Optional<Club> findByIdForUpdate(@Param("id") Integer id);

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
