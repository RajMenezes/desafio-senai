package br.com.footballmarket.shared.error;

import org.springframework.http.HttpStatus;

/**
 * Regra de negócio violada (409).
 */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
