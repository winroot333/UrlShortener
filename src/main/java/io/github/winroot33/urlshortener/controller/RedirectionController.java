package io.github.winroot33.urlshortener.controller;


import io.github.winroot33.urlshortener.service.LinkService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Контроллер для переадресации коротких ссылок
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@RestController
@RequiredArgsConstructor
public class RedirectionController {
    private final LinkService linkService;

    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(@PathVariable String shortCode,
                                      HttpServletResponse response) throws IOException {
        String originalUrl = linkService.getOriginalUrl(shortCode);
        response.sendRedirect(originalUrl);
    }
}
