# --- First database schema

# --- !Ups

create sequence s_work_id;
create sequence s_project_id;

create table project (
  id    bigint DEFAULT nextval('s_project_id'),
  name  varchar(255)
);

create table work (
  id    bigint DEFAULT nextval('s_work_id'),
  projectId bigint references project(id),
  startTime timestamp,
  endTime timestamp,
  breakTime int,
  description  varchar(255)
);

create table user (
  email varchar(255) not null primary key,
  password varchar(255) not null
);


# --- !Downs

drop table work;
drop table project;
drop sequence s_work_id;
drop sequence s_project_id;
drop table if exists user;