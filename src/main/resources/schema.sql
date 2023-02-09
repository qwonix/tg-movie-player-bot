create database "movie_player";
create schema if not exists "movie_player";

set search_path = "movie_player";

create table if not exists "user"
(
    chat_id         bigint primary key,
    "name"          varchar(64),
    is_admin        bool default false,
    state           varchar(100),
    tg_messages_ids json
);

create table if not exists series
(
    id                 serial primary key,
    "order"            int          not null,
    "name"             varchar(200) not null,
    description        text         not null,
    country            varchar(50),
    tg_preview_file_id varchar(100)
);

create table if not exists season
(
    id                   serial primary key,
    number               smallint not null,
    description          text     not null,
    total_episodes_count int      not null,
    series_id            int references series,
    tg_preview_file_id   varchar(100)
);

create table if not exists video
(
    id                 serial primary key,
    resolution         int  not null,
    audio_language     varchar(100),
    subtitles_language varchar(50),
    video_file_id      text not null,
    priority           int,
    episode_id         int references episode
);

create table if not exists episode
(
    id                 serial primary key,
    number             smallint     not null,
    production_code    int unique,
    title              varchar(200) not null,
    description        text         not null,
    release_date       date         not null,
    "language"         varchar(50),
    country            varchar(50),
    duration           interval,
    season_id          int references season,
    tg_preview_file_id varchar(100)
);