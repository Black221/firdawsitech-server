package sn.lhacksrt.firdawsitech_server.web.error;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /* ===================== 400 Bad Request  ===================== */

    @ExceptionHandler({
            IllegalArgumentException.class,
            IllegalStateException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest req) {
        log.debug("Bad request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req, null);
    }

    /* ===== 400 Bad Request (validation @Valid sur body: MethodArgumentNotValid) ===== */

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest req) {
        List<ApiError.FieldViolation> violations = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            violations.add(new ApiError.FieldViolation(
                    fe.getField(),
                    fe.getDefaultMessage(),
                    fe.getRejectedValue()
            ));
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Données invalides. Corrigez les champs indiqués.", req, violations);
    }

    /* ===== 400 Bad Request (validation @Validated sur params/path: ConstraintViolation) ===== */

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        List<ApiError.FieldViolation> violations = ex.getConstraintViolations().stream()
                .map(cv -> new ApiError.FieldViolation(
                        cv.getPropertyPath().toString(),
                        cv.getMessage(),
                        cv.getInvalidValue()))
                .toList();

        return build(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION",
                "Paramètres invalides.", req, violations);
    }

    /* ===================== 404 Not Found ===================== */

    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class, NotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", ex.getMessage(), req, null);
    }

    /* ===================== 405 Method Not Allowed ===================== */

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), req, null);
    }

    /* ===================== 409 Conflict (conflits DB: unique, FK…) ===================== */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        String msg = rootMessage(ex);
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                msg != null ? msg : "Conflit d’intégrité des données.", req, null);
    }

    /* ===================== 500 Internal Server Error ===================== */

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest req) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Une erreur inattendue s’est produite.", req, null);
    }

    /* ===================== Helpers ===================== */

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message,
                                           HttpServletRequest req,
                                           List<ApiError.FieldViolation> violations) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                req.getRequestURI(),
                violations == null || violations.isEmpty() ? null : violations
        );
        return ResponseEntity.status(status).body(body);
    }

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur.getMessage() != null && !cur.getMessage().isBlank()) return cur.getMessage();
            cur = cur.getCause();
        }
        return null;
    }
}
