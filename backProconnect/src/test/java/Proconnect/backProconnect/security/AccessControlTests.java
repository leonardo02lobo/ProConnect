package Proconnect.backProconnect.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import Proconnect.backProconnect.Entity.Comments;
import Proconnect.backProconnect.Entity.Company;
import Proconnect.backProconnect.Entity.Friend;
import Proconnect.backProconnect.Entity.Job;
import Proconnect.backProconnect.Entity.Message;
import Proconnect.backProconnect.Entity.Posts;
import Proconnect.backProconnect.Entity.Postulation;
import Proconnect.backProconnect.Entity.ProfilePhotos;
import Proconnect.backProconnect.Entity.WorkStation;
import jakarta.servlet.http.Cookie;

/**
 * Regresión de IDOR / control de acceso (auditoría SEC-01..SEC-09): A intenta mutar o leer recursos de B.
 * Públicos con mutación → 403; privados (mensajes, postulaciones) → 404 idéntico a un id inexistente.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccessControlTests extends SecurityTestSupport {

    Long anaId;
    Long bobId;
    Long carlaId;
    Cookie ana;
    Cookie bob;
    Cookie carla;
    Cookie dave;
    Cookie admin;

    @BeforeEach
    void users() throws Exception {
        anaId = createUser("ana", "USER");
        bobId = createUser("bob", "USER");
        carlaId = createUser("carla", "COMPANY_ADMIN");
        createUser("dave", "COMPANY_ADMIN");
        createUser("root", "ADMIN");
        ana = login("ana");
        bob = login("bob");
        carla = login("carla");
        dave = login("dave");
        admin = login("root");
    }

    private String json(String body) {
        return body;
    }

    private Long anaPost() {
        return posts.save(new Posts(null, anaId, "contenido", "titulo", null, new java.util.Date())).getId();
    }

    // ---- Posts (SEC-01) ----

    @Test
    void onlyAuthorEditsPost() throws Exception {
        Long postId = anaPost();
        mvc.perform(put("/api/posts/" + postId).cookie(bob).contentType(MediaType.APPLICATION_JSON)
                .content(json("{\"title\":\"hackeado\",\"content\":\"x\"}"))).andExpect(status().isForbidden());
        assertThat(posts.findById(postId).orElseThrow().getTitle()).isEqualTo("titulo");

        mvc.perform(put("/api/posts/" + postId).cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content(json("{\"title\":\"nuevo\",\"content\":\"x\"}"))).andExpect(status().isOk());
        assertThat(posts.findById(postId).orElseThrow().getTitle()).isEqualTo("nuevo");
    }

    @Test
    void onlyAuthorOrAdminDeletesPost() throws Exception {
        Long postId = anaPost();
        mvc.perform(delete("/api/posts/" + postId).cookie(bob)).andExpect(status().isForbidden());
        assertThat(posts.existsById(postId)).isTrue();
        mvc.perform(delete("/api/posts/" + postId).cookie(admin)).andExpect(status().isNoContent());
        assertThat(posts.existsById(postId)).isFalse();

        Long own = anaPost();
        mvc.perform(delete("/api/posts/" + own).cookie(ana)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/posts/999999").cookie(ana)).andExpect(status().isNotFound());
    }

    // ---- Comentarios (SEC-02) ----

    @Test
    void onlyAuthorOrAdminDeletesComment() throws Exception {
        Long postId = anaPost();
        Long c1 = comments.save(new Comments(null, postId, anaId, "hola", new java.util.Date())).getId();
        Long c2 = comments.save(new Comments(null, postId, anaId, "otra", new java.util.Date())).getId();
        mvc.perform(delete("/api/comments/" + c1).cookie(bob)).andExpect(status().isForbidden());
        assertThat(comments.existsById(c1)).isTrue();
        mvc.perform(delete("/api/comments/" + c1).cookie(ana)).andExpect(status().isNoContent());
        assertThat(comments.existsById(c1)).isFalse();
        mvc.perform(delete("/api/comments/" + c2).cookie(admin)).andExpect(status().isNoContent());
    }

    @Test
    void createIgnoresBodyIdSoForeignRowsCannotBeOverwritten() throws Exception {
        Long postId = anaPost();
        Long c1 = comments.save(new Comments(null, postId, anaId, "original", new java.util.Date())).getId();
        mvc.perform(post("/api/posts/" + postId + "/comments").cookie(bob).contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":" + c1 + ",\"postId\":" + postId + ",\"content\":\"pisado\"}"))
                .andExpect(status().isCreated());
        Comments original = comments.findById(c1).orElseThrow();
        assertThat(original.getContent()).isEqualTo("original");
        assertThat(original.getUserId()).isEqualTo(anaId);
        assertThat(comments.count()).isEqualTo(2);
    }

    // ---- Empresas y empleos (SEC-03) ----

    @Test
    void onlyCompanyAdminOrAdminCreatesCompanies() throws Exception {
        String body = "{\"name\":\"ACME\",\"description\":\"d\",\"location\":\"l\"}";
        mvc.perform(post("/api/companies").cookie(ana).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        assertThat(companies.count()).isZero();
        mvc.perform(post("/api/companies").cookie(carla).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        assertThat(companies.findAll()).singleElement().extracting(Company::getUserId).isEqualTo(carlaId);
        mvc.perform(post("/api/companies").cookie(admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void onlyOwnerManagesCompanyAndJobs() throws Exception {
        Long companyId = companies.save(new Company(null, "ACME", "d", "l", carlaId)).getId();
        String edit = "{\"name\":\"EVIL\",\"description\":\"d\",\"location\":\"l\"}";
        mvc.perform(put("/api/companies/" + companyId).cookie(dave).contentType(MediaType.APPLICATION_JSON)
                .content(edit)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/companies/" + companyId).cookie(bob)).andExpect(status().isForbidden());
        assertThat(companies.findById(companyId).orElseThrow().getName()).isEqualTo("ACME");

        String job = "{\"companyId\":" + companyId + ",\"title\":\"Dev\",\"description\":\"d\",\"workStation\":\"w\"}";
        mvc.perform(post("/api/jobs").cookie(dave).contentType(MediaType.APPLICATION_JSON).content(job))
                .andExpect(status().isForbidden());
        assertThat(jobs.count()).isZero();
        mvc.perform(post("/api/jobs").cookie(carla).contentType(MediaType.APPLICATION_JSON).content(job))
                .andExpect(status().isCreated());
        Long jobId = jobs.findAll().get(0).getId();
        mvc.perform(delete("/api/jobs/" + jobId).cookie(dave)).andExpect(status().isForbidden());
        assertThat(jobs.existsById(jobId)).isTrue();
        mvc.perform(delete("/api/jobs/" + jobId).cookie(carla)).andExpect(status().isNoContent());
        assertThat(jobs.existsById(jobId)).isFalse();

        mvc.perform(put("/api/companies/" + companyId).cookie(carla).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"ACME2\",\"description\":\"d\",\"location\":\"l\"}")).andExpect(status().isOk());
        mvc.perform(delete("/api/companies/" + companyId).cookie(admin)).andExpect(status().isNoContent());
        assertThat(companies.existsById(companyId)).isFalse();
    }

    // ---- Postulaciones (SEC-04) ----

    @Test
    void postulationsArePrivate() throws Exception {
        Long companyId = companies.save(new Company(null, "ACME", "d", "l", carlaId)).getId();
        Long jobId = jobs.save(new Job(null, companyId, "Dev", "d", "w", new java.util.Date())).getId();
        mvc.perform(post("/api/postulations").cookie(bob).contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\":" + jobId + ",\"userId\":" + anaId + "}")).andExpect(status().isCreated());
        Postulation bobs = postulations.findAll().get(0);
        assertThat(bobs.getUserId()).isEqualTo(bobId);

        MvcResult foreign = mvc.perform(get("/api/postulations/" + bobs.getId()).cookie(ana))
                .andExpect(status().isNotFound()).andReturn();
        MvcResult missing = mvc.perform(get("/api/postulations/999999").cookie(ana))
                .andExpect(status().isNotFound()).andReturn();
        assertThat(withoutCorrelation(foreign.getResponse().getContentAsString()))
                .isEqualTo(withoutCorrelation(missing.getResponse().getContentAsString()));

        mvc.perform(get("/api/postulations/" + bobs.getId()).cookie(bob)).andExpect(status().isOk());
        mvc.perform(get("/api/postulations/" + bobs.getId()).cookie(carla)).andExpect(status().isOk());
        mvc.perform(get("/api/postulations/" + bobs.getId()).cookie(admin)).andExpect(status().isOk());

        // Listados: el ajeno no ve nada; el dueño de la empresa ve las de sus empleos.
        String anaList = mvc.perform(get("/api/postulations").cookie(ana)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(anaList).isEqualTo("[]");
        String carlaList = mvc.perform(get("/api/postulations").cookie(carla)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(carlaList).contains("\"jobId\":" + jobId);

        mvc.perform(get("/api/jobs/" + jobId + "/postulations").cookie(dave)).andExpect(status().isForbidden());
        mvc.perform(get("/api/jobs/" + jobId + "/postulations").cookie(carla)).andExpect(status().isOk());

        mvc.perform(put("/api/postulations/" + bobs.getId()).cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"jobId\":" + jobId + "}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/postulations/" + bobs.getId()).cookie(ana)).andExpect(status().isNotFound());
        assertThat(postulations.existsById(bobs.getId())).isTrue();
        mvc.perform(delete("/api/postulations/" + bobs.getId()).cookie(bob)).andExpect(status().isNoContent());
        assertThat(postulations.existsById(bobs.getId())).isFalse();
    }

    // ---- Mensajes (SEC-05) ----

    @Test
    void messagesVisibleOnlyToParticipants() throws Exception {
        mvc.perform(post("/api/messages").cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"receiverId\":" + bobId + ",\"content\":\"secreto\",\"senderId\":" + bobId + "}"))
                .andExpect(status().isCreated());
        Message msg = messages.findAll().get(0);
        assertThat(msg.getSenderId()).isEqualTo(anaId);

        String carlaView = mvc.perform(get("/api/messages").cookie(carla)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(carlaView).doesNotContain("secreto");
        String bobView = mvc.perform(get("/api/messages").cookie(bob)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(bobView).contains("secreto");

        mvc.perform(delete("/api/messages/" + msg.getId()).cookie(carla)).andExpect(status().isNotFound());
        assertThat(messages.existsById(msg.getId())).isTrue();
        mvc.perform(delete("/api/messages/" + msg.getId()).cookie(ana)).andExpect(status().isNoContent());
    }

    // ---- Usuarios, fotos, workstations y amistades (SEC-06..SEC-09) ----

    @Test
    void onlySelfOrAdminUpdatesOrDeletesUser() throws Exception {
        String body = "{\"name\":\"pwned\",\"userName\":\"pwned\",\"email\":\"pwned@example.com\"}";
        mvc.perform(put("/api/users/" + bobId).cookie(ana).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        assertThat(users.findById(bobId).orElseThrow().getName()).isEqualTo("bob");
        mvc.perform(delete("/api/users/" + bobId).cookie(ana)).andExpect(status().isForbidden());
        assertThat(users.existsById(bobId)).isTrue();

        mvc.perform(put("/api/users/" + anaId).cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"ana2\",\"userName\":\"ana_u\",\"email\":\"ana@example.com\"}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/api/users/" + bobId).cookie(admin)).andExpect(status().isNoContent());
        assertThat(users.existsById(bobId)).isFalse();
    }

    @Test
    void onlyOwnerChangesProfilePhotos() throws Exception {
        Long photoId = photos.save(new ProfilePhotos(null, anaId, "a.png", "w.png")).getId();
        mvc.perform(put("/api/profile-photos/" + photoId).cookie(bob).contentType(MediaType.APPLICATION_JSON)
                .content("{\"photoUrl\":\"evil.png\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/profile-photos/" + photoId).cookie(bob).contentType(MediaType.APPLICATION_JSON)
                .content("{\"wallPhotoUrl\":\"evil.png\"}")).andExpect(status().isForbidden());
        ProfilePhotos p = photos.findById(photoId).orElseThrow();
        assertThat(p.getPhotoUrl()).isEqualTo("a.png");
        assertThat(p.getWallPhotoUrl()).isEqualTo("w.png");
        mvc.perform(put("/api/profile-photos/" + photoId).cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"photoUrl\":\"b.png\"}")).andExpect(status().isOk());
        mvc.perform(get("/api/profile-photos/" + photoId).cookie(bob)).andExpect(status().isOk());
    }

    @Test
    void onlyOwnerManagesWorkStation() throws Exception {
        Long wsId = workStations.save(new WorkStation(null, anaId, "Backend")).getId();
        mvc.perform(put("/api/workstations/" + wsId).cookie(bob).contentType(MediaType.APPLICATION_JSON)
                .content("{\"workStation\":\"robado\"}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/workstations/" + wsId).cookie(bob)).andExpect(status().isForbidden());
        assertThat(workStations.findById(wsId).orElseThrow().getWorkStation()).isEqualTo("Backend");
        mvc.perform(put("/api/workstations/" + wsId).cookie(admin).contentType(MediaType.APPLICATION_JSON)
                .content("{\"workStation\":\"Moderado\"}")).andExpect(status().isOk());
        // El ADMIN modera pero no se apropia del recurso.
        assertThat(workStations.findById(wsId).orElseThrow().getUserId()).isEqualTo(anaId);
    }

    @Test
    void onlyRecipientAcceptsOrRejectsFriendRequest() throws Exception {
        mvc.perform(post("/api/friends/requests").cookie(ana).contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":" + bobId + "}")).andExpect(status().isCreated());
        Friend request = friends.findAll().get(0);
        mvc.perform(post("/api/friends/requests/" + request.getId() + "/accept").cookie(carla)).andExpect(status().isForbidden());
        mvc.perform(post("/api/friends/requests/" + request.getId() + "/accept").cookie(ana)).andExpect(status().isForbidden());
        mvc.perform(post("/api/friends/requests/" + request.getId() + "/reject").cookie(carla)).andExpect(status().isForbidden());
        assertThat(friends.findById(request.getId()).orElseThrow().getStatus()).isEqualTo("PENDING");
        mvc.perform(post("/api/friends/requests/" + request.getId() + "/accept").cookie(bob)).andExpect(status().isOk());
        assertThat(friends.findById(request.getId()).orElseThrow().getStatus()).isEqualTo("ACCEPTED");
    }
}
