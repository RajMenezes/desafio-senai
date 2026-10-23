package br.com.footballmarket;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base dos testes de integração: aplicação inteira + PostgreSQL real (Testcontainers), com as migrations do Flyway.
 * O container é um bean, então é compartilhado por todas as classes que usam o mesmo contexto.
 * Os testes são ignorados quando o Docker não está disponível.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@Import(PostgresIntegrationTest.PostgresContainer.class)
public abstract class PostgresIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class PostgresContainer {

        @Bean
        @ServiceConnection
        PostgreSQLContainer<?> postgres() {
            return new PostgreSQLContainer<>("postgres:17-alpine");
        }
    }
}
