--liquibase formatted sql

--This is for the sparrow_dss schema

--logicalFilePath: changeLog11AlterColumn.sql

--changeset lmurphy:altercolumna
alter table sparrow_model modify (name varchar2 (150 char));
--rollback alter table sparrow_model modify (name varchar2 (40 byte));