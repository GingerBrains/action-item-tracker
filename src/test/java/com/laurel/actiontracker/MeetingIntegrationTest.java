package com.laurel.actiontracker;

import com.laurel.actiontracker.dto.request.LoginRequest;
import com.laurel.actiontracker.dto.request.MeetingRequest;
import com.laurel.actiontracker.dto.request.RegisterRequest;
import com.laurel.actiontracker.dto.response.AuthResponse;
import com.laurel.actiontracker.dto.response.MeetingResponse;
import com.laurel.actiontracker.entity.Meeting;
import com.laurel.actiontracker.entity.User;
import com.laurel.actiontracker.repository.MeetingRepository;
import com.laurel.actiontracker.repository.RefreshTokenRepository;
import com.laurel.actiontracker.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class MeetingIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("actiontracker_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        User admin = new User();
        admin.setEmail("testadmin@company.com");
        admin.setFullName("Test Admin");
        admin.setPasswordHash(passwordEncoder.encode("adminPassword123"));
        admin.setRole(User.Role.ADMIN);
        userRepository.save(admin);

        User member = new User();
        member.setEmail("testmember@company.com");
        member.setFullName("Test Member");
        member.setPasswordHash(passwordEncoder.encode("memberPassword123"));
        member.setRole(User.Role.MEMBER);
        userRepository.save(member);
    }

    @AfterEach
    void cleanup() {
        refreshTokenRepository.deleteAll();
        meetingRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String loginAndGetToken(RestTestClient restClient, String email, String password) {
        LoginRequest request = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        return restClient
                .post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getAccessToken();
    }

    private MeetingRequest buildMeetingRequest() {
        return MeetingRequest.builder()
                .title("Q1 Planning")
                .description("Quarterly planning session")
                .meetingDate(LocalDate.of(2026, 7, 15))
                .status(Meeting.Status.SCHEDULED)
                .build();
    }

    @Test
    void registerReturnsOk(@Autowired RestTestClient restClient) {
        RegisterRequest request = RegisterRequest.builder()
                .email("integrationtest@company.com")
                .fullName("Integration Test User")
                .password("somePassword123")
                .build();

        restClient
                .post().uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void loginReturnsOk(@Autowired RestTestClient restClient) {
        LoginRequest request = LoginRequest.builder()
                .email("testadmin@company.com")
                .password("adminPassword123")
                .build();

        restClient
                .post().uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void register_login_createMeeting_fullFlow(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Q1 Planning")
                .jsonPath("$.status").isEqualTo("SCHEDULED");
    }

    @Test
    void createMeeting_returns201ForAdmin(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.title").isEqualTo("Q1 Planning");
    }

    @Test
    void createMeeting_returns403ForMember(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testmember@company.com", "memberPassword123");

        restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getAllMeetings_returns200(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated();

        restClient
                .get().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("Q1 Planning");
    }

    @Test
    void getMeetingById_returns200(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        Long id = restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MeetingResponse.class)
                .returnResult()
                .getResponseBody()
                .getId();

        restClient
                .get().uri("/api/v1/meetings/{id}", id)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.title").isEqualTo("Q1 Planning");
    }

    @Test
    void getMeetingById_returns404WhenNotFound(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        restClient
                .get().uri("/api/v1/meetings/{id}", 999999L)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void updateMeeting_returns200ForAdmin(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        Long id = restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MeetingResponse.class)
                .returnResult()
                .getResponseBody()
                .getId();

        MeetingRequest updateRequest = MeetingRequest.builder()
                .title("Q1 Planning Updated")
                .meetingDate(LocalDate.of(2026, 7, 20))
                .status(Meeting.Status.IN_PROGRESS)
                .build();

        restClient
                .put().uri("/api/v1/meetings/{id}", id)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Q1 Planning Updated")
                .jsonPath("$.status").isEqualTo("IN_PROGRESS");
    }

    @Test
    void updateMeeting_returns403ForMember(@Autowired RestTestClient restClient) {
        String adminToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");
        String memberToken = loginAndGetToken(restClient, "testmember@company.com", "memberPassword123");

        Long id = restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MeetingResponse.class)
                .returnResult()
                .getResponseBody()
                .getId();

        restClient
                .put().uri("/api/v1/meetings/{id}", id)
                .header("Authorization", "Bearer " + memberToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void deleteMeeting_returns204ForAdmin(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        Long id = restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MeetingResponse.class)
                .returnResult()
                .getResponseBody()
                .getId();

        restClient
                .delete().uri("/api/v1/meetings/{id}", id)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteMeeting_returns403ForMember(@Autowired RestTestClient restClient) {
        String adminToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");
        String memberToken = loginAndGetToken(restClient, "testmember@company.com", "memberPassword123");

        Long id = restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MeetingResponse.class)
                .returnResult()
                .getResponseBody()
                .getId();

        restClient
                .delete().uri("/api/v1/meetings/{id}", id)
                .header("Authorization", "Bearer " + memberToken)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getAllMeetings_returns200ForMember(@Autowired RestTestClient restClient) {
        String memberToken = loginAndGetToken(restClient, "testmember@company.com", "memberPassword123");

        restClient
                .get().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + memberToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getMeetingById_returns200ForMember(@Autowired RestTestClient restClient) {
        String adminToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");
        String memberToken = loginAndGetToken(restClient, "testmember@company.com", "memberPassword123");

        Long id = restClient
                .post().uri("/api/v1/meetings")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildMeetingRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(MeetingResponse.class)
                .returnResult()
                .getResponseBody()
                .getId();

        restClient
                .get().uri("/api/v1/meetings/{id}", id)
                .header("Authorization", "Bearer " + memberToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Q1 Planning");
    }

    @Test
    void updateMeeting_returns404WhenNotFound(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        MeetingRequest updateRequest = MeetingRequest.builder()
                .title("Does Not Exist")
                .meetingDate(LocalDate.of(2026, 7, 20))
                .status(Meeting.Status.SCHEDULED)
                .build();

        restClient
                .put().uri("/api/v1/meetings/{id}", 999999L)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void deleteMeeting_returns404WhenNotFound(@Autowired RestTestClient restClient) {
        String accessToken = loginAndGetToken(restClient, "testadmin@company.com", "adminPassword123");

        restClient
                .delete().uri("/api/v1/meetings/{id}", 999999L)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getAllMeetings_returns401WhenUnauthenticated(@Autowired RestTestClient restClient) {
        restClient
                .get().uri("/api/v1/meetings")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getMeetingById_returns401WhenUnauthenticated(@Autowired RestTestClient restClient) {
        restClient
                .get().uri("/api/v1/meetings/{id}", 1L)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
