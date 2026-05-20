package br.com.fiap.solin.repository;

import br.com.fiap.solin.entity.Evento;
import br.com.fiap.solin.enums.TipoEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    Page<Evento> findByPetId(Long petId, Pageable pageable);

    /**
     * Retorna o evento mais recente de um determinado tipo para um pet.
     * Usado pelas estrategias de alerta para verificar quanto tempo
     * faz desde a ultima ocorrencia (ex: ultima vez que urinou).
     *
     * Usa Query Method do Spring Data (sem JPQL): findFirstBy... ORDER BY ... DESC.
     * Funciona tanto em Oracle quanto em H2.
     */
    Optional<Evento> findFirstByPetIdAndTipoOrderByDataHoraDesc(Long petId, TipoEvento tipo);

    @Query("SELECT e FROM Evento e " +
           "WHERE e.pet.id = :petId " +
           "AND (:tipo IS NULL OR e.tipo = :tipo) " +
           "AND (:inicio IS NULL OR e.dataHora >= :inicio) " +
           "AND (:fim IS NULL OR e.dataHora <= :fim)")
    Page<Evento> buscarComFiltros(@Param("petId") Long petId,
                                  @Param("tipo") TipoEvento tipo,
                                  @Param("inicio") LocalDateTime inicio,
                                  @Param("fim") LocalDateTime fim,
                                  Pageable pageable);
}
