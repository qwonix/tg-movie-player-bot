create database "movie_player";
create schema if not exists "movie_player";

set search_path = "movie_player";

create table if not exists "user"
(
    chat_id  bigint primary key,
    "name"   varchar(64),
    is_admin bool default false
);

create table if not exists movie
(
    id               serial primary key,
    "name"           varchar(200) not null,
    description      text         not null,
    release_date     date         not null,
    duration         interval,
    "language"       varchar(50),
    country          varchar(50),
    telegram_file_id varchar
);

create table if not exists series
(
    id          serial primary key,
    "name"      varchar(200) not null,
    description text         not null,
    country     varchar(50)
);

/*
create table if not exists series_details
(
    series_id serial primary key,
    genre     varchar(20)[],
    director  varchar(60)[],
    script    varchar(60)[]
);
*/

create table if not exists season
(
    id                    serial primary key,
    number                smallint not null,
    description           text     not null,
    premiere_release_date date     not null,
    final_release_date    date     not null,
    series_id             int references series
);

create table if not exists episode
(
    telegram_file_id char(70) primary key,
    number           smallint     not null,
    "name"           varchar(200) not null,
    description      text         not null,
    release_date     date         not null,
    "language"       varchar(50),
    country          varchar(50),
    duration         interval,
    season_id        int references season
);
