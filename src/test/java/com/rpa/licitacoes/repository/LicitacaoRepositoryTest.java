package com.rpa.licitacoes.repository;

import com.rpa.licitacoes.model.Licitacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LicitacaoRepositoryTest {

    @Autowired
    private LicitacaoRepository licitacaoRepository;

    @BeforeEach
    void setUp() {
        licitacaoRepository.deleteAll();
    }

    @Test
    void deveSalvarERecuperarLicitacao() {
        Licitacao licitacao = Licitacao.builder()
                .nomeOrgaoPromotor("Secretaria Municipal de Saúde")
                .numeroEdital("002/2025")
                .numeroProcesso("2025/002")
                .modalidade("Pregão Eletrônico")
                .portalOrigem("Jornal do Licitante")
                .dataCaptura(LocalDateTime.now())
                .build();

        Licitacao salva = licitacaoRepository.save(licitacao);

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getNomeOrgaoPromotor()).isEqualTo("Secretaria Municipal de Saúde");
    }

    @Test
    void deveFiltrarPorPortalOrigem() {
        licitacaoRepository.save(Licitacao.builder()
                .portalOrigem("Jornal do Licitante")
                .dataCaptura(LocalDateTime.now())
                .build());
        licitacaoRepository.save(Licitacao.builder()
                .portalOrigem("Outro Portal")
                .dataCaptura(LocalDateTime.now())
                .build());

        List<Licitacao> resultado = licitacaoRepository.findByPortalOrigem("Jornal do Licitante");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getPortalOrigem()).isEqualTo("Jornal do Licitante");
    }

    @Test
    void deveVerificarExistenciaPorNumeroEditalEPortal() {
        licitacaoRepository.save(Licitacao.builder()
                .numeroEdital("003/2025")
                .portalOrigem("Jornal do Licitante")
                .dataCaptura(LocalDateTime.now())
                .build());

        boolean existe = licitacaoRepository.existsByNumeroEditalAndPortalOrigem(
                "003/2025", "Jornal do Licitante");
        boolean naoExiste = licitacaoRepository.existsByNumeroEditalAndPortalOrigem(
                "999/2025", "Jornal do Licitante");

        assertThat(existe).isTrue();
        assertThat(naoExiste).isFalse();
    }
}
