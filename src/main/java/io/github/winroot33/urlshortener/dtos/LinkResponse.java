package io.github.winroot33.urlshortener.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ДТО с ответом для получения информации о ссылке
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@Builder
@Schema(description = "Response object containing shortened link information")
public record LinkResponse(
        @Schema(
                description = "Unique identifier of the link",
                example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UUID id,
        @Schema(
                description = "Short code/key for the shortened link",
                example = "test11",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 1,
                maxLength = 255
        )
        String shortCode,
        @Schema(
                description = "Full shortened url that can be used for redirection",
                example = "http://localhost:8080/test11",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String shortenedUrl,
        @Schema(
                description = "The original long url that was shortened",
                example = "https://example.com/very/long/url/path?param=value",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String originalUrl,
        @Schema(
                description = "Expiration date and time of the link. If null, the link never expires.",
                example = "2026-12-31T23:59:59",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED,
                nullable = true
        )
        LocalDateTime expiresAt

) {
}
