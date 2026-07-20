package io.github.winroot33.urlshortener.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

/**
 * ДТО для генерации короткой ссылки с указанием имени
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@Schema(description = "Data transfer object for creating a new shortened link")
public record LinkCreateDto(

        @Schema(
                description = "The url to be shortened",
                example = "https://example.com/very/long/url/to/be/shortened",
                minLength = 2,
                maxLength = 1000,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "url cannot be blank")
        @Size(min = 2, max = 1000, message = "url should be between 2 and 1000 symbols")
        @URL(message = "Should be valid url")
        String url,

        @Schema(
                description = "Custom short code for the link. If not provided, a random code will be generated.",
                example = "myCustomCode",
                minLength = 4,
                maxLength = 32,
                pattern = "^[a-zA-Z0-9]+$",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Size(min = 4, max = 32, message = "Code should be between 4 and 32 symbols")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Code must contain only letters and digits")
        String code,

        @Schema(
                description = "Expiration date and time for the shortened link. If not provided, the link will never expire.",
                example = "2026-12-31T23:59:59",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        @Future(message = "Expiration date should be in the future")
        LocalDateTime expirationDate

) {
}
