package io.github.winroot33.urlshortener.exceptions;

/**
 * Исключение когда запрошенная ссылка просрочена
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
public class LinkExpiredException extends RuntimeException {
    public LinkExpiredException(String message) {
        super(message);
    }
}
