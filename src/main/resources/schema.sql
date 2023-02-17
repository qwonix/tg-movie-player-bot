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
    title              varchar(150),
    description        text not null,
    preview_tg_file_id varchar(100)
);

create table if not exists series
(
    id                 serial primary key,
    title              varchar(200) not null,
    description        text         not null,
    country            varchar(50),
    preview_tg_file_id varchar(100),
    show_id            int references show
);

create table if not exists season
(
    id                   serial primary key,
    number               smallint not null,
    description          text     not null,
    total_episodes_count int      not null,
    preview_tg_file_id   varchar(100),
    series_id            int references series
);

create table if not exists episode
(
    id                 serial primary key,
    number             smallint     not null,
    production_code    int,
    title              varchar(200) not null,
    description        text         not null,
    release_date       date         not null,
    "language"         varchar(50),
    country            varchar(50),
    duration           interval,
    preview_tg_file_id varchar(100),
    season_id          int references season,
    unique (production_code, season_id)
);

create table if not exists video
(
    id                 serial primary key,
    video_tg_file_id   varchar(100) primary key,
    resolution         int not null,
    audio_language     varchar(100),
    subtitles_language varchar(100),
    priority           int
);

create table if not exists episode_video
(
    episode_id int references episode,
    video_id   int references video unique,
    primary key (episode_id, video_id)
);