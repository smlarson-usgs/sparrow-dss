--liquibase formatted sql

--This is for the sparrow_dss schema
   
--logicalFilePath: changeLog2Renames.sql

--changeset drsteini:renames2a
alter table model_reach rename to model_reach_old;
--rollback alter table model_reach_old rename to model_reach;

--changeset drsteini:renames2b
alter table model_reach_old rename constraint model_reach_pk to model_reach_pk_old;
--rollback alter table model_reach_old rename constraint model_reach_pk_old to model_reach_pk;

--changeset drsteini:renames2c
alter table model_reach_old rename constraint model_reach_uk_identifier to model_reach_uk_identifier_old;
--rollback alter table model_reach_old rename constraint model_reach_uk_identifier_old to model_reach_uk_identifier;

--changeset drsteini:renames2d
alter table model_reach_old rename constraint model_reach_rsize_chk to model_reach_rsize_chk_old;
--rollback alter table model_reach_old rename constraint model_reach_rsize_chk_old to model_reach_rsize_chk;

--changeset drsteini:renames2e
alter table model_reach_old rename constraint model_reach_enh_reach_fk to model_reach_enh_reach_fk_old;
--rollback alter table model_reach_old rename constraint model_reach_enh_reach_fk_old to model_reach_enh_reach_fk;

--changeset drsteini:renames2f
alter table model_reach_old rename constraint model_reach_sparrow_model_fk to model_reach_sprw_model_fk_old;
--rollback alter table model_reach_old rename constraint model_reach_sprw_model_fk_old to model_reach_sparrow_model_fk;

--changeset drsteini:renames2h
alter index model_reach_enh_reach_fk_i rename to model_reach_enh_reach_fk_i_old;
--rollback alter index model_reach_enh_reach_fk_i_old rename to model_reach_enh_reach_fk_i;

--changeset drsteini:renames2i
alter index model_reach_fnode_i rename to model_reach_fnode_i_old;
--rollback alter index model_reach_fnode_i_old rename to model_reach_fnode_i;

--changeset drsteini:renames2j
alter index model_reach_hydseq_i rename to model_reach_hydseq_i_old;
--rollback alter index model_reach_hydseq_i_old rename to model_reach_hydseq_i;

--changeset drsteini:renames2k
alter index model_reach_rsize_i rename to model_reach_rsize_i_old;
--rollback alter index model_reach_rsize_i_old rename to model_reach_rsize_i;

--changeset drsteini:renames2l
alter index model_reach_tnode_i rename to model_reach_tnode_i_old;
--rollback alter index model_reach_tnode_i_old rename to model_reach_tnode_i;

--changeset drsteini:renames2m
alter index model_reach_pk rename to model_reach_pk_old;
--rollback alter index model_reach_pk_old rename to model_reach_pk;

--changeset drsteini:renames2n
alter index model_reach_uk_identifier rename to model_reach_uk_identifier_old;
--rollback alter index model_reach_uk_identifier_old rename to model_reach_uk_identifier;



--changeset drsteini:renames2o
alter table model_calib_sites rename to model_calib_sites_old;
--rollback alter table model_calib_sites_old rename to model_calib_sites;

--changeset drsteini:renames2p
alter table model_calib_sites_old rename constraint model_calib_sites_pk to model_calib_sites_pk_old;
--rollback alter table model_calib_sites_old rename constraint model_calib_sites_pk_old to model_calib_sites_pk;

--changeset drsteini:renames2q
alter index calib_site_geom_i rename to calib_site_geom_i_old;
--rollback alter index calib_site_geom_i_old rename to calib_site_geom_i;

--changeset drsteini:renames2r
alter index model_calib_sites_pk rename to model_calib_sites_pk_old;
--rollback alter index model_calib_sites_pk_old rename to model_calib_sites_pk;



--changeset drsteini:renames2s
alter table model_reach_attrib rename to model_reach_attrib_old;
--rollback alter table model_reach_attrib_old rename to model_reach_attrib;

--changeset drsteini:renames2t
alter table model_reach_attrib_old rename constraint model_reach_attrib_pk to model_reach_attrib_pk_old;
--rollback alter table model_reach_attrib_old rename constraint model_reach_attrib_pk_old to model_reach_attrib_pk;

--changeset drsteini:renames2u
alter index model_reach_attrib_ow_name_i rename to model_reach_attr_ow_name_i_old;
--rollback alter index model_reach_attr_ow_name_i_old rename to model_reach_attrib_ow_name_i;

--changeset drsteini:renames2v
alter index model_reach_attrib_rname_i rename to model_reach_attrib_rname_i_old;
--rollback alter index model_reach_attrib_rname_i_old rename to model_reach_attrib_rname_i;

