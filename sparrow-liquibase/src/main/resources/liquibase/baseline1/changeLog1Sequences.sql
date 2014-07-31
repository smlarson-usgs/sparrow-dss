--liquibase formatted sql

--This is for the sparrow_dss schema
 
--changeset drsteini:sequences1a
create sequence model_reach_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence model_reach_seq;
  
--changeset drsteini:sequences1b
create sequence predefined_watershed_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence predefined_watershed_seq;

--changeset drsteini:sequences1c
create sequence reach_coef_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence reach_coef_seq;

--changeset drsteini:sequences1d
create sequence source_reach_coef_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence source_reach_coef_seq;

--changeset drsteini:sequences1e
create sequence source_reach_predict_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence source_reach_predict_seq;

--changeset drsteini:sequences1f
create sequence source_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence source_seq;
  
--changeset drsteini:sequences1g
create sequence source_value_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence source_value_seq;
  
--changeset drsteini:sequences1h
create sequence sparrow_model_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence sparrow_model_seq;
  
--changeset drsteini:sequences1i
create sequence total_predict_seq
  start with 1
  maxvalue 999999999999999999999999999
  minvalue 1
  nocycle
  cache 20
  order;
--rollback drop sequence total_predict_seq;
