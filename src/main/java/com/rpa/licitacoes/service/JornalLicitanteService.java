package com.rpa.licitacoes.service;

import com.rpa.licitacoes.model.Licitacao;
import com.rpa.licitacoes.repository.LicitacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serviço responsável por capturar licitações do portal Jornal do Licitante.
 * Utiliza Selenium WebDriver para interagir com a página JavaScript e
 * Jsoup para parsing do HTML resultante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JornalLicitanteService {

    private static final String PORTAL_URL = "https://jornaldolicitante.com.br/?take=9&skip=0#";
    private static final String PORTAL_NOME = "Jornal do Licitante";

    private final LicitacaoRepository licitacaoRepository;
    private final BeanFactory beanFactory;

    @Value("${rpa.jornal.max.cliques.exibir.mais:10}")
    private int maxCliquesExibirMais;

    @Value("${rpa.jornal.salvar.banco:true}")
    private boolean salvarNoBanco;

    /**
     * Executa o processo de captura de licitações no portal Jornal do Licitante.
     * Navega pela página, clica em "Exibir Mais" para carregar todos os registros,
     * abre cada card de licitação para capturar os detalhes e retorna a lista.
     *
     * @return lista de licitações capturadas
     */
    public List<Licitacao> capturarLicitacoes() {
        log.info("Iniciando captura de licitações no portal: {}", PORTAL_NOME);
        List<Licitacao> licitacoes = new ArrayList<>();
        WebDriver driver = null;

        try {
            driver = beanFactory.getBean(WebDriver.class);
            driver.get(PORTAL_URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Aguardar o carregamento inicial do conteúdo
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.divthree")));
            log.info("Página carregada. Iniciando cliques em 'Exibir Mais'...");

            // Clicar em "Exibir Mais" repetidamente para carregar todos os registros
            clicarExibirMais(driver, wait);

            // Obter o HTML completo após carregamento
            String htmlCompleto = driver.getPageSource();
            Document documento = Jsoup.parse(htmlCompleto);

            // Selecionar todos os cards de licitação dentro de div.divthree
            Elements cards = documento.select("div.divthree");
            log.info("Encontrados {} cards de licitação.", cards.size());

            for (Element card : cards) {
                try {
                    Licitacao licitacao = extrairDadosDoCard(card);
                    licitacoes.add(licitacao);
                } catch (Exception e) {
                    log.warn("Erro ao extrair dados de um card: {}", e.getMessage());
                }
            }

            // Abrir cada licitação (clicar em "Exibir Mais" por card) para detalhar
            licitacoes = capturarDetalhes(driver, wait, licitacoes);

            if (salvarNoBanco && !licitacoes.isEmpty()) {
                List<Licitacao> salvas = licitacaoRepository.saveAll(licitacoes);
                log.info("{} licitações salvas no banco de dados.", salvas.size());
                return salvas;
            }

        } catch (Exception e) {
            log.error("Erro durante a captura de licitações: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na captura de licitações do portal " + PORTAL_NOME, e);
        } finally {
            if (driver != null) {
                driver.quit();
                log.info("WebDriver encerrado.");
            }
        }

        log.info("Captura concluída. Total: {} licitações.", licitacoes.size());
        return licitacoes;
    }

    /**
     * Clica repetidamente no botão "Exibir Mais" até que ele desapareça
     * ou o limite de cliques seja atingido.
     */
    private void clicarExibirMais(WebDriver driver, WebDriverWait wait) {
        int cliques = 0;
        while (cliques < maxCliquesExibirMais) {
            try {
                List<WebElement> botoes = driver.findElements(
                        By.xpath("//button[normalize-space(text())='Exibir Mais']"));

                if (botoes.isEmpty()) {
                    log.info("Botão 'Exibir Mais' não encontrado. Todos os registros carregados.");
                    break;
                }

                WebElement botao = botoes.get(0);
                if (!botao.isDisplayed() || !botao.isEnabled()) {
                    log.info("Botão 'Exibir Mais' não está visível/habilitado.");
                    break;
                }

                int cardCountBefore = driver.findElements(By.cssSelector("div.divthree")).size();

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", botao);
                botao.click();
                cliques++;
                log.info("Clique {} em 'Exibir Mais' realizado.", cliques);

                // Aguardar novos cards serem carregados detectando mudança na contagem
                try {
                    wait.until(d -> d.findElements(By.cssSelector("div.divthree")).size() > cardCountBefore);
                } catch (Exception e) {
                    log.debug("Timeout aguardando novos cards após clique {}: {}", cliques, e.getMessage());
                }

            } catch (Exception e) {
                log.info("Botão 'Exibir Mais' não disponível após {} cliques: {}", cliques, e.getMessage());
                break;
            }
        }
        log.info("Total de cliques em 'Exibir Mais': {}", cliques);
    }

    /**
     * Captura os detalhes de cada licitação abrindo o modal/painel de detalhes.
     * Para cada card, clica no botão de detalhes e extrai os campos adicionais.
     */
    private List<Licitacao> capturarDetalhes(WebDriver driver, WebDriverWait wait,
                                              List<Licitacao> licitacoes) {
        // Obter todos os botões "Exibir Mais" individuais dos cards
        List<WebElement> botoesDetalhe = driver.findElements(
                By.xpath("//div[contains(@class,'divthree')]//button[normalize-space(text())='Exibir Mais']"));

        log.info("Iniciando captura de detalhes para {} licitações.", botoesDetalhe.size());

        List<Licitacao> licitacoesDetalhadas = new ArrayList<>();

        for (int i = 0; i < botoesDetalhe.size(); i++) {
            try {
                // Re-obter os botões a cada iteração (DOM pode ter sido atualizado)
                List<WebElement> botoes = driver.findElements(
                        By.xpath("//div[contains(@class,'divthree')]//button[normalize-space(text())='Exibir Mais']"));

                if (i >= botoes.size()) {
                    break;
                }

                WebElement botao = botoes.get(i);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", botao);
                wait.until(ExpectedConditions.elementToBeClickable(botao));
                botao.click();

                // Aguardar o painel de detalhes abrir verificando mudança no DOM
                try {
                    wait.until(ExpectedConditions.presenceOfElementLocated(
                            By.xpath("//div[contains(@class,'divthree')][" + (i + 1) + "]")));
                } catch (Exception ignored) {
                    // ignore timeout; proceed with current HTML
                }

                String html = driver.getPageSource();
                Document doc = Jsoup.parse(html);

                // Tentar extrair o detalhe do card aberto (índice i)
                Elements cards = doc.select("div.divthree");
                if (i < cards.size()) {
                    Licitacao detalhada = extrairDetalhesCompletos(cards.get(i));
                    licitacoesDetalhadas.add(detalhada);
                }

                // Fechar o painel se houver botão de fechar
                try {
                    List<WebElement> fechar = driver.findElements(By.xpath(
                            "//button[normalize-space(text())='Fechar' or @aria-label='Fechar' or @aria-label='Close']"));
                    if (!fechar.isEmpty()) {
                        fechar.get(0).click();
                        wait.until(ExpectedConditions.invisibilityOf(fechar.get(0)));
                    }
                } catch (Exception ignored) {
                    // ignore
                }

            } catch (Exception e) {
                log.warn("Erro ao capturar detalhes da licitação {}: {}", i, e.getMessage());
                // Adicionar licitação sem detalhes se disponível
                if (i < licitacoes.size()) {
                    licitacoesDetalhadas.add(licitacoes.get(i));
                }
            }
        }

        // Se não conseguiu detalhes, retornar a lista original
        return licitacoesDetalhadas.isEmpty() ? licitacoes : licitacoesDetalhadas;
    }

    /**
     * Extrai os dados básicos visíveis de um card de licitação.
     */
    private Licitacao extrairDadosDoCard(Element card) {
        return Licitacao.builder()
                .nomeOrgaoPromotor(extrairTexto(card, "nome-orgao, .orgao, .organ, h3, h4, strong"))
                .dataPublicacao(extrairTexto(card, ".data-publicacao, .data, .date, [class*='data']"))
                .modalidade(extrairTexto(card, ".modalidade, [class*='modalidade']"))
                .objeto(extrairTexto(card, ".objeto, [class*='objeto'], p"))
                .portalOrigem(PORTAL_NOME)
                .dataCaptura(LocalDateTime.now())
                .build();
    }

    /**
     * Extrai os detalhes completos de um card de licitação após abertura do painel.
     * Mapeia os rótulos de campo para os atributos da entidade Licitação.
     */
    private Licitacao extrairDetalhesCompletos(Element card) {
        Licitacao.LicitacaoBuilder builder = Licitacao.builder()
                .portalOrigem(PORTAL_NOME)
                .dataCaptura(LocalDateTime.now());

        // Mapear campos por rótulo (label: valor)
        Elements linhas = card.select("tr, li, div[class*='field'], div[class*='row'], p");

        for (Element linha : linhas) {
            String textoCompleto = linha.text().trim();

            mapearCampo(builder, textoCompleto, linha);
        }

        // Fallback: extrair texto direto de elementos específicos
        extrairCamposEspecificos(builder, card);

        return builder.build();
    }

    /**
     * Mapeia o texto de uma linha para o campo correto do builder.
     */
    private void mapearCampo(Licitacao.LicitacaoBuilder builder, String texto, Element elemento) {
        String lower = texto.toLowerCase();

        if (lower.contains("órgão promotor") || lower.contains("orgao promotor")) {
            builder.nomeOrgaoPromotor(extrairValorAposRotulo(texto, ":|–|-"));
        } else if (lower.contains("data") && (lower.contains("publicação") || lower.contains("publicacao"))) {
            builder.dataPublicacao(extrairValorAposRotulo(texto, ":|–|-"));
        } else if (lower.contains("modalidade")) {
            builder.modalidade(extrairValorAposRotulo(texto, ":|–|-"));
        } else if (lower.contains("objeto")) {
            builder.objeto(extrairValorAposRotulo(texto, ":|–|-"));
        } else if ((lower.contains("número") || lower.contains("numero"))
                && lower.contains("edital")) {
            builder.numeroEdital(extrairValorAposRotulo(texto, ":|–|-"));
        } else if ((lower.contains("número") || lower.contains("numero"))
                && lower.contains("processo")) {
            builder.numeroProcesso(extrairValorAposRotulo(texto, ":|–|-"));
        } else if (lower.contains("pregoeiro") || lower.contains("agente de contratação")
                || lower.contains("agente de contratacao")) {
            builder.pregoeiroAgenteContratacao(extrairValorAposRotulo(texto, ":|–|-"));
        } else if ((lower.contains("término") || lower.contains("termino")) && lower.contains("proposta")) {
            builder.terminoRecebimentoPropostas(extrairValorAposRotulo(texto, ":|–|-"));
        } else if (lower.contains("modo de disputa") || lower.contains("modo disputa")) {
            builder.modoDisputa(extrairValorAposRotulo(texto, ":|–|-"));
        } else if (lower.contains("início de lances") || lower.contains("inicio de lances")
                || lower.contains("inicio lances")) {
            builder.inicioLances(extrairValorAposRotulo(texto, ":|–|-"));
        }

        // Capturar link do edital
        Element link = elemento.selectFirst("a[href]");
        if (link != null && (lower.contains("edital") || lower.contains("documento"))) {
            builder.edital(link.attr("href"));
        }
    }

    /**
     * Extrai campos específicos por seletores CSS conhecidos do portal.
     */
    private void extrairCamposEspecificos(Licitacao.LicitacaoBuilder builder, Element card) {
        // Tentar extrair pelo texto do label seguido de valor
        for (Element el : card.select("span, td, div, p, label")) {
            String texto = el.text().trim();
            if (!texto.isEmpty()) {
                mapearCampo(builder, texto, el);
            }
        }

        // Extrair links de edital
        Element linkEdital = card.selectFirst("a[href*='edital'], a[href*='.pdf'], a[href*='documento']");
        if (linkEdital != null) {
            builder.edital(linkEdital.attr("href"));
            if (linkEdital.text() != null && !linkEdital.text().isEmpty()) {
                builder.numeroEdital(linkEdital.text().trim());
            }
        }
    }

    /**
     * Extrai o valor após um rótulo separado por delimitador (ex: "Campo: valor").
     */
    private String extrairValorAposRotulo(String texto, String delimitadorRegex) {
        String[] partes = texto.split(delimitadorRegex, 2);
        if (partes.length == 2) {
            return partes[1].trim();
        }
        return texto.trim();
    }

    /**
     * Extrai o texto de um elemento usando seletores CSS em ordem de prioridade.
     */
    private String extrairTexto(Element elemento, String seletores) {
        for (String seletor : seletores.split(",")) {
            Element encontrado = elemento.selectFirst(seletor.trim());
            if (encontrado != null && !encontrado.text().isEmpty()) {
                return encontrado.text().trim();
            }
        }
        return null;
    }

    /**
     * Retorna todas as licitações salvas no banco de dados do portal.
     */
    public List<Licitacao> listarLicitacoesSalvas() {
        return licitacaoRepository.findByPortalOrigem(PORTAL_NOME);
    }

    /**
     * Retorna todas as licitações salvas no banco de dados.
     */
    public List<Licitacao> listarTodasLicitacoes() {
        return licitacaoRepository.findAll();
    }
}
