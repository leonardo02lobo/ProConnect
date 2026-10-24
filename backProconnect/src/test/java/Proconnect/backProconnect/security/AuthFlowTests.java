package Proconnect.backProconnect.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.Entity.User;
import Proconnect.backProconnect.repository.LikeRepository;
import Proconnect.backProconnect.repository.PostsRepository;
import Proconnect.backProconnect.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowTests {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PostsRepository postsRepository;
    @Autowired
    LikeRepository likeRepository;

    @BeforeEach
    void clean() {
        likeRepository.deleteAll();
        postsRepository.deleteAll();
        userRepository.deleteAll();
    }

    private MvcResult register(String name, String email, String password, String extra) throws Exception {
        String body = "{\"name\":\"" + name + "\",\"userName\":\"" + name + "_u\",\"email\":\"" + email
                + "\",\"password\":\"" + password + "\"" + extra + "}";
        return mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();
    }

    private Cookie login(String email, String password) throws Exception {
        MvcResult res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return res.getResponse().getCookie("pc_session");
    }

    @Test
    void registerForcesUserRoleHashesPasswordAndHidesIt() throws Exception {
        MvcResult res = register("ana", "ana@example.com", "password123", ",\"role\":\"ADMIN\"");
        assertThat(res.getResponse().getStatus()).isEqualTo(201);
        assertThat(res.getResponse().getContentAsString()).doesNotContain("password").contains("\"role\":\"USER\"");
        User saved = userRepository.findByEmail("ana@example.com").orElseThrow();
        assertThat(saved.getRole()).isEqualTo("USER");
        assertThat(saved.getPassword()).startsWith("$2").isNotEqualTo("password123");
    }

    @Test
    void registerValidations() throws Exception {
        assertThat(register("ana", "ana@example.com", "password123", "").getResponse().getStatus()).isEqualTo(201);
        assertThat(register("ana2", "ana@example.com", "password123", "").getResponse().getStatus()).isEqualTo(409);
        assertThat(register("bob", "bob@example.com", "short", "").getResponse().getStatus()).isEqualTo(400);
        assertThat(register("bob", "not-an-email", "password123", "").getResponse().getStatus()).isEqualTo(400);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void loginSetsHttpOnlyCookieAndMeLogoutWork() throws Exception {
        register("ana", "ana@example.com", "password123", "");
        MvcResult res = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();
        String setCookie = res.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).startsWith("pc_session=").contains("HttpOnly").contains("SameSite=Lax")
                .contains("Path=/").contains("Max-Age=3600");
        Cookie session = res.getResponse().getCookie("pc_session");

        mvc.perform(get("/api/auth/me").cookie(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("ana_u")).andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());

        mvc.perform(post("/api/auth/logout").cookie(session)).andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        org.hamcrest.Matchers.allOf(org.hamcrest.Matchers.containsString("pc_session="),
                                org.hamcrest.Matchers.containsString("Max-Age=0"))));
    }

    @Test
    void wrongCredentialsAreGeneric401WithoutCookie() throws Exception {
        register("ana", "ana@example.com", "password123", "");
        MvcResult wrongPass = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@example.com\",\"password\":\"nope-nope\"}"))
                .andExpect(status().isUnauthorized()).andReturn();
        MvcResult noUser = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ghost@example.com\",\"password\":\"nope-nope\"}"))
                .andExpect(status().isUnauthorized()).andReturn();
        assertThat(wrongPass.getResponse().getHeader(HttpHeaders.SET_COOKIE)).isNull();
        assertThat(SecurityTestSupport.stripCorrelation(wrongPass.getResponse().getContentAsString()))
                .isEqualTo(SecurityTestSupport.stripCorrelation(noUser.getResponse().getContentAsString()));
    }

    @Test
    void everyApiEndpointRequiresSession() throws Exception {
        mvc.perform(get("/api/posts")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/users/1/profile")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/posts").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"x\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(postsRepository.count()).isZero();
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void forgedOrExpiredTokensAreRejected() throws Exception {
        String forged = Jwts.builder().subject("1").claim("role", "ADMIN")
                .signWith(Keys.hmacShaKeyFor("another-secret-another-secret-another".getBytes(StandardCharsets.UTF_8)))
                .compact();
        String expired = Jwts.builder().subject("1").expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        mvc.perform(get("/api/posts").cookie(new Cookie("pc_session", forged))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/posts").cookie(new Cookie("pc_session", expired))).andExpect(status().isUnauthorized());
    }

    @Test
    void authorComesFromTokenNotBody() throws Exception {
        register("ana", "ana@example.com", "password123", "");
        register("bob", "bob@example.com", "password123", "");
        Long anaId = userRepository.findByEmail("ana@example.com").orElseThrow().getId();
        Long bobId = userRepository.findByEmail("bob@example.com").orElseThrow().getId();
        Cookie ana = login("ana@example.com", "password123");

        mvc.perform(post("/api/posts").cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + bobId + ",\"title\":\"hola\",\"content\":\"c\"}"))
                .andExpect(status().isCreated());
        List<Posts> posts = postsRepository.findAll();
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getUserId()).isEqualTo(anaId);

        Long postId = posts.get(0).getId();
        mvc.perform(post("/api/posts/" + postId + "/likes").cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + bobId + ",\"postId\":" + postId + "}"))
                .andExpect(status().isCreated());
        assertThat(likeRepository.existsByUserIdAndPostId(anaId, postId)).isTrue();
        assertThat(likeRepository.existsByUserIdAndPostId(bobId, postId)).isFalse();

        String feed = mvc.perform(get("/api/posts").cookie(ana)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(feed).contains("\"userLiked\":true").doesNotContain("password");
        String users = mvc.perform(get("/api/users").cookie(ana)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(users).contains("ana@example.com").doesNotContain("password").doesNotContain("$2");
    }

    @Test
    void jwtServiceRefusesMissingOrShortSecret() {
        assertThatThrownBy(() -> new JwtService("", Duration.ofHours(1))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
        assertThatThrownBy(() -> new JwtService("too-short", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("32 bytes");
    }
}
