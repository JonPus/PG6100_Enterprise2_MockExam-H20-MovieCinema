create table movie_info
(
    movie_id    varchar(255) not null,
    movie_title varchar(255) not null,
    directors   varchar(255) not null,
    year        integer      not null check ( year >= 0 ),
    primary key (movie_id)
);