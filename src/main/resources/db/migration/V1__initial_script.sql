create sequence hibernate_sequence start 1 increment 1;
create table author
(
    id         int8 not null,
    uuid       varchar(255),
    version    int8,
    created_at timestamp,
    name       varchar(255),
    primary key (id)
);
create table book
(
    id         int8 not null,
    uuid       varchar(255),
    version    int8,
    available  int8,
    cover_id   int8,
    crated_at  timestamp,
    price      numeric(19, 2),
    title      varchar(255),
    updated_at timestamp,
    year       int4,
    primary key (id)
);
create table book_authors
(
    books_id   int8 not null,
    authors_id int8 not null,
    primary key (books_id, authors_id)
);
create table order_item
(
    id       int8 not null,
    uuid     varchar(255),
    version  int8,
    quantity int4 not null,
    book_id  int8,
    order_id int8,
    primary key (id)
);
create table orders
(
    id           int8 not null,
    uuid         varchar(255),
    version      int8,
    created_at   timestamp,
    delivery     varchar(255),
    status       varchar(255),
    updated_at   timestamp,
    recipient_id int8,
    primary key (id)
);
create table recipient
(
    id       int8 not null,
    uuid     varchar(255),
    version  int8,
    city     varchar(255),
    email    varchar(255),
    name     varchar(255),
    phone    varchar(255),
    street   varchar(255),
    zip_code varchar(255),
    primary key (id)
);
create table upload
(
    id           int8 not null,
    uuid         varchar(255),
    version      int8,
    content_type varchar(255),
    created_at   timestamp,
    file         bytea,
    filename     varchar(255),
    primary key (id)
);
create table useres_roles
(
    user_id int8 not null,
    role    varchar(255)
);
create table users
(
    id         int8 not null,
    uuid       varchar(255),
    version    int8,
    created_at timestamp,
    password   varchar(255),
    updated_at timestamp,
    username   varchar(255),
    primary key (id)
);
alter table if exists book add constraint UK_g0286ag1dlt4473st1ugemd0m unique (title);
alter table if exists book_authors add constraint FK551i3sllw1wj7ex6nir16blsm foreign key (authors_id) references author;
alter table if exists book_authors add constraint FKmuhqocx8etx13u6jrtutnumek foreign key (books_id) references book;
alter table if exists order_item add constraint FKb033an1f8qmpbnfl0a6jb5njs foreign key (book_id) references book;
alter table if exists order_item add constraint FKt4dc2r9nbvbujrljv3e23iibt foreign key (order_id) references orders;
alter table if exists orders add constraint FKcxwo1jbmo15jih4b5qjclvye8 foreign key (recipient_id) references recipient;
alter table if exists useres_roles add constraint FKo7o6ulb97w5nc4wkwswo104uj foreign key (user_id) references users;
