create sequence hibernate_sequence start with 1 increment by 1;

create table room_copy
(
    id               bigint       not null,
    room_id          varchar(255),
    number_of_copies integer      not null check ( number_of_copies >= 0 ),
    user_user_id     varchar(255) not null,
    primary key (id)
);

create table user_data
(
    user_id    varchar(255) not null,
    room_packs integer      not null check ( room_packs >= 0 ),
    coins      integer      not null check ( coins >= 0 ),
    primary key (user_id)
);

alter table room_copy
    add constraint FKtco9dei78cocpwi1sxye9mw3b foreign key (user_user_id) references user_data;