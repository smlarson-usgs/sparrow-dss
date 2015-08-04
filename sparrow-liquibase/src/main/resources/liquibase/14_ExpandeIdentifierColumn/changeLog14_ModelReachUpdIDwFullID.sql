--liquibase formatted sql

--This is for the sparrow_dss schema

--logicalFilePath: changeLog14_ModelReachUpdIDwFullID.sql

--changeset kmschoep:updateIDENTIFIERwithFULL_IDENTIFIER
UPDATE SPARROW_DSS.MODEL_REACH
SET IDENTIFIER = TO_NUMBER(FULL_IDENTIFIER)
WHERE
      full_identifier is not null
  AND decode(REGEXP_INSTR (full_identifier, '[^[:digit:]]'),0,'NUMBER','NOT_NUMBER') = 'NUMBER';
--rollback select null from dual;
  
  
--changeset kmschoep:MarkModelsAsFullIdSame
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM SPARROW_DSS.MODEL_REACH where IDENTIFIER != TO_NUMBER(full_identifier);
UPDATE SPARROW_MODEL SET IS_ID_FULLID_SAME = 'T';
--rollback update sparrow_model set IS_ID_FULLID_SAME = 'F' where enh_network_id = 43;