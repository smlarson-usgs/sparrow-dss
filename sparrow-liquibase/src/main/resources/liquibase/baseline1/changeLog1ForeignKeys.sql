--liquibase formatted sql

--This is for the sparrow_dss schema
 
--logicalFilePath: changeLog1ForeignKeys.sql

--changeset drsteini:foreignKeys1a
alter table model_reach add constraint model_reach_enh_reach_fk foreign key (enh_reach_id) references stream_network.enh_reach (enh_reach_id);
--rollback alter table model_reach drop constraint model_reach_enh_reach_fk;
  
--changeset drsteini:foreignKeys1b
alter table model_reach add constraint model_reach_sparrow_model_fk foreign key (sparrow_model_id) references sparrow_model (sparrow_model_id) on delete cascade;
--rollback alter table model_reach drop constraint model_reach_sparrow_model_fk;
  
--changeset drsteini:foreignKeys1c
alter table model_reach_attrib add constraint model_reach_attrib_model__fk1 foreign key (model_reach_id) references model_reach (model_reach_id);
--rollback alter table model_reach_attrib drop constraint model_reach_attrib_model__fk1;
  
--changeset drsteini:foreignKeys1d
alter table model_reach_geom add constraint model_reach_geom_fk foreign key (model_reach_id) references model_reach (model_reach_id) on delete cascade;
--rollback alter table model_reach_geom drop constraint model_reach_geom_fk;
  
--changeset drsteini:foreignKeys1e
alter table model_reach_watershed add constraint model_reach_id_fk foreign key (model_reach_id) references model_reach (model_reach_id);
--rollback alter table model_reach_watershed drop constraint model_reach_id_fk;

--changeset drsteini:foreignKeys1f
alter table model_reach_watershed add constraint watershed_id_fk foreign key (watershed_id) references predefined_watershed (watershed_id);
--rollback alter table model_reach_watershed drop constraint watershed_id_fk;
  
--changeset drsteini:foreignKeys1g
alter table reach_coef add constraint reach_ceof_model_reach_fk foreign key (model_reach_id) references model_reach (model_reach_id) on delete cascade;
--rollback alter table reach_coef drop constraint reach_ceof_model_reach_fk;
  
--changeset drsteini:foreignKeys1h
alter table source add constraint source_sparrow_model_fk foreign key (sparrow_model_id) references sparrow_model (sparrow_model_id) on delete cascade;
--rollback alter table source drop constraint source_sparrow_model_fk;
  
--changeset drsteini:foreignKeys1i
alter table source_reach_coef add constraint source_reach_coef_fk foreign key (model_reach_id) references model_reach (model_reach_id) on delete cascade;
--rollback alter table source_reach_coef drop constraint source_reach_coef_fk;

--changeset drsteini:foreignKeys1j
alter table source_reach_coef add constraint source_reach_coef_source_fk foreign key (source_id) references source (source_id) on delete cascade;
--rollback alter table source_reach_coef drop constraint source_reach_coef_source_fk;
  
--changeset drsteini:foreignKeys1k
alter table source_value add constraint source_value_model_reach_fk foreign key (model_reach_id) references model_reach (model_reach_id) on delete cascade;
--rollback alter table source_value drop constraint source_value_model_reach_fk;

--changeset drsteini:foreignKeys1l
alter table source_value add constraint source_value_source_fk foreign key (source_id) references source (source_id) on delete cascade;
--rollback alter table source_value drop constraint source_value_source_fk;
  