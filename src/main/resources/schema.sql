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
    tg_preview_file_id char(100)
);

create table if not exists season
(
    id                    serial primary key,
    number                smallint not null,
    description           text     not null,
    premiere_release_date date     not null,
    final_release_date    date     not null,
    total_episodes_count  int      not null,
    series_id             int references series,
    tg_preview_file_id    char(100)
);

create table if not exists episode
(
    id                 serial primary key,
    number             smallint     not null,
    title              varchar(200) not null,
    description        text         not null,
    release_date       date         not null,
    "language"         varchar(50),
    country            varchar(50),
    duration           interval,
    season_id          int references season,
    tg_video_file_id   char(100),
    tg_preview_file_id char(100)
);
