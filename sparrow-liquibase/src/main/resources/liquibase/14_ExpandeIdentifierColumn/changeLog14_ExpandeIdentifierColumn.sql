--liquibase formatted sql

--This is for the sparrow_dss schema

--changeset eeverman:alterIdColumnToTenDigits
ALTER TABLE MODEL_REACH MODIFY (IDENTIFIER number(10,0));
-- ALTER TABLE ENH_REACH MODIFY (IDENTIFIER number(10,0)); --ALREADY IS 10
ALTER TABLE NOM_REACH MODIFY (IDENTIFIER number(10,0));


--changeset eeverman:copyFULL_IDENTIFIERtoIDENTIFIERforSPARROW_DSS
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:0 select COUNT(*) from SPARROW_DSS.MODEL_REACH where full_identifier is null OR decode(REGEXP_INSTR (full_identifier, '[^[:digit:]]'),0,'NUMBER','NOT_NUMBER') = 'NUMBER';

UPDATE SPARROW_DSS.MODEL_REACH SET FULL_IDENTIFIER = TRIM(FULL_IDENTIFIER);

UPDATE SPARROW_DSS.MODEL_REACH
SET IDENTIFIER = TO_NUMBER(FULL_IDENTIFIER)
WHERE
      full_identifier is not null
  AND decode(REGEXP_INSTR (full_identifier, '[^[:digit:]]'),0,'NUMBER','NOT_NUMBER') = 'NUMBER'
  AND IDENTIFIER != TO_NUMBER(FULL_IDENTIFIER);
  
--changeset eeverman:copyFULL_IDENTIFIERtoIDENTIFIERforSTREAM_NETWORK_ENH_REACH
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:0 select COUNT(*) from STREAM_NETWORK.ENH_REACH where full_identifier is null OR decode(REGEXP_INSTR (full_identifier, '[^[:digit:]]'),0,'NUMBER','NOT_NUMBER') = 'NOT_NUMBER';

UPDATE STREAM_NETWORK.ENH_REACH SET FULL_IDENTIFIER = TRIM(FULL_IDENTIFIER);

UPDATE STREAM_NETWORK.ENH_REACH
SET IDENTIFIER = TO_NUMBER(FULL_IDENTIFIER)
WHERE
      full_identifier is not null
  AND decode(REGEXP_INSTR (full_identifier, '[^[:digit:]]'),0,'NUMBER','NOT_NUMBER') = 'NUMBER'
  AND IDENTIFIER != TO_NUMBER(FULL_IDENTIFIER);
  
--changeset eeverman:copyFULL_IDENTIFIERtoIDENTIFIERforSTREAM_NETWORK_NOM_REACH
--preconditions onFail:HALT onError:HALT
--NO PRECONDITIONS FOR NOM_REACH - WE DON'T REALLY CARE ABOUT SOME OF THE EDGE CASES HERE.

UPDATE STREAM_NETWORK.NOM_REACH SET FULL_IDENTIFIER = TRIM(FULL_IDENTIFIER);

UPDATE STREAM_NETWORK.NOM_REACH
SET IDENTIFIER = TO_NUMBER(FULL_IDENTIFIER)
WHERE
      full_identifier is not null
  AND decode(REGEXP_INSTR (full_identifier, '[^[:digit:]]'),0,'NUMBER','NOT_NUMBER') = 'NUMBER'
  AND IDENTIFIER != TO_NUMBER(FULL_IDENTIFIER);

--changeset eeverman:MarkModelsAsFullIdSame
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM SPARROW_DSS.MODEL_REACH where IDENTIFIER != TO_NUMBER(full_identifier);

UPDATE SPARROW_DSS.MODEL_REACH SET IS_ID_FULLID_SAME = 'T';
  
  
--changeset eeverman:rebuildViews




