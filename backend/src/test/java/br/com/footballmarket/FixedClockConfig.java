package br.com.footballmarket;

import br.com.footballmarket.player.domain.PlayerFixtures;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Relógio parado em {@link PlayerFixtures#NOW}, para os testes de controller.
 */
@TestConfiguration
public class FixedClockConfig {

    @Bean
    Clock clock() {
        return Clock.fixed(PlayerFixtures.NOW.toInstant(), ZoneOffset.ofHours(-3));
    }
}
