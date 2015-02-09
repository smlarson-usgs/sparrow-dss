--liquibase formatted sql

--This is for the sparrow_dss schema

--changeset kmschoep:mod_ancil_temp_eda_code
alter table temp_ancil modify (edacode varchar2(15 char));
--rollback alter table temp_ancil modify (edacode varchar2(10 char));


--changeset kmschoep:mod_model_reach_attrib_eda_code
alter table model_reach_attrib modify (edacode varchar2(15 char));
--rollback alter table model_reach_attrib modify (edacode varchar2(10 char));