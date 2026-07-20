package io.github.winroot33.urlshortener;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.winroot33.urlshortener.dtos.LinkCreateDto;
import io.github.winroot33.urlshortener.dtos.LinkResponse;
import io.github.winroot33.urlshortener.entity.Link;
import io.github.winroot33.urlshortener.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = UrlShortenerApplication.class)
@AutoConfigureMockMvc
@Transactional
@Testcontainers
@DisplayName("Integration tests for link shortening full cycle")
public class UrlShortenerApplicationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private LinkRepository linkRepository;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    public void cleanDb() {
        linkRepository.deleteAll();
    }

    private LinkCreateDto createValidLinkCreateDtoNoTtl() {
        return new LinkCreateDto(
                "https://example.com",
                "noTtl123",
                null
        );
    }

    private LinkCreateDto createLinkCreateDtoWithTtlNoCode() {
        return new LinkCreateDto(
                "https://example-with-ttl.com",
                null,
                LocalDateTime.now().plusHours(2)
        );
    }


    @Test
    @DisplayName("Should create short link and verify it in database")
    void shouldCreateShortLinkAndPersistInDatabase() throws Exception {
        LinkCreateDto createDto = createValidLinkCreateDtoNoTtl();

        String responseJson = mockMvc.perform(post("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(createDto.url()))
                .andExpect(jsonPath("$.shortCode").value(createDto.code()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LinkResponse response = objectMapper.readValue(responseJson, LinkResponse.class);
        UUID linkId = response.id();

        Link savedLink = linkRepository.findById(linkId)
                .orElseThrow(() -> new AssertionError("Link not found in database"));

        assertThat(savedLink.getOriginalUrl()).isEqualTo(createDto.url());
        assertThat(savedLink.getCode()).isEqualTo(createDto.code());
        assertThat(savedLink.getId()).isEqualTo(linkId);
    }

    @Test
    @DisplayName("Should create short link and redirect successfully")
    void shouldCreateShortLinkAndRedirectSuccessfully() throws Exception {
        LinkCreateDto createDto = createValidLinkCreateDtoNoTtl();

        String responseJson = mockMvc.perform(post("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(createDto.url()))
                .andExpect(jsonPath("$.shortCode").value(createDto.code()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        LinkResponse response = objectMapper.readValue(responseJson, LinkResponse.class);
        String shortCode = response.shortCode();

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(createDto.url()));
    }

    @Test
    @DisplayName("Should create short link with TTL and verify expiration time")
    void shouldCreateShortLinkWithTtlAndVerifyExpiration() throws Exception {
        LinkCreateDto createDto = createLinkCreateDtoWithTtlNoCode();
        LocalDateTime expectedExpiration = createDto.expirationDate();

        String responseJson = mockMvc.perform(post("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalUrl").value(createDto.url()))
                .andExpect(jsonPath("$.shortCode").isString())
                .andReturn()
                .getResponse()
                .getContentAsString();

        LinkResponse response = objectMapper.readValue(responseJson, LinkResponse.class);
        Link savedLink = linkRepository.findById(response.id())
                .orElseThrow(() -> new AssertionError("Link not found"));

        assertThat(savedLink.getExpiresAt())
                .isCloseTo(expectedExpiration, within(1, java.time.temporal.ChronoUnit.SECONDS));
        assertThat(savedLink.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should retrieve all links from database with pagination")
    void shouldRetrieveAllLinksWithPagination() throws Exception {
        Link link1 = Link.builder()
                .originalUrl("https://first-link.com")
                .code("first1")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();

        Link link2 = Link.builder()
                .originalUrl("https://second-link.com")
                .code("second2")
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        linkRepository.saveAll(List.of(link1, link2));

        mockMvc.perform(get("/links")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].originalUrl").value("https://first-link.com"))
                .andExpect(jsonPath("$[0].shortCode").value("first1"))
                .andExpect(jsonPath("$[1].originalUrl").value("https://second-link.com"))
                .andExpect(jsonPath("$[1].shortCode").value("second2"));

        List<Link> allLinksFromDb = linkRepository.findAll();
        assertThat(allLinksFromDb).hasSize(2);
    }
}
