package ru.qwonix.tgMoviePlayerBot.entity;

import lombok.Builder;
import lombok.Data;


@Builder(toBuilder = true)
@Data
public class Series {
    private int id;
    private String name;
    private String description;
    private String country;
}
