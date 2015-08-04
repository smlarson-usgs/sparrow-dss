--liquibase formatted sql

--This is for the stream_network schema


--logicalFilePath: changeLog14_StreamNetworkAddOldIDCol.sql


--changeset kmschoep:ERaddColumnOld_Identifier3
ALTER TABLE ENH_REACH ADD (OLD_IDENTIFIER number(10,0));
--rollback select 'the rollback for this is very complicated on a compressed table' from dual;

--changeset kmschoep:ERcopyIDENTIFIERtoOLD_IDENTIFIER
--preconditions onFail:HALT onError:HALT
UPDATE ENH_REACH
SET OLD_IDENTIFIER = IDENTIFIER
WHERE
      identifier is not null;
--rollback select null from dual; 