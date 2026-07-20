package io.github.winroot33.urlshortener.util;

import io.github.winroot33.urlshortener.entity.Link;
import io.github.winroot33.urlshortener.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Утилитный класс для работы со ссылками
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */

@Component
@RequiredArgsConstructor
public class LinkUtils {

    private final LinkRepository linkRepository;
    @Value("${app.base-url}")
    String baseUrl;
    @Value("${app.link.random-code-digits}")
    private int codeDigitsCount;

    /**
     * Метод получения короткого url по коду
     *
     * @param code код для получения короткого url
     * @return короткий url
     */
    public String getShortUrlByCode(String code) {
        return baseUrl + "/" + code;
    }

    /**
     * Метод для проверки истек ли срок действия ссылки
     *
     * @param link ссылка для проверки
     * @return true - просрочена, false - ссылка действительна
     */
    public boolean isExpired(Link link) {
        return link.getExpiresAt() != null && link.getExpiresAt().isBefore(LocalDateTime.now());
    }

    /**
     * Метод для генерации уникального короткого кода для ссылки
     *
     * @return Уникальный короткий код
     */
    public String generateUniqueCode() {
        String code;
        do {
            code = RandomStringUtils.secure().nextAlphanumeric(codeDigitsCount);
        } while (linkRepository.existsByCode(code));

        return code;
    }

}
