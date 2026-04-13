package com.rpa.licitacoes.controller;

import com.rpa.licitacoes.model.Licitacao;
import com.rpa.licitacoes.service.JornalLicitanteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para o portal Jornal do Licitante.
 * Expõe endpoints para disparar o RPA e consultar licitações capturadas.
 *
 * <p>Padrão MVC:
 * <ul>
 *   <li>Model  – {@link Licitacao}</li>
 *   <li>View   – JSON via Spring MVC (@RestController)</li>
 *   <li>Controller – esta classe</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/portais/jornal-licitante")
@RequiredArgsConstructor
public class JornalLicitanteController {

    private final JornalLicitanteService jornalLicitanteService;

    /**
     * Dispara a captura de licitações no portal Jornal do Licitante.
     * O RPA navega até o portal, carrega todos os registros (via "Exibir Mais")
     * e extrai os dados mapeados.
     *
     * @return lista de licitações capturadas
     */
    @PostMapping("/capturar")
    public ResponseEntity<List<Licitacao>> capturarLicitacoes() {
        log.info("Requisição recebida para capturar licitações - Jornal do Licitante");
        List<Licitacao> licitacoes = jornalLicitanteService.capturarLicitacoes();
        log.info("Captura concluída. {} licitações retornadas.", licitacoes.size());
        return ResponseEntity.ok(licitacoes);
    }

    /**
     * Lista todas as licitações do portal Jornal do Licitante salvas no banco.
     *
     * @return lista de licitações salvas
     */
    @GetMapping
    public ResponseEntity<List<Licitacao>> listarLicitacoes() {
        List<Licitacao> licitacoes = jornalLicitanteService.listarLicitacoesSalvas();
        return ResponseEntity.ok(licitacoes);
    }

    /**
     * Lista todas as licitações de todos os portais salvas no banco.
     *
     * @return lista completa de licitações
     */
    @GetMapping("/todas")
    public ResponseEntity<List<Licitacao>> listarTodasLicitacoes() {
        List<Licitacao> licitacoes = jornalLicitanteService.listarTodasLicitacoes();
        return ResponseEntity.ok(licitacoes);
    }
}
