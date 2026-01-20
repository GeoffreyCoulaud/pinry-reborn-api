-- apply changes
create table pins (
  id                            uuid not null,
  author_id                     uuid not null,
  source_context_url            text not null,
  source_media_url              text not null,
  description                   text not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_pins primary key (id),
  foreign key (author_id) references users (id) on delete restrict on update restrict
);

create table pin_tag_model (
  pin_id                        uuid not null,
  tag_id                        uuid not null,
  foreign key (pin_id) references pins (id) on delete restrict on update restrict,
  foreign key (tag_id) references tags (id) on delete restrict on update restrict
);

create table tags (
  id                            uuid not null,
  author_id                     uuid not null,
  name                          text not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_tags primary key (id),
  foreign key (author_id) references users (id) on delete restrict on update restrict
);

create table users (
  id                            uuid not null,
  name                          text not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_users primary key (id)
);

create table user_password_hashes (
  id                            uuid not null,
  user_id                       uuid not null,
  hash                          text not null,
  algorithm                     text(6) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_user_password_hashes_algorithm check ( algorithm in ('BCRYPT')),
  constraint pk_user_password_hashes primary key (id),
  foreign key (user_id) references users (id) on delete restrict on update restrict
);

