-- foreign keys and indices
-- SQLite cannot add a foreign key to an existing table (the generator left a "not supported"
-- placeholder here). Rebuild `image_download` with the `pin_id` foreign key the association now
-- declares, preserving every column, its rows and the primary key. No index on `pin_id`: it is
-- the primary key, which SQLite already indexes.
create table image_download_tmp_rebuild (
  pin_id                        uuid not null,
  source_url                    text not null,
  status                        text not null,
  reason_code                   text,
  last_error                    text,
  task_id                       uuid not null,
  requested_at                  timestamp not null,
  updated_at                    timestamp not null,
  constraint pk_image_download primary key (pin_id),
  foreign key (pin_id) references pins (id) on delete restrict on update restrict
);
insert into image_download_tmp_rebuild (pin_id, source_url, status, reason_code, last_error, task_id, requested_at, updated_at)
  select pin_id, source_url, status, reason_code, last_error, task_id, requested_at, updated_at
  from image_download;
drop table image_download;
alter table image_download_tmp_rebuild rename to image_download;
