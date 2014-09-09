--liquibase formatted sql

--This is for the sparrow_dss schema

--changeset kmschoep:addColumnOld_Identifier
ALTER TABLE MODEL_REACH ADD COLUMN (OLD_IDENTIFIER number(10,0));
--rollback update model_reach set identifier = old_identifier;
--rollback alter table MODEL_REACH drop COLUMN OLD_IDENTIFIER;

--changeset kmschoep:copyIDENTIFIERtoOLD_IDENTIFIER
--preconditions onFail:HALT onError:HALT
UPDATE MODEL_REACH
SET OLD_IDENTIFIER = IDENTIFIER
WHERE
      identifier is not null;
--rollback select null from dual;  

--changeset kmschoep:modIDENTIFIER10digits
--preconditions onFail:HALT onError:HALT
ALTER TABLE MODEL_REACH MODIFY (IDENTIFIER number(10,0));
--rollback alter table MODEL_REACH modify (identifier number(9,0));


--changeset kmschoep:MarkModelsAsFullIdSame
--preconditions onFail:HALT onError:HALT
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM SPARROW_DSS.MODEL_REACH where IDENTIFIER != TO_NUMBER(full_identifier);
UPDATE SPARROW_MODEL SET IS_ID_FULLID_SAME = 'T';
--rollback select null from dual;