package Proconnect.backProconnect.web;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.cors.DefaultCorsProcessor;

import Proconnect.backProconnect.exception.ProblemResponses;

/** Igual que el procesador CORS por defecto, pero el rechazo (403) sale en formato problem+json. */
public class ProblemCorsProcessor extends DefaultCorsProcessor {

    @Override
    protected void rejectRequest(ServerHttpResponse response) throws IOException {
        if (response instanceof ServletServerHttpResponse servlet) {
            ProblemResponses.write(null, servlet.getServletResponse(), HttpStatus.FORBIDDEN, "Origen no permitido");
            return;
        }
        super.rejectRequest(response);
    }
}
