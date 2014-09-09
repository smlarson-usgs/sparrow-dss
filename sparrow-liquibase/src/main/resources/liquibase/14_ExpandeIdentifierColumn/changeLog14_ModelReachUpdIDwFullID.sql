--liquibase formatted sql

--This is for the sparrow_dss schema

--changeset kmschoep:updateIDENTIFIERwithFULL_IDENTIFIER
UPDATE SPARROW_DSS.MODEL_REACH
SET IDENTIFIER = TO_NUMBER(FULL_IDENTIFIER)
WHERE
      full_identifier is not null
  AND decode(REGEXP_INSTR (full_identifier, '[^[:digit:]]'),0,'NUMBER','NOT_NUMBER') = 'NUMBER';
--rollback select null from dual;