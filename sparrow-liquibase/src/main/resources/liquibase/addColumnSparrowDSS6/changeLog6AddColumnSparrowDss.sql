--liquibase formatted sql

--This is for the sparrow_dss schema

--logicalFilePath: changeLog6AddColumnSparrowDss.sql

--changeset lmurphy:addcolumnsa
ALTER TABLE SPARROW_MODEL ADD (WATERSHED_CALC_VALID  CHAR(1 CHAR));
--rollback alter table sparrow_model drop column watershed_calc_valid;




