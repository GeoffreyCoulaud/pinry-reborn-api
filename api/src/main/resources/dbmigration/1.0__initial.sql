-- apply changes
create table users (
  id                            varchar(40) not null,
  name                          varchar(255) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_users primary key (id)
);

