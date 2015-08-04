--liquibase formatted sql

--This is for the sparrow_dss schema
  
--logicalFilePath: changeLog14_ModelReachAddOldIDCol.sql

--changeset kmschoep:addColumnOld_Identifier
ALTER TABLE MODEL_REACH ADD (OLD_IDENTIFIER number(10,0));
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
--rollback select null from dual;


--changeset kmschoep:addSwapColumnOld_Identifier
ALTER TABLE MODEL_REACH_SWAP ADD (OLD_IDENTIFIER number(10,0));
--rollback update model_reach_swap set identifier = old_identifier;
--rollback alter table MODEL_REACH_SWAP drop COLUMN OLD_IDENTIFIER;


--changeset kmschoep:modIDENTIFIER10digitsSwap
--preconditions onFail:HALT onError:HALT
ALTER TABLE MODEL_REACH_SWAP MODIFY (IDENTIFIER number(10,0));
--rollback select null from dual;

