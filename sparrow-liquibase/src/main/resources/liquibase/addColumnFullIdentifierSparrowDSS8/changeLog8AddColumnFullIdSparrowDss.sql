--liquibase formatted sql

--This is for the sparrow_dss schema

--changeset lmurphy:addcolumnsa
ALTER TABLE SPARROW_MODEL ADD (IS_ID_FULLID_SAME  CHAR(1 CHAR));
--rollback alter table sparrow_model drop column is_id_fullid_same;




