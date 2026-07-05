package io.github.winroot33.urlshortener.controller;


import io.github.winroot33.urlshortener.dtos.LinkCreateDto;
import io.github.winroot33.urlshortener.dtos.LinkResponse;
import io.github.winroot33.urlshortener.dtos.LinkUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Класс для описания OpenAPI контроллера
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@Tag(
        name = "Link Management",
        description = "API for managing short links"
)
public interface LinkManagementOpenApi {

    @Operation(
            summary = "Get all links",
            description = "Returns a paginated list of links",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Page number, starting from 0",
                            example = "0",
                            in = ParameterIn.QUERY
                    ),
                    @Parameter(
                            name = "size",
                            description = "Number of links per page",
                            example = "20",
                            in = ParameterIn.QUERY
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Links successfully returned",
                            content = @Content(
                                    array = @ArraySchema(schema = @Schema(implementation = LinkResponse.class))
                            )
                    )
            }
    )
    List<LinkResponse> findAll(
            @Min(value = 0, message = "page could not be less than 0")
            @Max(value = 500, message = "page could not be more than 500")
            int page,
            @Min(value = 1, message = "size could not be less than 1")
            @Max(value = 500, message = "size could not be more than 500")
            int size
    );

    @Operation(
            summary = "Create a link",
            description = "Creates a new short link",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Link successfully created",
                            content = @Content(schema = @Schema(implementation = LinkResponse.class))
                    )
            }
    )
    LinkResponse create(
            @RequestBody(
                    required = true,
                    description = "Link creation payload",
                    content = @Content(schema = @Schema(implementation = LinkCreateDto.class))
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid LinkCreateDto dto
    );

    @Operation(
            summary = "Delete a link",
            description = "Deletes a link by its identifier",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Link identifier",
                            example = "550e8400-e29b-41d4-a716-446655440000",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Link successfully deleted"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Link not found"
                    )
            }
    )
    void deleteLink(
            @NotNull(message = "Link id should not be null") UUID id);

    @Operation(
            summary = "Update a link",
            description = "Updates an existing link by its identifier",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Link identifier",
                            example = "550e8400-e29b-41d4-a716-446655440000",
                            required = true,
                            in = ParameterIn.PATH
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Link successfully updated",
                            content = @Content(schema = @Schema(implementation = LinkResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Link not found"
                    )
            }
    )
    LinkResponse updateLink(
            @NotNull(message = "Link id should not be null")
            UUID id,
            @RequestBody(
                    required = true,
                    description = "Link update payload",
                    content = @Content(schema = @Schema(implementation = LinkCreateDto.class))
            )
            @org.springframework.web.bind.annotation.RequestBody @Valid LinkUpdateDto dto
    );
}