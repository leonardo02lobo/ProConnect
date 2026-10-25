package Proconnect.backProconnect.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.repository.CommentsRepository;
import Proconnect.backProconnect.repository.CompanyRepository;
import Proconnect.backProconnect.repository.FriendRepository;
import Proconnect.backProconnect.repository.JobRepository;
import Proconnect.backProconnect.repository.LikeRepository;
import Proconnect.backProconnect.repository.MessageRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import Proconnect.backProconnect.repository.PostulationRepository;
import Proconnect.backProconnect.repository.ProfilePhotosRepository;
import Proconnect.backProconnect.repository.UserRepository;
import Proconnect.backProconnect.repository.WorkStationRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Utilidades comunes de los tests de seguridad: datos limpios, usuarios con rol y sesión real por
 * cookie.
 */
public abstract class SecurityTestSupport {

    protected static final String PASSWORD = "password123";

    @Autowired protected MockMvc mvc;
    @Autowired protected UserRepository users;
    @Autowired protected PostsRepository posts;
    @Autowired protected CommentsRepository comments;
    @Autowired protected LikeRepository likes;
    @Autowired protected CompanyRepository companies;
    @Autowired protected JobRepository jobs;
    @Autowired protected PostulationRepository postulations;
    @Autowired protected MessageRepository messages;
    @Autowired protected FriendRepository friends;
    @Autowired protected ProfilePhotosRepository photos;
    @Autowired protected WorkStationRepository workStations;

    @BeforeEach
    void cleanDatabase() {
        postulations.deleteAll();
        jobs.deleteAll();
        companies.deleteAll();
        messages.deleteAll();
        friends.deleteAll();
        likes.deleteAll();
        comments.deleteAll();
        posts.deleteAll();
        photos.deleteAll();
        workStations.deleteAll();
        users.deleteAll();
    }

    /**
     * Registra un usuario (siempre USER) y, si hace falta, le asigna otro rol directamente en la
     * BD.
     */
    protected Long createUser(String name, String role) throws Exception {
        String body =
                "{\"name\":\""
                        + name
                        + "\",\"userName\":\""
                        + name
                        + "_u\",\"email\":\""
                        + name
                        + "@example.com\",\"password\":\""
                        + PASSWORD
                        + "\"}";
        mvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                .andExpect(status().isCreated());
        User user = users.findByEmail(name + "@example.com").orElseThrow();
        if (!"USER".equals(role)) {
            user.setRole(role);
            users.save(user);
        }
        return user.getId();
    }

    /**
     * Cuerpo de error sin los campos de correlación ({@code traceId} es aleatorio por petición e
     * {@code instance} repite la URI pedida): lo que queda es lo que podría filtrar información del
     * recurso.
     */
    protected static String withoutCorrelation(String problemJson) {
        return stripCorrelation(problemJson);
    }

    public static String stripCorrelation(String problemJson) {
        return problemJson.replaceAll(",?\\s*\"(traceId|instance)\"\\s*:\\s*\"[^\"]*\"", "");
    }

    protected Cookie login(String name) throws Exception {
        MvcResult res =
                mvc.perform(
                                post("/api/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                "{\"email\":\""
                                                        + name
                                                        + "@example.com\",\"password\":\""
                                                        + PASSWORD
                                                        + "\"}"))
                        .andExpect(status().isOk())
                        .andReturn();
        return res.getResponse().getCookie("pc_session");
    }
}
