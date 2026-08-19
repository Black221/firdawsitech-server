package sn.lhacksrt.firdawsitech_server.web.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,      // e.g. "Bad Request"
        String code,       // e.g. "VALIDATION_ERROR" | "ENTITY_NOT_FOUND"
        String message,    // message lisible
        String path,       // URI de la requête
        List<FieldViolation> violations // erreurs de validation champ par champ
) {
    public record FieldViolation(String field, String message, Object rejectedValue) {}
}
