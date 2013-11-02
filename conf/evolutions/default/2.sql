# --- First database schema

# --- !Ups

create sequence s_invoice_id;

create table invoice (
  id bigint DEFAULT nextval('s_invoice_id') PRIMARY KEY ,
  invoice_number varchar(50),
  project_id bigint references project(id),
  invoice_date date,
  total_hours decimal,
  invoice_status varchar(20)
);


# --- !Downs

drop table if exists invoice;
drop sequence s_invoice_id;

