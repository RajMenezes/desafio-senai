package br.com.footballmarket.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Recurso inexistente (404). Inclui jogadores excluídos logicamente.
 */
public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
