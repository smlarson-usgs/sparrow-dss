--liquibase formatted sql

--This is for the sparrow_dss schema

--changeset kmschoep:mod_ancil_temp_eda_code
alter table temp_ancil modify (edacode varchar2(30 char));
--rollback alter table temp_ancil modify (edacode varchar2(15 char));


--changeset kmschoep:mod_model_reach_attrib_eda_code
alter table model_reach_attrib modify (edacode varchar2(30 char));
--rollback alter table model_reach_attrib modify (edacode varchar2(15 char));


--changeset kmschoep:mod_ancil_temp_eda_name
alter table temp_ancil modify (edaname varchar2(85 char));
--rollback alter table temp_ancil modify (edaname varchar2(60 char));


--changeset kmschoep:mod_model_reach_attrib_eda_name
alter table model_reach_attrib modify (edaname varchar2(85 char));
--rollback alter table model_reach_attrib modify (edaname varchar2(60 char));