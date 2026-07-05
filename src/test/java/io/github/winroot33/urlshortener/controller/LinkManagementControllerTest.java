package io.github.winroot33.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.winroot33.urlshortener.dtos.LinkCreateDto;
import io.github.winroot33.urlshortener.dtos.LinkResponse;
import io.github.winroot33.urlshortener.entity.Link;
import io.github.winroot33.urlshortener.exceptions.GlobalExceptionHandler;
import io.github.winroot33.urlshortener.exceptions.LinkNotFoundException;
import io.github.winroot33.urlshortener.mapper.LinkMapper;
import io.github.winroot33.urlshortener.service.LinkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LinkManagementController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Link controller tests")
public class LinkManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LinkService linkService;

    @MockitoBean
    private LinkMapper linkMapper;

    private Link createMockLink(UUID id) {
        return Link.builder()
                .id(id)
                .originalUrl("https://example.com")
                .code("abc123")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    private LinkResponse createMockLinkResponse(UUID id) {
        return LinkResponse.builder()
                .id(id)
                .originalUrl("https://example.com")
                .shortCode("abc123")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    private LinkCreateDto createMockLinkCreateDto() {
        return new LinkCreateDto(
                "https://example.com",
                "abc123",
                LocalDateTime.now().plusDays(1)
        );
    }

    @Test
    @DisplayName("Get all links with pagination")
    void getAllLinks() throws Exception {
        List<Link> links = List.of(
                createMockLink(UUID.randomUUID()),
                createMockLink(UUID.randomUUID())
        );
        List<LinkResponse> responses = List.of(
                createMockLinkResponse(UUID.randomUUID()),
                createMockLinkResponse(UUID.randomUUID())
        );

        when(linkService.findAll(0, 20)).thenReturn(links);
        when(linkMapper.toResponseList(links)).thenReturn(responses);

        mockMvc.perform(get("/links")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Create link successfully")
    void createLink() throws Exception {
        LinkCreateDto dto = createMockLinkCreateDto();
        Link savedLink = createMockLink(UUID.randomUUID());
        LinkResponse response = createMockLinkResponse(savedLink.getId());

        when(linkService.addLink(dto.URL(), dto.code(), dto.expirationDate())).thenReturn(savedLink);
        when(linkMapper.toResponse(savedLink)).thenReturn(response);

        mockMvc.perform(post("/links")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedLink.getId().toString()));
    }

    @Test
    @DisplayName("Delete link successfully")
    void deleteLink() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(linkService).removeLink(id);

        mockMvc.perform(delete("/links/{id}", id))
                .andExpect(status().isNoContent());

        verify(linkService).removeLink(id);
    }

    @Test
    @DisplayName("Update link successfully")
    void updateLink() throws Exception {
        UUID id = UUID.randomUUID();
        LinkCreateDto dto = createMockLinkCreateDto();
        Link updatedLink = createMockLink(id);
        LinkResponse response = createMockLinkResponse(id);

        when(linkService.updateLink(id, dto.URL(), dto.expirationDate())).thenReturn(updatedLink);
        when(linkMapper.toResponse(updatedLink)).thenReturn(response);

        mockMvc.perform(put("/links/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @DisplayName("Return bad request when page is negative")
    void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/links")
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Return bad request when link id is invalid")
    void shouldReturnBadRequestWhenLinkIdIsInvalid() throws Exception {
        mockMvc.perform(delete("/links/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Return not found when link does not exist")
    void shouldReturnNotFoundWhenLinkDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();

        doThrow(new LinkNotFoundException("Link not found for id: " + id))
                .when(linkService).removeLink(id);

        mockMvc.perform(delete("/links/{id}", id))
                .andExpect(status().isNotFound());
    }
}