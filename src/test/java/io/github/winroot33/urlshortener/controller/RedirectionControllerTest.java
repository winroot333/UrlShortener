package io.github.winroot33.urlshortener.controller;

import io.github.winroot33.urlshortener.exceptions.GlobalExceptionHandler;
import io.github.winroot33.urlshortener.exceptions.LinkExpiredException;
import io.github.winroot33.urlshortener.exceptions.LinkNotFoundException;
import io.github.winroot33.urlshortener.service.LinkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RedirectionController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Redirection controller tests")
public class RedirectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LinkService linkService;

    @Test
    @DisplayName("Redirect to original URL should redirect when short code exists")
    void redirectToOriginalUrl_ShouldRedirect_WhenShortCodeExists() throws Exception {
        String shortCode = "abc123";
        String originalUrl = "https://example.com";

        when(linkService.getOriginalUrl(shortCode)).thenReturn(originalUrl);

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(originalUrl));

        verify(linkService, times(1)).getOriginalUrl(shortCode);
    }

    @Test
    @DisplayName("Redirect to original URL should return 404 when short code not found")
    void redirectToOriginalUrl_ShouldReturn404_WhenShortCodeNotFound() throws Exception {
        String shortCode = "invalid";
        String errorMessage = "Link not found for code: " + shortCode;

        when(linkService.getOriginalUrl(shortCode))
                .thenThrow(new LinkNotFoundException(errorMessage));

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]").value(errorMessage))
                .andExpect(jsonPath("$.errors.length()").value(1));

        verify(linkService, times(1)).getOriginalUrl(shortCode);
    }

    @Test
    @DisplayName("Redirect to original URL should return 410 when link is expired")
    void redirectToOriginalUrl_ShouldReturn410_WhenLinkExpired() throws Exception {
        String shortCode = "expired";
        String errorMessage = "Link expired for code: " + shortCode;

        when(linkService.getOriginalUrl(shortCode))
                .thenThrow(new LinkExpiredException(errorMessage));

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.errors[0]").value(errorMessage))
                .andExpect(jsonPath("$.errors.length()").value(1));

        verify(linkService, times(1)).getOriginalUrl(shortCode);
    }
}