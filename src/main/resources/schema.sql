create table IF NOT EXISTS GENRE
(
    GENRE_ID   INTEGER not null,
    GENRE_NAME VARCHAR(63) not null,
    constraint GENRE_PK
        primary key (GENRE_ID)
);

create table IF NOT EXISTS MPA
(
    MPA_ID   INTEGER not null,
    MPA_NAME VARCHAR(63) not null,
    constraint MPA_PK
        primary key (MPA_ID)
);

create table IF NOT EXISTS FILMS
(
    FILM_ID          INTEGER AUTO_INCREMENT,
    FILM_NAME        VARCHAR(63)      not null,
    FILM_DESCRIPTION VARCHAR(255) not null,
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
    USER_EMAIL VARCHAR(255) not null,
    USER_NAME  VARCHAR(64) not null,
    USER_LOGIN VARCHAR(32) not null,
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

create table IF NOT EXISTS DIRECTORS
(
    DIRECTOR_ID   INTEGER AUTO_INCREMENT,
    DIRECTOR_NAME VARCHAR(127) not null,
    constraint DIRECTORS_PK
        primary key (DIRECTOR_ID)
);

create table IF NOT EXISTS FILMS_DIRECTORS
(
    FILM_ID     INTEGER not null,
    DIRECTOR_ID INTEGER not null,
    constraint "FILMS_ID_fk"
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE,
    constraint "DIRECTORS_fk"
        foreign key (DIRECTOR_ID) references DIRECTORS ON DELETE CASCADE
);

create table IF NOT EXISTS REVIEWS
(
    REVIEW_ID   INTEGER AUTO_INCREMENT,
    CONTENT     VARCHAR(16383) not null,
    IS_POSITIVE BOOLEAN not null,
    USER_ID     INTEGER not null,
    FILM_ID     INTEGER not null,
    USEFUL      INTEGER DEFAULT 0,
    constraint REVIEWS_PK
        primary key (REVIEW_ID),
    constraint "REVIEWS_USERS_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE,
    constraint "REVIEWS_FILMS_fk"
        foreign key (FILM_ID) references FILMS ON DELETE CASCADE
);

create table IF NOT EXISTS REVIEWS_MARK
(
    MARK_ID   INTEGER AUTO_INCREMENT,
    REVIEW_ID INTEGER not null,
    USER_ID   INTEGER not null,
    MARK      INT2    not null,
    constraint uq_marks UNIQUE (REVIEW_ID, USER_ID),
    constraint REVIEWS_MARK_PK
        primary key (MARK_ID),
    constraint "REVIEWS_MARK_USERS_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE,
    constraint "REVIEWS_MARK_REVIEWS_fk"
        foreign key (REVIEW_ID) references REVIEWS ON DELETE CASCADE
);

create table IF NOT EXISTS EVENTS
(
    EVENT_ID INTEGER AUTO_INCREMENT,
    TIME_EVENT TIMESTAMP DEFAULT CURRENT_TIMESTAMP(),
    USER_ID INTEGER NOT NULL,
    EVENT_TYPE VARCHAR(64) NOT NULL,
    OPERATION VARCHAR(255) NOT NULL,
    ENTITY_ID INTEGER NOT NULL,
    constraint EVENTS_PK
        primary key (EVENT_ID),
    constraint "EVENTS_USERS_fk"
        foreign key (USER_ID) references USERS ON DELETE CASCADE
);
