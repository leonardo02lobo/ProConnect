package Proconnect.backProconnect.exception;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Manejo centralizado de errores: toda respuesta de error es {@code application/problem+json} (RFC 7807) con el
 * código HTTP de su causa y un {@code traceId}. Los errores no previstos responden 500 genérico (sin stack trace,
 * SQL ni mensajes de Hibernate) y el detalle completo queda en el log asociado al mismo traceId.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    static final String GENERIC_500 = "Error interno del servidor";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(NotFoundException e, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage(), req);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> conflict(ConflictException e, HttpServletRequest req) {
        return respond(HttpStatus.CONFLICT, e.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ProblemDetail> forbidden(ForbiddenException e, HttpServletRequest req) {
        return respond(HttpStatus.FORBIDDEN, e.getMessage(), req);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ProblemDetail> validation(ValidationException e, HttpServletRequest req) {
        ProblemDetail problem = ProblemResponses.problem(HttpStatus.BAD_REQUEST, e.getMessage(), req);
        ProblemResponses.withErrors(problem, e.getErrors());
        return build(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> constraint(ConstraintViolationException e, HttpServletRequest req) {
        List<ValidationException.FieldError> errors = e.getConstraintViolations().stream()
                .map(v -> new ValidationException.FieldError(lastNode(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        ProblemDetail problem = ProblemResponses.problem(HttpStatus.BAD_REQUEST, "Datos inválidos", req);
        return build(ProblemResponses.withErrors(problem, errors));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> typeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        ProblemDetail problem = ProblemResponses.problem(HttpStatus.BAD_REQUEST, "Parámetro inválido", req);
        return build(ProblemResponses.withErrors(problem,
                List.of(new ValidationException.FieldError(e.getName(), "Valor no válido"))));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ProblemDetail> tooMany(TooManyRequestsException e, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(e.getRetryAfterSeconds()))
                .contentType(ProblemResponses.PROBLEM_JSON)
                .body(ProblemResponses.problem(HttpStatus.TOO_MANY_REQUESTS, e.getMessage(), req));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ProblemDetail> unauthenticated(AuthenticationCredentialsNotFoundException e,
            HttpServletRequest req) {
        return respond(HttpStatus.UNAUTHORIZED, "Sesión requerida", req);
    }

    /** Unicidad u otra restricción de la BD: 409 sin exponer el mensaje del driver. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> integrity(DataIntegrityViolationException e, HttpServletRequest req) {
        log.warn("Violación de integridad en {}: {}", req.getRequestURI(), e.getMostSpecificCause().getMessage());
        return respond(HttpStatus.CONFLICT, "El recurso entra en conflicto con datos existentes", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> unexpected(Exception e, HttpServletRequest req) {
        log.error("Error no controlado en {} {}", req.getMethod(), req.getRequestURI(), e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_500, req);
    }

    /** Bean Validation sobre {@code @Valid @RequestBody}: 400 con un elemento de {@code errors} por campo. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers, @NonNull HttpStatusCode status, @NonNull WebRequest request) {
        List<ValidationException.FieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationException.FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        ProblemDetail problem = ProblemResponses.problem(HttpStatus.BAD_REQUEST, "Datos inválidos", servlet(request));
        ProblemResponses.withErrors(problem, errors);
        return ResponseEntity.badRequest().contentType(ProblemResponses.PROBLEM_JSON).body(problem);
    }

    /**
     * Resto de excepciones estándar de Spring MVC (body ilegible, método no soportado, 404 de ruta...): se
     * conserva su código pero el cuerpo pasa a tener el formato común (title, instance, traceId) sin detalles
     * internos.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex, @Nullable Object body,
            @NonNull HttpHeaders headers, @NonNull HttpStatusCode statusCode, @NonNull WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (status.is5xxServerError()) {
            log.error("Error de infraestructura MVC", ex);
        }
        String detail = status.is5xxServerError() ? GENERIC_500
                : (body instanceof ProblemDetail pd && pd.getDetail() != null ? pd.getDetail()
                        : status.getReasonPhrase());
        if (status == HttpStatus.BAD_REQUEST && ex instanceof org.springframework.http.converter.HttpMessageNotReadableException) {
            detail = "Body ausente o con formato inválido";
        }
        ProblemDetail problem = ProblemResponses.problem(status, detail, servlet(request));
        return ResponseEntity.status(status).headers(headers).contentType(ProblemResponses.PROBLEM_JSON)
                .body(problem);
    }

    private static ResponseEntity<ProblemDetail> respond(HttpStatus status, String detail, HttpServletRequest req) {
        return build(ProblemResponses.problem(status, detail, req));
    }

    private static ResponseEntity<ProblemDetail> build(ProblemDetail problem) {
        return ResponseEntity.status(problem.getStatus()).contentType(ProblemResponses.PROBLEM_JSON).body(problem);
    }

    private static HttpServletRequest servlet(WebRequest request) {
        return request instanceof NativeWebRequest nwr ? nwr.getNativeRequest(HttpServletRequest.class) : null;
    }

    private static String lastNode(String path) {
        int dot = path.lastIndexOf('.');
        return dot >= 0 ? path.substring(dot + 1) : path;
    }
}
