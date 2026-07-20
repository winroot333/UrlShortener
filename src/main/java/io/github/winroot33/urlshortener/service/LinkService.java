package io.github.winroot33.urlshortener.service;

import io.github.winroot33.urlshortener.entity.Link;
import io.github.winroot33.urlshortener.exceptions.LinkAlreadyExistsException;
import io.github.winroot33.urlshortener.exceptions.LinkExpiredException;
import io.github.winroot33.urlshortener.exceptions.LinkNotFoundException;
import io.github.winroot33.urlshortener.repository.LinkRepository;
import io.github.winroot33.urlshortener.util.LinkUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы со ссылками
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final LinkUtils linkUtils;

    /**
     * Метод для получения всех коротких ссылок с пагинацией
     *
     * @param page страница
     * @param size размер записей на странице
     * @return список коротких ссылок
     */
    @Transactional(readOnly = true)
    public List<Link> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return linkRepository.findAll(pageable).getContent();
    }

    /**
     * Добавление короткой ссылки
     *
     * @param url       оригинальный URL для переадресации
     * @param code      название, код для ссылки, nullable
     * @param expiresAt срок действия ссылки, nullable
     * @return добавленная ссылка
     */
    @Transactional
    public Link addLink(@NotNull String url, String code, LocalDateTime expiresAt) {
        if (code == null) {
            code = linkUtils.generateUniqueCode();
        } else {
            if (linkRepository.existsByCode(code)) {
                throw new LinkAlreadyExistsException("Link already exists for code: " + code);
            }
        }

        Link link = new Link();
        link.setCode(code);
        link.setOriginalUrl(url);
        link.setExpiresAt(expiresAt);
        return linkRepository.save(link);
    }

    /**
     * Обновление короткой ссылки
     *
     * @param url       оригинальный URL для переадресации
     * @param expiresAt срок действия ссылки, nullable
     * @return добавленная ссылка
     */
    @Transactional
    public Link updateLink(@NotNull UUID id, @NotNull String url, LocalDateTime expiresAt) {

        Link link = linkRepository.findById(id).orElseThrow(
                () -> new LinkNotFoundException("Link not found for id: " + id));

        link.setOriginalUrl(url);
        link.setExpiresAt(expiresAt);
        return linkRepository.save(link);
    }

    /**
     * Метод для удаления ссылки по id
     *
     * @param id ID ссылки для удаления
     */
    @Transactional
    public void removeLink(UUID id) {
        if (!linkRepository.existsById(id)) {
            throw new LinkNotFoundException("Link not found with id: " + id);
        }
        linkRepository.deleteById(id);
    }

    /**
     * Метод для получения оригинального URL по корткому коду для переадресации
     *
     * @param code код для получения URL
     * @return Оригинальный URL
     */
    @Transactional(readOnly = true)
    public String getOriginalUrl(String code) {
        Link link = linkRepository.findByCode(code).orElseThrow(
                () -> new LinkNotFoundException("Link not found for code: " + code)
        );

        if (linkUtils.isExpired(link)) {
            throw new LinkExpiredException("Link expired for code: " + code);
        }

        return link.getOriginalUrl();
    }
}
