--liquibase formatted sql

--This is for the sparrow_dss schema
    
--logicalFilePath: changeLog3Tables.sql

--changeset drsteini:tables3a
create table etl_parameters
(mrb							varchar2(40 char)							constraint nn_etl_parameters_pk_a not null
,constituent					varchar2(10 char)							constraint nn_etl_parameters_pk_b not null
,number_of_sources				number										constraint nn_etl_parameters_srcs not null
,iteration						number										constraint nn_etl_parameters_iter not null
,sparrow_model_id				number										constraint nn_etl_parameters_model not null
,enh_network_id					number										constraint nn_etl_parameters_network not null
,constraint etl_parameters_pk
   primary key (mrb, constituent)
);
--rollback drop table etl_parameters cascade constraints purge;

--changeset drsteini:tables3b
create table model_calib_sites_swap
(model_reach_id					number										constraint nn_model_calib_sites_swp_pk not null
,station_name					varchar2(60 char)
,actual							number
,site_geom						mdsys.sdo_geometry
,latitude						number
,longitude						number
,predict						number
,station_id						varchar2(25 char)
,sparrow_model_id				number(9)									constraint nn_model_calib_sites_sw_mdl_id not null
,constraint model_calib_sites_swp_pk
   primary key (model_reach_id)
);
--rollback drop table model_calib_sites_swap cascade constraints purge;

--changeset drsteini:tables3c
create table model_reach_attrib_swap
(model_reach_id					number(9)									constraint nn_model_reach_attrib_swp_pk not null
,reach_name						varchar2(60 char)
,open_water_name				varchar2(60 char)
,meanq							number(16,6)
,meanv							number(16,6)
,catch_area						number(16,6)
,cum_catch_area					number(16,6)
,reach_length					number(16)
,huc2							varchar2(2 char)
,huc4							varchar2(4 char)
,huc6							varchar2(6 char)
,huc8							varchar2(8 char)
,head_reach						number(1)
,shore_reach					number(1)
,term_trans						number(1)
,term_estuary					number(1)
,term_nonconnect				number(1)
,edaname						varchar2(60 char)
,edacode						varchar2(10 char)
,huc2_name						varchar2(60 char)
,huc4_name						varchar2(60 char)
,huc6_name						varchar2(60 char)
,huc8_name						varchar2(60 char)
,sparrow_model_id				number(9)									constraint nn_reach_attrib_swp_mdl_id not null
,constraint model_reach_attrib_swp_pk
   primary key (model_reach_id)
);
--rollback drop table model_reach_attrib_swap cascade constraints purge;

--changeset drsteini:tables3d
create table model_reach_swap
(model_reach_id					number(9)                   				constraint nn_model_reach_swap_pk not null
,identifier						number(9)                   				constraint nn_model_reach_swap_id not null
,full_identifier				varchar2(40 char)           				constraint nn_model_reach_swap_fid not null
,sparrow_model_id				number(9)                   				constraint nn_model_reach_swap_mid not null
,enh_reach_id					number(9)
,hydseq							number(11)                  				constraint nn_model_reach_swap_hydseq not null
,iftran							number(1)                   				constraint nn_model_reach_swap_iftran not null
,fnode							number(9)
,tnode							number(9)
,frac							number(10,9)
,reach_size						number(1)				default 5			constraint nn_model_reach_swap_rsz not null
,mean_pload						number
,se_pload						number
,mean_pload_inc					number
,se_pload_inc					number
,constraint model_reach_swap_rsize_chk
   check (reach_size between 1 and 5)
,constraint model_reach_swap_pk
   primary key(model_reach_id)
,constraint model_reach_swap_uk_identifier
   unique (sparrow_model_id, identifier)
);
--rollback drop table model_reach_swap cascade constraints purge;

--changeset drsteini:tables3e
create table reach_coef_swap
(reach_coef_id					number(9)									constraint nn_reach_coef_swp_pk not null
,iteration						number(4)									constraint nn_reach_coef_swp_iteration not null
,inc_delivery					float(126)									constraint nn_reach_coef_swp_inc_delivery not null
,total_delivery					float(126)									constraint nn_reach_coef_swp_tot_delivery not null
,boot_error						float(126)				default 0			constraint nn_reach_coef_swp_boot_error not null
,model_reach_id					number(9)									constraint nn_reach_coef_swp_mdl_reach_id not null
,sparrow_model_id				number(9)									constraint nn_reach_coef_swp_mdl_id not null
,constraint reach_coef_swp_fact_pk
   primary key (reach_coef_id)
,constraint reach_coef_swp_uk_value
   unique (model_reach_id, iteration)
);
--rollback drop table reach_coef_swap cascade constraints purge;

