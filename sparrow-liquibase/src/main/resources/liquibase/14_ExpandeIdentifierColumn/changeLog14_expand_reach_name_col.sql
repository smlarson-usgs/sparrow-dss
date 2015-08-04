--liquibase formatted sql

--This is for the sparrow_dss schema

--logicalFilePath: changeLog14_expand_reach_name_col.sql

--changeset kmschoep:mod_ancil_temp_pname
alter table temp_ancil modify (pname varchar2(65 char));
--rollback alter table temp_ancil modify (pname varchar2(60 char));

--changeset kmschoep:mod_model_reach_attrib_reach_name
alter table model_reach_attrib modify (reach_name varchar2(65 char));
--rollback alter table model_reach_attrib modify (reach_name varchar2(60 char));

--changeset kmschoep:mod_model_reach_attrib_swap_reach_name
alter table model_reach_attrib_swap modify (reach_name varchar2(65 char));
--rollback alter table model_reach_attrib_swap modify (reach_name varchar2(60 char));
