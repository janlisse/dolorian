# --- First database schema

# --- !Ups

create sequence s_work_item_id;
create sequence s_project_id;
create sequence s_invoice_template_id;

create table project (
  id bigint DEFAULT nextval('s_project_id') PRIMARY KEY,
  number varchar(50),
  description varchar(255)
);

create table work_item (
  id bigint DEFAULT nextval('s_work_item_id') PRIMARY KEY ,
  project_id bigint references project(id),
  start_time timestamp,
  end_time timestamp,
  break_time int,
  description  varchar(255)
);

create table invoice_template (
  id bigint DEFAULT nextval('s_invoice_template_id') PRIMARY KEY ,
  project_id bigint references project(id),
  template_file varchar(255),
  hourly_rate decimal,
  invoice_number varchar(50)
);

create table app_user (
  name varchar(255) not null,
  email varchar(255) not null primary key,
  password varchar(255) not null,
  UNIQUE(email, password)
);


# --- !Downs

drop table if exists work_item;
drop table if exists invoice_template;
drop table if exists project;
drop sequence s_work_item_id;
drop sequence s_project_id;
drop sequence s_invoice_template_id;
drop table if exists app_user;
