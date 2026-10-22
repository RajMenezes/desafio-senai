package br.com.footballmarket.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Dado, formato ou parâmetro inválido (400).
 */
public class BadRequestException extends ApiException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