--changeset drsteini:renames2w
alter index model_reach_attrib_pk rename to model_reach_attrib_pk_old;
--rollback alter index model_reach_attrib_pk_old rename to model_reach_attrib_pk;



--changeset drsteini:renames2x
alter table reach_coef rename to reach_coef_old;
--rollback alter table reach_coef_old rename to reach_coef;

--changeset drsteini:renames2y
alter table reach_coef_old rename constraint reach_coef_fact_pk to reach_coef_fact_pk_old;
--rollback alter table reach_coef_old rename constraint reach_coef_fact_pk_old to reach_coef_fact_pk;

--changeset drsteini:renames2z
alter table reach_coef_old rename constraint reach_coef_uk_value to reach_coef_uk_value_old;
--rollback alter table reach_coef_old rename constraint reach_coef_uk_value_old to reach_coef_uk_value;

--changeset drsteini:renames2aa
alter index reach_coef_it_i rename to reach_coef_it_i_old;
--rollback alter index reach_coef_it_i_old rename to reach_coef_it_i;

--changeset drsteini:renames2ab
alter index reach_coef_fact_pk rename to reach_coef_fact_pk_old;
--rollback alter index reach_coef_fact_pk_old rename to reach_coef_fact_pk;

--changeset drsteini:renames2ac
alter index reach_coef_uk_value rename to reach_coef_uk_value_old;
--rollback alter index reach_coef_uk_value_old rename to reach_coef_uk_value;

--changeset drsteini:renames2ad
alter table reach_coef_old rename constraint reach_ceof_model_reach_fk to reach_ceof_model_reach_fk_old;
--rollback alter table reach_coef_old rename constraint reach_ceof_model_reach_fk_old to reach_ceof_model_reach_fk;
  


--changeset drsteini:renames2ae
alter table source_reach_coef rename to source_reach_coef_old;
--rollback alter table source_reach_coef_old rename to source_reach_coef;

--changeset drsteini:renames2af
alter table source_reach_coef_old rename constraint source_reach_coef_pk to source_reach_coef_pk_old;
--rollback alter table source_reach_coef_old rename constraint source_reach_coef_pk_old to source_reach_coef_pk;

--changeset drsteini:renames2ag
alter table source_reach_coef_old rename constraint source_reach_coef_uk_value to source_reach_coef_uk_value_old;
--rollback alter table source_reach_coef_old rename constraint source_reach_coef_uk_value_old to source_reach_coef_uk_value;

--changeset drsteini:renames2ah
alter index source_reach_it_i rename to source_reach_it_i_old;
--rollback alter index source_reach_it_i_old rename to source_reach_it_i;

--changeset drsteini:renames2ai
alter index source_reach_coef_pk rename to source_reach_coef_pk_old;
--rollback alter index source_reach_coef_pk_old rename to source_reach_coef_pk;

--changeset drsteini:renames2aj
alter table source_reach_coef_old rename constraint source_reach_coef_fk to source_reach_coef_fk_old;
--rollback alter table source_reach_coef_old rename constraint source_reach_coef_fk_old to source_reach_coef_fk;

--changeset drsteini:renames2ak
alter table source_reach_coef_old rename constraint source_reach_coef_source_fk to src_reach_coef_source_fk_old;
--rollback alter table source_reach_coef_old rename constraint src_reach_coef_source_fk_old to source_reach_coef_source_fk;



--changeset drsteini:renames2al
alter table source_value rename to source_value_old;
--rollback alter table source_value_old rename to source_value;

--changeset drsteini:renames2am
alter table source_value_old rename constraint source_value_pk to source_value_pk_old;
--rollback alter table source_value_old rename constraint source_value_pk_old to source_value_pk;

--changeset drsteini:renames2an
alter table source_value_old rename constraint source_value_uk_value to source_value_uk_value_old;
--rollback alter table source_value_old rename constraint source_value_uk_value_old to source_value_uk_value;

--changeset drsteini:renames2ao
alter index source_value_pk rename to source_value_pk_old;
--rollback alter index source_value_pk_old rename to source_value_pk;

--changeset drsteini:renames2ap
alter index source_value_uk_value rename to source_value_uk_value_old;
--rollback alter index source_value_uk_value_old rename to source_value_uk_value;

--changeset drsteini:renames2aq
alter table source_value_old rename constraint source_value_model_reach_fk to src_value_model_reach_fk_old;
--rollback alter table source_value_old rename constraint src_value_model_reach_fk_old to source_value_model_reach_fk;

--changeset drsteini:renames2ar
alter table source_value_old rename constraint source_value_source_fk to source_value_source_fk_old;
--rollback alter table source_value_old rename constraint source_value_source_fk_old to source_value_source_fk;
