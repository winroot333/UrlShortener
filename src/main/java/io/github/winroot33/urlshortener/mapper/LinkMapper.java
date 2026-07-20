package io.github.winroot33.urlshortener.mapper;

import io.github.winroot33.urlshortener.dtos.LinkResponse;
import io.github.winroot33.urlshortener.entity.Link;
import io.github.winroot33.urlshortener.util.LinkUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * Mapper для преобразования ссылок в дто для ответов и создания Link объекта из dto
 *
 * @author Mikhail Vasiliev (winroot123@gmail.com)
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class LinkMapper {

    @Autowired
    protected LinkUtils linkUtils;

    @Mapping(target = "shortenedUrl",
            expression = "java(linkUtils.getShortUrlByCode(link.getCode()))")
    @Mapping(target = "shortCode", source = "code")
    public abstract LinkResponse toResponse(Link link);

    public abstract List<LinkResponse> toResponseList(Collection<Link> links);
}
