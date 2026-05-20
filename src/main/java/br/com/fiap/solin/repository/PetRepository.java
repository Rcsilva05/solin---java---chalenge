package br.com.fiap.solin.repository;

import br.com.fiap.solin.entity.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PetRepository extends JpaRepository<Pet, Long> {

    Page<Pet> findByTutorId(Long tutorId, Pageable pageable);

    Page<Pet> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("SELECT p FROM Pet p " +
           "WHERE (:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
           "AND (:especieId IS NULL OR p.especie.id = :especieId) " +
           "AND (:tutorId IS NULL OR p.tutor.id = :tutorId)")
    Page<Pet> buscarComFiltros(@Param("nome") String nome,
                               @Param("especieId") Long especieId,
                               @Param("tutorId") Long tutorId,
                               Pageable pageable);
}
