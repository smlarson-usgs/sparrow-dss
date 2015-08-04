--liquibase formatted sql

--This is for the sparrow_dss schema
       
--logicalFilePath: changeLog1Types.sql

--changeset drsteini:types1a 
CREATE OR REPLACE TYPE SPARROW_DSS.MV_DATELIST as TABLE of DATE;
--rollback DROP TYPE SPARROW_DSS.MV_DATELIST;

--changeset drsteini:types1b
CREATE OR REPLACE TYPE SPARROW_DSS.MV_NUMBERLIST as TABLE of NUMBER;
--rollback DROP TYPE SPARROW_DSS.MV_NUMBERLIST;

--changeset drsteini:types1c
CREATE OR REPLACE TYPE SPARROW_DSS.MV_STRINGLIST as TABLE of VARCHAR2(1000);
--rollback DROP TYPE SPARROW_DSS.MV_STRINGLIST;
