package br.com.fiap.solin.service;

import br.com.fiap.solin.dto.request.EventoRequest;
import br.com.fiap.solin.dto.response.EventoResponse;
import br.com.fiap.solin.entity.Evento;
import br.com.fiap.solin.entity.Pet;
import br.com.fiap.solin.enums.TipoEvento;
import br.com.fiap.solin.exception.RecursoNaoEncontradoException;
import br.com.fiap.solin.mapper.EventoMapper;
import br.com.fiap.solin.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository repository;
    private final EventoMapper mapper;
    private final PetService petService;
    private final AlertaService alertaService;

    @Transactional
    @CacheEvict(value = "eventos", allEntries = true)
    public EventoResponse registrar(EventoRequest dto) {
        Pet pet = petService.buscarEntidade(dto.petId());
        Evento salvo = repository.save(mapper.toEntity(dto, pet));

        // Apos registrar um evento, reavalia as regras de alerta para o pet.
        // O Strategy decide quais alertas disparar.
        alertaService.avaliarRegrasParaPet(pet);

        return mapper.toResponse(salvo);
    }

    public Page<EventoResponse> listarPorPet(Long petId, TipoEvento tipo,
                                             LocalDateTime inicio, LocalDateTime fim,
                                             Pageable pageable) {
        return repository.buscarComFiltros(petId, tipo, inicio, fim, pageable)
                .map(mapper::toResponse);
    }

    public EventoResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    @CacheEvict(value = "eventos", allEntries = true)
    public void excluir(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Evento", id);
        }
        repository.deleteById(id);
    }

    private Evento buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Evento", id));
    }
}
