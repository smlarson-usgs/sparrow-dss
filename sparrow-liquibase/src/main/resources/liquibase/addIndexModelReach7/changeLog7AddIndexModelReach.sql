--liquibase formatted sql

--This is for the sparrow_dss schema

--logicalFilePath: changeLog7AddIndexModelReach.sql

--changeset lmurphy:addindexa

CREATE INDEX SPARROW_DSS.MODEL_REACH_FULL_IDENTIFIER ON SPARROW_DSS.MODEL_REACH
(FULL_IDENTIFIER);
--rollback DROP INDEX SPARROW_DSS.MODEL_REACH_FULL_IDENTIFIER;




