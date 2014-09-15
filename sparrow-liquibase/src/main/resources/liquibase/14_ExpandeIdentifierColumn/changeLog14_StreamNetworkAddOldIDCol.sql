--liquibase formatted sql

--This is for the stream_network schema

--changeset kmschoep:ERaddColumnOld_Identifier
ALTER TABLE ENH_REACH ADD (OLD_IDENTIFIER number(10,0));
--rollback update enh_reach set identifier = old_identifier;
--rollback alter table ENH_REACH drop COLUMN OLD_IDENTIFIER;

--changeset kmschoep:ERcopyIDENTIFIERtoOLD_IDENTIFIER
--preconditions onFail:HALT onError:HALT
UPDATE ENH_REACH
SET OLD_IDENTIFIER = IDENTIFIER
WHERE
      identifier is not null;
--rollback select null from dual; 