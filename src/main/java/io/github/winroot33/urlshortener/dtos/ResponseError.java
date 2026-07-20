package io.github.winroot33.urlshortener.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "Model of API error")
public class ResponseError {

    @Schema(
            description = "Error List",
            example = "url cannot be blank"
    )
    private List<String> errors;

    public ResponseError(String message) {
        errors = new ArrayList<>(List.of(message));
    }
}