--changeset drsteini:tables3f
create table source_reach_coef_swap
(source_reach_coef_id			number(9)									constraint nn_src_reach_coef_swp_pk not null
,iteration						number(4)									constraint nn_src_reach_coef_swp_iter not null
,value							float(126)									constraint nn_src_reach_coef_swp_value not null
,source_id						number(9)									constraint nn_src_reach_coef_swp_src_id not null
,model_reach_id					number(9)									constraint nn_src_reach_coef_swp_mri not null
,sparrow_model_id				number(9)									constraint nn_src_reach_coef_swp_mdl_id not null
,constraint source_reach_coef_swp_pk
   primary key (source_reach_coef_id)
,constraint source_reach_coef_swp_uk_value
   unique (model_reach_id, source_id, iteration)
);
--rollback drop table source_reach_coef_swap cascade constraints purge;

--changeset drsteini:tables3g
create table source_swap
(source_id						number(9)                   				constraint nn_source_swp_pk not null
,name							varchar2(60 char)           				constraint nn_source_swp_name not null
,description					clob
,sort_order						number(3)				default 1			constraint nn_source_swp_srt not null
,sparrow_model_id				number(9)                   				constraint nn_source_swp_mdl_id not null
,identifier						number(3)                   				constraint nn_source_swp_ident not null
,display_name					varchar2(30 char)
,constituent					varchar2(30 char)           				constraint nn_source_swp_const not null
,units							varchar2(30 char)           				constraint nn_source_swp_units not null
,precision						number(2)                   				constraint nn_source_swp_precsn not null
,is_point_source				varchar2(1 char)            				constraint nn_source_swp_isptsr not null
,constraint source_swp_chk_is_pt_src
   check (is_point_source in ('T','F'))
,constraint source_swp_pk
   primary key (source_id)
,constraint source_swp_uk_identifier
   unique (source_id, identifier)
,constraint source_swp_uk_source_name
   unique (sparrow_model_id, name)
);
--rollback drop table source_swap cascade constraints purge;

--changeset drsteini:tables3h
create table source_value_swap
(source_value_id				number(9)									constraint nn_src_value_swp_pk not null
,value							float(126)									constraint nn_src_value_swp_value not null
,source_id						number(9)									constraint nn_src_value_swp_src_id not null
,model_reach_id					number(9)									constraint nn_src_value_swp_mdl_rch_id not null
,mean_pload						number
,se_pload						number
,mean_pload_inc					number
,se_pload_inc					number
,sparrow_model_id				number(9)									constraint nn_src_value_swp_mdl_id not null
,constraint source_value_swp_pk
   primary key (source_value_id)
,constraint source_value_swp_uk_value
   unique (model_reach_id, source_id)
);
--rollback drop table source_value_swap cascade constraints purge;

--changeset drsteini:tables3i
create table temp_ancil
(local_id						number
,std_id							number
,new_or_modified				varchar2(1 char)
,mrb_id							number
,hydseq							number
,sqkm							number
,demtarea						number
,meanq							number
,pname							varchar2(60 char)
,headflag						varchar2(1 char)
,termflag						number
,rchtype						number
,meanv							number
,length_m						number
,edaname						varchar2(60 char)
,edacode						varchar2(5 char)
,huc8							varchar2(8 char)
,gridcode						number
,constraint temp_ancil_pk
   primary key (local_id)
);
--rollback drop table temp_ancil cascade constraints purge;

--changeset drsteini:tables3j
create table temp_coef
(mrb_id							number
,iter							number
,inc_delivf						number
,tot_delivf						number
,boot_error						number
,c_source01						number
,c_source02						number
,c_source03						number
,c_source04						number
,c_source05						number
,c_source06						number
,c_source07						number
,c_source08						number
,c_source09						number
,c_source10						number
,c_source11						number
,c_source12						number
,c_source13						number
,c_source14						number
,c_source15						number
);
--rollback drop table temp_coef cascade constraints purge;

--changeset drsteini:tables3k
create table temp_grid_codes
(comid							number
,gridcode						number
,constraint temp_grid_codes_pk
   primary key (gridcode)
);
--rollback drop table temp_grid_codes cascade constraints purge;

--changeset drsteini:tables3l
create table temp_huc_list
(comid							number
,huc8_int						number
,huc8_char						varchar2(8 char)
,gridcode						number
,constraint temp_huc_list_uk
   unique (gridcode)
);
--rollback drop table temp_huc_list cascade constraints purge;

