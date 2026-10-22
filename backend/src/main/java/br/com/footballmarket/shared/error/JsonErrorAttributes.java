package br.com.footballmarket.shared.error;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

/**
 * Erros que escapam do Spring MVC (ex.: rejeitados pelo Tomcat) caem no /error do Spring Boot.
 * Aqui o corpo é reduzido ao formato do enunciado: {@code {"message": "..."}}.
 */
@Component
public class JsonErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> defaults = super.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        Object status = defaults.get("status");
        HttpStatus httpStatus = status instanceof Integer code
                ? HttpStatus.resolve(code)
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return Map.of("message", ErrorResponse.defaultMessageFor(
                httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
