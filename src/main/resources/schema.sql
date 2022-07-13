create database "movie_player";
create schema if not exists "movie_player";

set search_path = "movie_player";

create table if not exists "user"
(
    chat_id  bigint primary key,
    name     varchar(64),
    is_admin bool default false
);

