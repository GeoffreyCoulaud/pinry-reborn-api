-- apply changes
create table pins (
  id                            varchar(40) not null,
  author_id                     varchar(40) not null,
  source_url                    varchar(255) not null,
  media_url                     varchar(255) not null,
  description                   varchar(255) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_pins primary key (id),
  foreign key (author_id) references users (id) on delete restrict on update restrict
);

create table pin_tag_model (
  pin_id                        varchar(40) not null,
  tag_id                        varchar(40) not null,
  foreign key (pin_id) references pins (id) on delete restrict on update restrict,
  foreign key (tag_id) references tags (id) on delete restrict on update restrict
);

create table tags (
  id                            varchar(40) not null,
  author_id                     varchar(40) not null,
  name                          varchar(255) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_tags primary key (id),
  foreign key (author_id) references users (id) on delete restrict on update restrict
);

create table user_password_hashes (
  id                            varchar(40) not null,
  user_id                       varchar(40) not null,
  hash                          varchar(255) not null,
  algorithm                     integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_user_password_hashes_algorithm check ( algorithm in (0)),
  constraint pk_user_password_hashes primary key (id),
  foreign key (user_id) references users (id) on delete restrict on update restrict
);

