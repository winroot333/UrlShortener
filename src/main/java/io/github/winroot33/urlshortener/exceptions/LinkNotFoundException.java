package io.github.winroot33.urlshortener.exceptions;

/**
 * Исключение когда необходимая ссылка не найдена
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String message) {
        super(message);
    }
}
