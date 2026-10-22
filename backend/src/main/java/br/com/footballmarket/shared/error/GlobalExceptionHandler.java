package br.com.footballmarket.shared.error;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Converte toda falha em {@code {"message": "..."}} com o status do enunciado:
 * 400 (dado inválido), 404 (inexistente), 409 (regra de negócio) e 500 (inesperado).
 * Sempre devolve um único erro por requisição.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
        return respond(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return respond(HttpStatus.BAD_REQUEST, describeUnreadableBody(ex));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Dados inválidos.");
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ErrorResponse> handleInvalidParameters(HandlerMethodValidationException ex) {
        String message = ex.getAllErrors().stream()
                .map(MessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Parâmetros inválidos.");
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElse("Dados inválidos.");
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Id inválido na URL ({@code abc}, {@code 0}, {@code 01}...) conta como inexistente (404);
     * valor inválido em query string é 400.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return respondToInvalidArgument(ex.getParameter(), ex.getName());
    }

    @ExceptionHandler(MethodArgumentConversionNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleConversionNotSupported(MethodArgumentConversionNotSupportedException ex) {
        return respondToInvalidArgument(ex.getParameter(), ex.getName());
    }

    private static ResponseEntity<ErrorResponse> respondToInvalidArgument(MethodParameter parameter, String name) {
        if (parameter.hasParameterAnnotation(PathVariable.class)) {
            return respond(HttpStatus.NOT_FOUND, ErrorResponse.NOT_FOUND);
        }
        return respond(HttpStatus.BAD_REQUEST, "Valor inválido para o parâmetro '" + name + "'.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        return respond(HttpStatus.BAD_REQUEST, "O parâmetro '" + ex.getParameterName() + "' é obrigatório.");
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    ResponseEntity<ErrorResponse> handleUnknownRoute(Exception ex) {
        return respond(HttpStatus.NOT_FOUND, ErrorResponse.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return respond(HttpStatus.METHOD_NOT_ALLOWED, ErrorResponse.defaultMessageFor(HttpStatus.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return respond(HttpStatus.BAD_REQUEST, "O corpo da requisição deve ser JSON (Content-Type: application/json).");
    }

    /**
     * Rede de segurança para restrições do banco (ex.: unicidade sob concorrência).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade no banco: {}", ex.getMostSpecificCause().getMessage());
        return respond(HttpStatus.CONFLICT, ErrorResponse.defaultMessageFor(HttpStatus.CONFLICT));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Falha inesperada", ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponse.INTERNAL_ERROR);
    }

    /**
     * O Content-Type é fixado explicitamente para que o erro seja JSON mesmo em endpoints
     * que produzem outro formato (ex.: exportação CSV).
     */
    private static ResponseEntity<ErrorResponse> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(message));
    }

    private static String describeUnreadableBody(HttpMessageNotReadableException ex) {
        for (Throwable cause = ex.getCause(); cause != null; cause = cause.getCause()) {
            if (cause instanceof ApiException apiException) {
                String field = fieldPath(ex.getCause());
                return field.isEmpty() ? apiException.getMessage() : "Campo '" + field + "': " + apiException.getMessage();
            }
        }
        if (ex.getCause() instanceof JsonParseException) {
            return "JSON malformado.";
        }
        if (ex.getCause() instanceof JsonMappingException mappingException) {
            String field = fieldPath(mappingException);
            return field.isEmpty()
                    ? "O corpo da requisição deve ser um objeto JSON."
                    : "Valor inválido para o campo '" + field + "'.";
        }
        return "O corpo da requisição está ausente ou não é um JSON válido.";
    }

    private static String fieldPath(Throwable throwable) {
        if (!(throwable instanceof JsonMappingException mappingException)) {
            return "";
        }
        StringBuilder path = new StringBuilder();
        for (JsonMappingException.Reference reference : mappingException.getPath()) {
            if (reference.getFieldName() != null) {
                if (!path.isEmpty()) {
                    path.append('.');
                }
                path.append(reference.getFieldName());
            } else if (reference.getIndex() >= 0) {
                path.append('[').append(reference.getIndex()).append(']');
            }
        }
        return path.toString();
    }
}
