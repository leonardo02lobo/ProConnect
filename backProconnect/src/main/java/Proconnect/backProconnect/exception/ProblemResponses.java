package Proconnect.backProconnect.exception;

import Proconnect.backProconnect.web.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Construcción de respuestas RFC 7807 ({@code application/problem+json}) compartida por el {@link
 * GlobalExceptionHandler} y por los filtros que responden antes de llegar a un controller
 * (401/403/429).
 */
public final class ProblemResponses {

    public static final MediaType PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON;
    private static final ObjectMapper JSON = new JsonMapper();

    private ProblemResponses() {}

    /**
     * ProblemDetail con {@code type}, {@code title}, {@code status}, {@code detail}, {@code
     * instance} y traceId.
     */
    public static ProblemDetail problem(
            HttpStatus status, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setType(URI.create("about:blank"));
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }
        String traceId = MDC.get(TraceIdFilter.MDC_KEY);
        if (traceId != null) {
            problem.setProperty("traceId", traceId);
        }
        return problem;
    }

    public static ProblemDetail withErrors(
            ProblemDetail problem, List<ValidationException.FieldError> errors) {
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * Escribe el problem+json directamente en la respuesta servlet (filtros y entry points de
     * seguridad).
     */
    public static void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String detail)
            throws IOException {
        ProblemDetail problem = problem(status, detail, request);
        response.setStatus(status.value());
        response.setContentType(PROBLEM_JSON.toString());
        response.setCharacterEncoding("UTF-8");
        JSON.writeValue(response.getWriter(), toMap(problem));
    }

    private static java.util.Map<String, Object> toMap(ProblemDetail problem) {
        java.util.Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("type", String.valueOf(problem.getType()));
        body.put("title", problem.getTitle());
        body.put("status", problem.getStatus());
        body.put("detail", problem.getDetail());
        if (problem.getInstance() != null) {
            body.put("instance", problem.getInstance().toString());
        }
        if (problem.getProperties() != null) {
            body.putAll(problem.getProperties());
        }
        return body;
    }
}
