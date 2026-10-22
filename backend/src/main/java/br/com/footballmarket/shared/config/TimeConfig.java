package br.com.footballmarket.shared.config;

import br.com.footballmarket.shared.time.BrasiliaTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Relógio do servidor no horário de Brasília. Injetar o {@link Clock} (em vez de chamar {@code now()}
 * direto) permite fixar o instante nos testes de regras que dependem de datas (ex.: multa da RN15).
 */
@Configuration
public class TimeConfig {

    @Bean
    Clock clock() {
        return Clock.system(BrasiliaTime.OFFSET);
    }
}
