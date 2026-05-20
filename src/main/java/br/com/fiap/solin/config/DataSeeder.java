package br.com.fiap.solin.config;

import br.com.fiap.solin.entity.Especie;
import br.com.fiap.solin.repository.EspecieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Popula o banco com algumas especies basicas quando a aplicacao
 * sobe no perfil "dev". Facilita testar a API sem precisar criar
 * tudo na mao.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EspecieRepository especieRepository;

    @Override
    public void run(String... args) {
        if (especieRepository.count() > 0) {
            return;
        }

        especieRepository.save(Especie.builder()
                .nome("Cao")
                .horasMaximasSemUrinar(8)
                .build());

        especieRepository.save(Especie.builder()
                .nome("Gato")
                .horasMaximasSemUrinar(24)
                .build());

        especieRepository.save(Especie.builder()
                .nome("Coelho")
                .horasMaximasSemUrinar(12)
                .build());
    }
}
