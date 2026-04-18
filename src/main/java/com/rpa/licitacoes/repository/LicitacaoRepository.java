package com.rpa.licitacoes.repository;

import com.rpa.licitacoes.model.Licitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para persistência de licitações.
 */
@Repository
public interface LicitacaoRepository extends JpaRepository<Licitacao, Long> {

    List<Licitacao> findByPortalOrigem(String portalOrigem);

    List<Licitacao> findByNumeroProcesso(String numeroProcesso);

    boolean existsByNumeroEditalAndPortalOrigem(String numeroEdital, String portalOrigem);
}
