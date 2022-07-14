package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@Data
public class Series {
    @EqualsAndHashCode.Include
    private int id;
    private String name;
    private String description;
    private String country;
}