--changeset drsteini:tables3m
create table temp_predict
(mrb_id							number
,pload_total					varchar2(20 char)
,mean_pload_total				varchar2(20 char)
,mean_pload_01					varchar2(20 char)
,mean_pload_02					varchar2(20 char)
,mean_pload_03					varchar2(20 char)
,mean_pload_04					varchar2(20 char)
,mean_pload_05					varchar2(20 char)
,mean_pload_06					varchar2(20 char)
,mean_pload_07					varchar2(20 char)
,mean_pload_08					varchar2(20 char)
,mean_pload_09					varchar2(20 char)
,mean_pload_10					varchar2(20 char)
,mean_pload_11					varchar2(20 char)
,mean_pload_12					varchar2(20 char)
,mean_pload_13					varchar2(20 char)
,mean_pload_14					varchar2(20 char)
,mean_pload_15					varchar2(20 char)
,mean_pload_16					varchar2(20 char)
,se_pload_total					varchar2(20 char)
,se_pload_01					varchar2(20 char)
,se_pload_02					varchar2(20 char)
,se_pload_03					varchar2(20 char)
,se_pload_04					varchar2(20 char)
,se_pload_05					varchar2(20 char)
,se_pload_06					varchar2(20 char)
,se_pload_07					varchar2(20 char)
,se_pload_08					varchar2(20 char)
,se_pload_09					varchar2(20 char)
,se_pload_10					varchar2(20 char)
,se_pload_11					varchar2(20 char)
,se_pload_12					varchar2(20 char)
,se_pload_13					varchar2(20 char)
,se_pload_14					varchar2(20 char)
,se_pload_15					varchar2(20 char)
,se_pload_16					varchar2(20 char)
,mean_pload_inc_total			varchar2(20 char)
,mean_pload_inc_01				varchar2(20 char)
,mean_pload_inc_02				varchar2(20 char)
,mean_pload_inc_03				varchar2(20 char)
,mean_pload_inc_04				varchar2(20 char)
,mean_pload_inc_05				varchar2(20 char)
,mean_pload_inc_06				varchar2(20 char)
,mean_pload_inc_07				varchar2(20 char)
,mean_pload_inc_08				varchar2(20 char)
,mean_pload_inc_09				varchar2(20 char)
,mean_pload_inc_10				varchar2(20 char)
,mean_pload_inc_11				varchar2(20 char)
,mean_pload_inc_12				varchar2(20 char)
,mean_pload_inc_13				varchar2(20 char)
,mean_pload_inc_14				varchar2(20 char)
,mean_pload_inc_15				varchar2(20 char)
,mean_pload_inc_16				varchar2(20 char)
,se_pload_inc_total				varchar2(20 char)
,se_pload_inc_01				varchar2(20 char)
,se_pload_inc_02				varchar2(20 char)
,se_pload_inc_03				varchar2(20 char)
,se_pload_inc_04				varchar2(20 char)
,se_pload_inc_05				varchar2(20 char)
,se_pload_inc_06				varchar2(20 char)
,se_pload_inc_07				varchar2(20 char)
,se_pload_inc_08				varchar2(20 char)
,se_pload_inc_09				varchar2(20 char)
,se_pload_inc_10				varchar2(20 char)
,se_pload_inc_11				varchar2(20 char)
,se_pload_inc_12				varchar2(20 char)
,se_pload_inc_13				varchar2(20 char)
,se_pload_inc_14				varchar2(20 char)
,se_pload_inc_15				varchar2(20 char)
,se_pload_inc_16				varchar2(20 char)
);
--rollback drop table temp_predict cascade constraints purge;

--changeset drsteini:tables3n
create table temp_resids
(station_id						varchar2(25 char)
,station_name					varchar2(60 char)
,lat							number
,lon							number
,mrb_id							number
,actual							number
,reach							number
,constraint temp_resids_uk
   unique (reach)
);
--rollback drop table temp_resids cascade constraints purge;

--changeset drsteini:tables3o
create table temp_src
(mrb_id							number
,src01							number
,src02							number
,src03							number
,src04							number
,src05							number
,src06							number
,src07							number
,src08							number
,src09							number
,src10							number
,src11							number
,src12							number
,src13							number
,src14							number
,src15							number
);
--rollback drop table temp_src cascade constraints purge;

--changeset drsteini:tables3p
create table temp_src_metadata
(id								number
,sort_order						number
,source_name					varchar2(100 char)
,short_name						varchar2(100 char)
,long_name						varchar2(255 char)
,source_units					varchar2(44 char)
,precision						varchar2(5 char)
,is_point_source				varchar2(1 char)
,description					varchar2(150 char)
);
--rollback drop table temp_src_metadata cascade constraints purge;

--changeset drsteini:tables3q
create table temp_topo
(mrb_id							number
,fnode							number
,tnode							number
,iftran							number
,hydseq							number
,frac							number
);
--rollback drop table temp_topo cascade constraints purge;
