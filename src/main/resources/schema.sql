create table IF NOT EXISTS GENRE
(
    GENRE_ID   INTEGER not null,
    GENRE_NAME VARCHAR not null,
    constraint GENRE_PK
        primary key (GENRE_ID)
);

create table IF NOT EXISTS MPA
(
    MPA_ID   INTEGER not null,
    MPA_NAME VARCHAR not null,
    constraint MPA_PK
        primary key (MPA_ID)
);

create table IF NOT EXISTS FILMS
(
    FILM_ID          INTEGER AUTO_INCREMENT,
    FILM_NAME        VARCHAR      not null,
    FILM_DESCRIPTION VARCHAR(200) not null,
    RELEASE_DATE     DATE         not null,
    DURATION         INTEGER      not null,
    MPA_ID           INTEGER      not null,
    LIKES            INTEGER DEFAULT 0,
    constraint FILMS_PK
        primary key (FILM_ID),
    constraint "FILMS_MPA_null_fk"
        foreign key (MPA_ID) references MPA
);

create table IF NOT EXISTS FILMS_GENRE
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint "FILMS_GENRE_fk"
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint "FILMS_GENRE_GENRE_null_fk"
        foreign key (GENRE_ID) references GENRE ON DELETE CASCADE
);

create table IF NOT EXISTS USERS
(
    USER_ID    INTEGER AUTO_INCREMENT,
    USER_EMAIL VARCHAR not null,
    USER_NAME  VARCHAR not null,
    USER_LOGIN VARCHAR not null,
    BIRTHDAY   DATE    not null,
    constraint USERS_PK
        primary key (USER_ID)
);

create table IF NOT EXISTS FILMS_LIKES
(
    FILM_ID INTEGER not null,
    USER_ID INTEGER not null,
    constraint uq_likes UNIQUE (FILM_ID, USER_ID),
    constraint "FILMS_LIKES_fk"
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint "FILMS_LIKES_USER_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);

create table IF NOT EXISTS FRIENDS
(
    USER_ID   INTEGER not null,
    FRIEND_ID INTEGER not null,
    constraint "USER_FRIENDS_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE,
    constraint "FRIEND_USER_fk"
        foreign key (FRIEND_ID) references USERS (USER_ID) ON DELETE CASCADE
);
