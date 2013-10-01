# --- First database schema

# --- !Ups

create sequence s_work_id;
create sequence s_project_id;
create sequence s_invoice_template_id;

create table project (
  id bigint DEFAULT nextval('s_project_id') PRIMARY KEY,
  name  varchar(255),
  number varchar(50)
);

create table work (
  id bigint DEFAULT nextval('s_work_id') PRIMARY KEY ,
  projectId bigint references project(id),
  startTime timestamp,
  endTime timestamp,
  breakTime int,
  description  varchar(255)
);

create table invoice_template (
  id bigint DEFAULT nextval('s_invoice_template_id') PRIMARY KEY ,
  projectId bigint references project(id),
  templateFile varchar(255),
  hourlyRate decimal,
  invoiceNumber varchar(50)
);

create table app_user (
  name varchar(255) not null,
  email varchar(255) not null primary key,
  password varchar(255) not null,
  UNIQUE(email, password)
);


# --- !Downs

drop table if exists work;
drop table if exists invoice_template;
drop table if exists project;
drop sequence s_work_id;
drop sequence s_project_id;
drop sequence s_invoice_template_id;
drop table if exists app_user;
