package io.github.winroot33.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Сущность ссылки для редиректа с короткого адреса на оригинальный
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Builder
@Table(name = "links")
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

}
