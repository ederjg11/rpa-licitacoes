package com.rpa.licitacoes.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidade que representa uma licitação pública capturada pelo RPA.
 */
@Entity
@Table(name = "licitacoes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Licitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nome do Órgão Promotor */
    @Column(name = "nome_orgao_promotor", length = 500)
    private String nomeOrgaoPromotor;

    /** Data de publicação */
    @Column(name = "data_publicacao", length = 100)
    private String dataPublicacao;

    /** Modalidade da licitação */
    @Column(name = "modalidade", length = 200)
    private String modalidade;

    /** Objeto da licitação */
    @Column(name = "objeto", columnDefinition = "TEXT")
    private String objeto;

    /** Link/URL do Edital */
    @Column(name = "edital", length = 1000)
    private String edital;

    /** Número do Edital */
    @Column(name = "numero_edital", length = 200)
    private String numeroEdital;

    /** Número do Processo */
    @Column(name = "numero_processo", length = 200)
    private String numeroProcesso;

    /** Pregoeiro / Agente de Contratação */
    @Column(name = "pregoeiro_agente_contratacao", length = 300)
    private String pregoeiroAgenteContratacao;

    /** Término do recebimento de propostas */
    @Column(name = "termino_recebimento_propostas", length = 100)
    private String terminoRecebimentoPropostas;

    /** Modo de disputa */
    @Column(name = "modo_disputa", length = 200)
    private String modoDisputa;

    /** Início de Lances */
    @Column(name = "inicio_lances", length = 100)
    private String inicioLances;

    /** Portal de origem */
    @Column(name = "portal_origem", length = 200)
    private String portalOrigem;

    /** Data/hora de captura pelo RPA */
    @Column(name = "data_captura")
    private LocalDateTime dataCaptura;
}
