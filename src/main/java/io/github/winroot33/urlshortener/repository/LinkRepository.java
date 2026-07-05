package io.github.winroot33.urlshortener.repository;

import io.github.winroot33.urlshortener.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий для работы со ссылками
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
public interface LinkRepository extends JpaRepository<Link, UUID> {
    Optional<Link> findByCode(String code);

    boolean existsByCode(String code);
}
