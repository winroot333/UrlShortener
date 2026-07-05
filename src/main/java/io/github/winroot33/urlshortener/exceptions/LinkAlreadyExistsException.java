package io.github.winroot33.urlshortener.exceptions;

/**
 * Исключение когда запрошенная ссылка уже существует
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
public class LinkAlreadyExistsException extends RuntimeException {
    public LinkAlreadyExistsException(String message) {
        super(message);
    }
}
