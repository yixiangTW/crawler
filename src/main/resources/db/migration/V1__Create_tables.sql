create table NEWS(
id bigint primary key auto_increment,
title text,
content text,
url varchar(1000),
created_time timestamp default now(),
modified_time timestamp default now()
) DEFAULT CHARSET=utf8mb4;

CREATE TABLE LINKS_TO_BE_PROCESSED (
    LINK VARCHAR(1000)
);

CREATE TABLE LINKS_ALREADY_PROCESSED (
    LINK VARCHAR(1000)
)