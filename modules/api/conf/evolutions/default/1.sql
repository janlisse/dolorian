# --- First database schema

# --- !Ups

create sequence s_work_item_id;
create sequence s_project_id;
create sequence s_template_id;
create sequence s_customer_id;
create sequence s_invoice_id;

create table template (
  id bigint DEFAULT nextval('s_template_id') PRIMARY KEY ,
  name varchar(255),
  key varchar(255),
  type varchar(20)
);

create table customer (
  id bigint DEFAULT nextval('s_customer_id') PRIMARY KEY ,
  name varchar(255),
  short_name char(3),
  street varchar(255),
  street_number varchar(255),
  city varchar(255),
  zip_code varchar(255),
  invoice_sequence int DEFAULT 0
);

create table project (
  id bigint DEFAULT nextval('s_project_id') PRIMARY KEY,
  number varchar(50),
  description varchar(255),
  customer_id bigint references customer(id),
  invoice_template_id bigint references template(id),
  report_template_id bigint references template(id),
  hourly_rate decimal
);

create table work_item (
  id bigint DEFAULT nextval('s_work_item_id') PRIMARY KEY ,
  project_id bigint references project(id),
  start_time timestamp,
  end_time timestamp,
  break_time int,
  duration bigint,
  date date,
  description  varchar(255)
);

create table app_user (
  name varchar(255) not null,
  email varchar(255) not null primary key,
  password varchar(255) not null,
  UNIQUE(email, password)
);

create table invoice (
  id bigint DEFAULT nextval('s_invoice_id') PRIMARY KEY ,
  invoice_number varchar(50),
  project_id bigint references project(id),
  invoice_date date,
  total_hours decimal,
  invoice_status varchar(20)
);


# --- !Downs

drop table if exists work_item;
drop table if exists project;
drop table if exists template;
drop table if exists customer;
drop sequence s_work_item_id;
drop sequence s_project_id;
drop sequence s_template_id;
drop sequence s_customer_id;
drop table if exists app_user;
drop table if exists invoice;
drop sequence s_invoice_id;
