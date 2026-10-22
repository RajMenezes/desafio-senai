package br.com.footballmarket.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Base das exceções que viram uma resposta HTTP com corpo {@code {"message": "..."}}.
 */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
