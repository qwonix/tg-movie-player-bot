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

create table if not exists show
(
    id                 serial primary key,
    title              varchar(150) not null,
    description        text         not null,
    preview_tg_file_id varchar(100) not null
);

create table if not exists movie
(
    id                 serial primary key,
    title              varchar(150)        not null,
    description        text                not null,
    preview_tg_file_id varchar(100)        not null,
    show_id            int references show not null
);

create table if not exists series
(
    id                 serial primary key,
    title              varchar(200)        not null,
    description        text                not null,
    country            varchar(50),
    preview_tg_file_id varchar(100)        not null,
    show_id            int references show not null
);
create table if not exists season
(
    id                   serial primary key,
    number               smallint              not null,
    description          text                  not null,
    total_episodes_count int                   not null,
    preview_tg_file_id   varchar(100)          not null,
    series_id            int references series not null
);

create table if not exists episode
(
    id                 serial primary key,
    number             smallint              not null,
    production_code    int,
    title              varchar(200)          not null,
    description        text                  not null,
    release_date       date                  not null,
    "language"         varchar(50),
    country            varchar(50),
    duration           interval,
    preview_tg_file_id varchar(100)          not null,
    season_id          int references season not null,
    unique (production_code, season_id)
);

create table if not exists video
(
    id                 serial primary key,
    video_tg_file_id   varchar(100) not null unique,
    resolution         int          not null,
    audio_language     varchar(100),
    subtitles_language varchar(100),
    priority           int          not null
);

create table if not exists video_entity
(
    video_id    int references video not null unique,
    entity_id   int                  not null,
    entity_type varchar(30)          not null
);