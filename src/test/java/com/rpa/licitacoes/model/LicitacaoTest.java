package com.rpa.licitacoes.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class LicitacaoTest {

    @Test
    void deveCriarLicitacaoComBuilder() {
        LocalDateTime agora = LocalDateTime.now();

        Licitacao licitacao = Licitacao.builder()
                .nomeOrgaoPromotor("Prefeitura Municipal de São Paulo")
                .dataPublicacao("01/04/2025")
                .modalidade("Pregão Eletrônico")
                .objeto("Aquisição de materiais de escritório")
                .edital("https://portal.example.com/edital/123.pdf")
                .numeroEdital("001/2025")
                .numeroProcesso("2025/001")
                .pregoeiroAgenteContratacao("João da Silva")
                .terminoRecebimentoPropostas("30/04/2025 10:00")
                .modoDisputa("Aberto")
                .inicioLances("30/04/2025 10:00")
                .portalOrigem("Jornal do Licitante")
                .dataCaptura(agora)
                .build();

        assertThat(licitacao.getNomeOrgaoPromotor()).isEqualTo("Prefeitura Municipal de São Paulo");
        assertThat(licitacao.getDataPublicacao()).isEqualTo("01/04/2025");
        assertThat(licitacao.getModalidade()).isEqualTo("Pregão Eletrônico");
        assertThat(licitacao.getObjeto()).isEqualTo("Aquisição de materiais de escritório");
        assertThat(licitacao.getEdital()).isEqualTo("https://portal.example.com/edital/123.pdf");
        assertThat(licitacao.getNumeroEdital()).isEqualTo("001/2025");
        assertThat(licitacao.getNumeroProcesso()).isEqualTo("2025/001");
        assertThat(licitacao.getPregoeiroAgenteContratacao()).isEqualTo("João da Silva");
        assertThat(licitacao.getTerminoRecebimentoPropostas()).isEqualTo("30/04/2025 10:00");
        assertThat(licitacao.getModoDisputa()).isEqualTo("Aberto");
        assertThat(licitacao.getInicioLances()).isEqualTo("30/04/2025 10:00");
        assertThat(licitacao.getPortalOrigem()).isEqualTo("Jornal do Licitante");
        assertThat(licitacao.getDataCaptura()).isEqualTo(agora);
    }

    @Test
    void deveCriarLicitacaoVazia() {
        Licitacao licitacao = new Licitacao();
        assertThat(licitacao).isNotNull();
        assertThat(licitacao.getId()).isNull();
        assertThat(licitacao.getNomeOrgaoPromotor()).isNull();
    }
}
