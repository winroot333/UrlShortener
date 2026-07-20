package io.github.winroot33.urlshortener.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * ДТО для обновления короткой ссылки
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@Schema(description = "DTO for updating an existing link")
public record LinkUpdateDto(


        @Schema(
                description = "The url to be shortened",
                example = "https://example.com/very/long/url/to/be/shortened",
                minLength = 2,
                maxLength = 1000,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Size(min = 2, max = 1000, message = "url should be between 2 and 1000 symbols")
        @URL(message = "Should be valid url")
        String url,

        @Schema(
                description = "Expiration date and time for the shortened link. If not provided, the link will never expire.",
                example = "2026-12-31T23:59:59",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Future(message = "Expiration date should be in the future")
        LocalDateTime expirationDate

) {
}