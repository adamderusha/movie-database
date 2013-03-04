# Movie Schema

# --- !Ups
CREATE TABLE movie (
    id SERIAL,
    title varchar(500) NOT NULL,
    year smallint NOT NULL,
    poster text,

    PRIMARY KEY (id),
    UNIQUE(title, year)
);

CREATE TABLE genre (
    movie_id int references movie(id) ON DELETE CASCADE,
    genre varchar(20) NOT NULL
);

# --- !Downs
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS movie;
