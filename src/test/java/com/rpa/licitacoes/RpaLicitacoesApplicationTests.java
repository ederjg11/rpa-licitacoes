package com.rpa.licitacoes;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "selenium.headless=true",
        "rpa.jornal.salvar.banco=false"
})
class RpaLicitacoesApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring inicializa corretamente
    }
}
