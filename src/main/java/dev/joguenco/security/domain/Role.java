package dev.joguenco.security.domain;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

@MappedEntity
public record Role(@Id String id, String string) {}
