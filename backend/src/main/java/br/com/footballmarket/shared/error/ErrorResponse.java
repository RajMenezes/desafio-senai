package br.com.footballmarket.shared.error;

import org.springframework.http.HttpStatusCode;

/**
 * Corpo padrão de toda recusa: um objeto JSON com {@code message} não vazio e sem stack trace.
 */
public record ErrorResponse(String message) {

    public static final String NOT_FOUND = "Recurso não encontrado.";
    public static final String INTERNAL_ERROR = "Erro interno inesperado. A operação foi desfeita.";

    public static String defaultMessageFor(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> "Requisição inválida.";
            case 404 -> NOT_FOUND;
            case 405 -> "Método não suportado para este caminho.";
            case 409 -> "A operação viola uma regra de negócio.";
            default -> status.is4xxClientError() ? "Requisição recusada." : INTERNAL_ERROR;
        };
    }
}
