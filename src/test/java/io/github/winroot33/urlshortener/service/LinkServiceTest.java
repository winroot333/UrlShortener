package io.github.winroot33.urlshortener.service;

import io.github.winroot33.urlshortener.entity.Link;
import io.github.winroot33.urlshortener.exceptions.LinkAlreadyExistsException;
import io.github.winroot33.urlshortener.exceptions.LinkExpiredException;
import io.github.winroot33.urlshortener.exceptions.LinkNotFoundException;
import io.github.winroot33.urlshortener.repository.LinkRepository;
import io.github.winroot33.urlshortener.util.LinkUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Link service tests")
public class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private LinkUtils linkUtils;

    @InjectMocks
    private LinkService linkService;

    private Link createMockLink(UUID id) {
        return Link.builder()
                .id(id)
                .originalUrl("https://example.com")
                .code("abc123")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    @DisplayName("Find all links with pagination should return list of links")
    void findAll_ShouldReturnListOfLinks() {
        int page = 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);

        List<Link> links = List.of(
                createMockLink(UUID.randomUUID()),
                createMockLink(UUID.randomUUID())
        );
        Page<Link> pageResult = new PageImpl<>(links, pageable, links.size());

        when(linkRepository.findAll(pageable)).thenReturn(pageResult);

        List<Link> result = linkService.findAll(page, size);

        assertThat(result).hasSize(2)
                .isEqualTo(links);
        verify(linkRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Find all links with pagination should return empty list when no links found")
    void findAll_ShouldReturnEmptyList_WhenNoLinksFound() {
        int page = 0;
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);
        Page<Link> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(linkRepository.findAll(pageable)).thenReturn(emptyPage);

        List<Link> result = linkService.findAll(page, size);

        assertThat(result).isEmpty();
        verify(linkRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Add link with generated code when code is null should create new link")
    void addLink_ShouldGenerateCode_WhenCodeIsNull() {
        String url = "https://example.com";
        String generatedCode = "gen123";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        Link expectedLink = new Link();
        expectedLink.setCode(generatedCode);
        expectedLink.setOriginalUrl(url);
        expectedLink.setExpiresAt(expiresAt);

        when(linkUtils.generateUniqueCode()).thenReturn(generatedCode);
        when(linkRepository.save(any(Link.class))).thenReturn(expectedLink);

        Link result = linkService.addLink(url, null, expiresAt);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(generatedCode);
        assertThat(result.getOriginalUrl()).isEqualTo(url);
        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
        verify(linkUtils, times(1)).generateUniqueCode();
        verify(linkRepository, times(1)).save(any(Link.class));
        verify(linkRepository, never()).existsByCode(anyString());
    }

    @Test
    @DisplayName("Add link with provided code should create new link")
    void addLink_ShouldCreateLink_WhenCodeIsProvided() {
        String url = "https://example.com";
        String code = "abc123";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        Link expectedLink = new Link();
        expectedLink.setCode(code);
        expectedLink.setOriginalUrl(url);
        expectedLink.setExpiresAt(expiresAt);

        when(linkRepository.existsByCode(code)).thenReturn(false);
        when(linkRepository.save(any(Link.class))).thenReturn(expectedLink);

        Link result = linkService.addLink(url, code, expiresAt);

        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getOriginalUrl()).isEqualTo(url);
        assertThat(result.getExpiresAt()).isEqualTo(expiresAt);
        verify(linkRepository, times(1)).existsByCode(code);
        verify(linkRepository, times(1)).save(any(Link.class));
        verify(linkUtils, never()).generateUniqueCode();
    }

    @Test
    @DisplayName("Add link with existing code should throw LinkAlreadyExistsException")
    void addLink_ShouldThrowException_WhenCodeAlreadyExists() {
        String url = "https://example.com";
        String code = "abc123";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        when(linkRepository.existsByCode(code)).thenReturn(true);

        assertThatThrownBy(() -> linkService.addLink(url, code, expiresAt))
                .isInstanceOf(LinkAlreadyExistsException.class)
                .hasMessage("Link already exists for code: " + code);

        verify(linkRepository, times(1)).existsByCode(code);
        verify(linkRepository, never()).save(any(Link.class));
    }

    @Test
    @DisplayName("Update link should update existing link")
    void updateLink_ShouldUpdateLink_WhenLinkExists() {
        UUID id = UUID.randomUUID();
        String newUrl = "https://updated.com";
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(2);

        Link existingLink = createMockLink(id);
        Link updatedLink = createMockLink(id);
        updatedLink.setOriginalUrl(newUrl);
        updatedLink.setExpiresAt(newExpiresAt);

        when(linkRepository.findById(id)).thenReturn(Optional.of(existingLink));
        when(linkRepository.save(any(Link.class))).thenReturn(updatedLink);

        Link result = linkService.updateLink(id, newUrl, newExpiresAt);

        assertThat(result).isNotNull();
        assertThat(result.getOriginalUrl()).isEqualTo(newUrl);
        assertThat(result.getExpiresAt()).isEqualTo(newExpiresAt);
        assertThat(result.getId()).isEqualTo(id);
        verify(linkRepository, times(1)).findById(id);
        verify(linkRepository, times(1)).save(existingLink);
    }

    @Test
    @DisplayName("Update link should throw LinkNotFoundException when link does not exist")
    void updateLink_ShouldThrowException_WhenLinkNotFound() {
        UUID id = UUID.randomUUID();
        String newUrl = "https://updated.com";
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(2);

        when(linkRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.updateLink(id, newUrl, newExpiresAt))
                .isInstanceOf(LinkNotFoundException.class)
                .hasMessage("Link not found for id: " + id);

        verify(linkRepository, times(1)).findById(id);
        verify(linkRepository, never()).save(any(Link.class));
    }

    @Test
    @DisplayName("Remove link should delete link when exists")
    void removeLink_ShouldDeleteLink_WhenLinkExists() {
        UUID id = UUID.randomUUID();

        when(linkRepository.existsById(id)).thenReturn(true);

        linkService.removeLink(id);

        verify(linkRepository, times(1)).existsById(id);
        verify(linkRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Remove link should throw LinkNotFoundException when link does not exist")
    void removeLink_ShouldThrowException_WhenLinkNotFound() {
        UUID id = UUID.randomUUID();

        when(linkRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> linkService.removeLink(id))
                .isInstanceOf(LinkNotFoundException.class)
                .hasMessage("Link not found with id: " + id);

        verify(linkRepository, times(1)).existsById(id);
        verify(linkRepository, never()).deleteById(id);
    }

    @Test
    @DisplayName("Get original url should return url when link exists and not expired")
    void getOriginalUrl_ShouldReturnUrl_WhenLinkExistsAndNotExpired() {
        String code = "abc123";
        UUID id = UUID.randomUUID();
        String originalUrl = "https://example.com";

        Link link = createMockLink(id);
        link.setCode(code);
        link.setOriginalUrl(originalUrl);

        when(linkRepository.findByCode(code)).thenReturn(Optional.of(link));
        when(linkUtils.isExpired(link)).thenReturn(false);

        String result = linkService.getOriginalUrl(code);

        assertThat(result).isEqualTo(originalUrl);
        verify(linkRepository, times(1)).findByCode(code);
        verify(linkUtils, times(1)).isExpired(link);
    }

    @Test
    @DisplayName("Get original url should throw LinkNotFoundException when link does not exist")
    void getOriginalUrl_ShouldThrowException_WhenLinkNotFound() {
        String code = "abc123";

        when(linkRepository.findByCode(code)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkService.getOriginalUrl(code))
                .isInstanceOf(LinkNotFoundException.class)
                .hasMessage("Link not found for code: " + code);

        verify(linkRepository, times(1)).findByCode(code);
        verify(linkUtils, never()).isExpired(any(Link.class));
    }

    @Test
    @DisplayName("Get original url should throw LinkExpiredException when link is expired")
    void getOriginalUrl_ShouldThrowException_WhenLinkExpired() {
        String code = "abc123";
        UUID id = UUID.randomUUID();

        Link link = createMockLink(id);
        link.setCode(code);

        when(linkRepository.findByCode(code)).thenReturn(Optional.of(link));
        when(linkUtils.isExpired(link)).thenReturn(true);

        assertThatThrownBy(() -> linkService.getOriginalUrl(code))
                .isInstanceOf(LinkExpiredException.class)
                .hasMessage("Link expired for code: " + code);

        verify(linkRepository, times(1)).findByCode(code);
        verify(linkUtils, times(1)).isExpired(link);
    }
}