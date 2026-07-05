package io.github.winroot33.urlshortener.controller;

import io.github.winroot33.urlshortener.dtos.LinkCreateDto;
import io.github.winroot33.urlshortener.dtos.LinkResponse;
import io.github.winroot33.urlshortener.dtos.LinkUpdateDto;
import io.github.winroot33.urlshortener.entity.Link;
import io.github.winroot33.urlshortener.mapper.LinkMapper;
import io.github.winroot33.urlshortener.service.LinkService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер для добавления, редактирования ссылок
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/links")
public class LinkManagementController implements LinkManagementOpenApi {
    private final LinkService linkService;
    private final LinkMapper linkMapper;

    @Override
    @GetMapping
    public List<LinkResponse> findAll(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page could not be less than 0")
            @Max(value = 500, message = "page could not be more than 500")
            int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size could not be less than 1")
            @Max(value = 500, message = "size could not be more than 500")
            int size
    ) {

        return linkMapper.toResponseList(
                linkService.findAll(page, size)
        );
    }

    @Override
    @PostMapping
    public LinkResponse create(@RequestBody @Valid LinkCreateDto dto) {
        Link savedLink = linkService.addLink(dto.URL(), dto.code(), dto.expirationDate());
        return linkMapper.toResponse(savedLink);
    }

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteLink(@PathVariable
                           @NotNull(message = "Link id should not be null")
                           UUID id) {
        linkService.removeLink(id);
    }

    @Override
    @PutMapping("/{id}")
    public LinkResponse updateLink(@PathVariable
                                   @NotNull(message = "Link id should not be null")
                                   UUID id,
                                   @RequestBody @Valid LinkUpdateDto dto) {
        Link updatedLink = linkService.updateLink(id, dto.URL(), dto.expirationDate());
        return linkMapper.toResponse(updatedLink);
    }

}
