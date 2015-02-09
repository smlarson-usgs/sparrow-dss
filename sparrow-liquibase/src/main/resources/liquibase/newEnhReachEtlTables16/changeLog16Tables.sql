--liquibase formatted sql

--This is for the stream_network schema
 
--changeset kmschoep:tables16a
create table temp_enh_reach
(
  enh_reach_id     number(10),
  enh_network_id   number(10),
  nom_reach_id     number(10),
  identifier       number(10),
  full_identifier  varchar2(100 byte) ,
  hydseq           number(10),
  fnode            number(10),
  tnode            number(10),
  frac             number(10,9),
  reach_size       number(1),
  head_reach       number(1),
  shore_reach      number(1),
  term_trans       number(1),
  term_estuary     number(1),
  term_nonconnect  number(1),
  edaname          varchar2(50 byte),
  edacda           varchar2(5 byte),
  old_identifier   number(10)
)
nologging 
nocache
noparallel
monitoring;
--rollback drop table temp_enh_reach;

--changeset kmschoep:tables16b
create table temp_enh_reach_attrib
(
  enh_reach_id     number(10)           ,
  reach_name       varchar2(60 byte),
  open_water_name  varchar2(60 byte),
  meanq            number(10,4),
  meanv            number(4,2),
  catch_area       number(9,3),
  cum_catch_area   number(10,3),
  reach_length     number(10,3),
  huc2             varchar2(2 byte),
  huc4             varchar2(4 byte),
  huc6             varchar2(6 byte),
  huc8             varchar2(8 byte),
  huc2_name        varchar2(60 byte),
  huc4_name        varchar2(60 byte),
  huc6_name        varchar2(60 byte),
  huc8_name        varchar2(60 byte)
)
nologging 
nocache
noparallel
monitoring;
--rollback drop table temp_enh_reach_attrib;

--changeset kmschoep:add_col_enh_reach
alter table enh_reach add (mrb_no number);
--rollback select 'cannot drop col from compressed table' from dual;


