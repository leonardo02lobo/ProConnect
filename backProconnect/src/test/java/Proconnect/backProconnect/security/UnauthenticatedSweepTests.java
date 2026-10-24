package Proconnect.backProconnect.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Recorre TODOS los endpoints registrados en Spring MVC (no una lista a mano, así sobrevive a renombrados de
 * rutas) y comprueba que sin sesión responden 401, salvo login, registro y logout.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UnauthenticatedSweepTests {

    private static final Set<String> PUBLIC = Set.of("POST /api/auth/login", "POST /api/auth/register",
            "POST /api/auth/logout");

    @Autowired
    MockMvc mvc;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    RequestMappingHandlerMapping mapping;

    @Test
    void everyApiEndpointRequiresSession() throws Exception {
        List<String> checked = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (RequestMappingInfo info : mapping.getHandlerMethods().keySet()) {
            Set<String> patterns = info.getPatternValues();
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            for (String pattern : patterns) {
                if (!pattern.startsWith("/api")) {
                    continue;
                }
                for (RequestMethod method : methods.isEmpty() ? Set.of(RequestMethod.GET) : methods) {
                    String key = method.name() + " " + pattern;
                    if (PUBLIC.contains(key)) {
                        continue;
                    }
                    String uri = pattern.replaceAll("\\{[^}]+}", "1");
                    int status = mvc.perform(request(HttpMethod.valueOf(method.name()), uri)
                            .param("keyword", "x").param("userIdBeFollowed", "1").param("userId", "1")
                            .param("otherUserId", "1")
                            .contentType(MediaType.APPLICATION_JSON).content("{}"))
                            .andReturn().getResponse().getStatus();
                    checked.add(key);
                    if (status != 401) {
                        failures.add(key + " -> " + status);
                    }
                }
            }
        }
        assertThat(checked).hasSizeGreaterThan(40);
        assertThat(failures).as("endpoints accesibles sin sesión").isEmpty();
    }
}
