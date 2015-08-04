--liquibase formatted sql

--This is for the sparrow_dss schema
    
--logicalFilePath: changeLog2Tables.sql

--changeset drsteini:tables2a
create table model_reach
(model_reach_id					number(9)                   			constraint nn_model_reach_pk not null
,identifier						number(9)                   			constraint nn_model_reach_id not null
,full_identifier				varchar2(40 char)           			constraint nn_model_reach_fid not null
,sparrow_model_id				number(9)                   			constraint nn_model_reach_mid not null
,enh_reach_id					number(9)
,hydseq							number(11)                  			constraint nn_model_reach_hydseq not null
,iftran							number(1)                   			constraint nn_model_reach_iftran not null
,fnode							number(9)
,tnode							number(9)
,frac							number(10,9)
,reach_size						number(1)                   default 5	constraint nn_model_reach_rsz not null
,mean_pload						number
,se_pload						number
,mean_pload_inc					number
,se_pload_inc					number
,constraint model_reach_rsize_chk
   check (reach_size between 1 and 5)
,constraint model_reach_pk
   primary key(model_reach_id)
,constraint model_reach_uk_identifier
   unique (sparrow_model_id, identifier)
,constraint model_reach_enh_reach_fk 
   foreign key (enh_reach_id) 
     references stream_network.enh_reach (enh_reach_id)
,constraint model_reach_sparrow_model_fk 
   foreign key (sparrow_model_id) 
     references sparrow_model (sparrow_model_id)
)
partition by list (sparrow_model_id)
(partition sparrow_model_22 values (22)
,partition sparrow_model_23 values (23)
,partition sparrow_model_24 values (24)
,partition sparrow_model_30 values (30)
,partition sparrow_model_35 values (35)
,partition sparrow_model_36 values (36)
,partition sparrow_model_41 values (41)
,partition sparrow_model_42 values (42)
,partition sparrow_model_43 values (43)
,partition sparrow_model_44 values (44)
,partition sparrow_model_49 values (49)
,partition sparrow_model_50 values (50)
,partition sparrow_model_51 values (51)
,partition sparrow_model_52 values (52)
,partition sparrow_model_53 values (53)
,partition sparrow_model_54 values (54)
,partition sparrow_model_55 values (55)
,partition sparrow_model_57 values (57)
,partition sparrow_model_58 values (58)
);
--rollback drop table model_reach cascade constraints purge;

--changeset drsteini:tables2b
create table model_calib_sites
(model_reach_id					number									constraint nn_model_calib_sites_pk not null
,station_name      				varchar2(60 char)
,actual            				number
,site_geom         				mdsys.sdo_geometry
,latitude          				number
,longitude         				number
,predict           				number
,station_id        				varchar2(25 char)
,sparrow_model_id  				number(9) 								constraint nn_model_calib_sites_mdl_id not null
,constraint model_calib_sites_pk
   primary key (model_reach_id)
,constraint model_calib_sites_model_fk 
   foreign key (sparrow_model_id) 
     references sparrow_model (sparrow_model_id)
,constraint model_calib_sites_reach_fk 
   foreign key (model_reach_id) 
     references model_reach (model_reach_id)
)
partition by list (sparrow_model_id)
(partition sparrow_model_22 values (22)
,partition sparrow_model_23 values (23)
,partition sparrow_model_24 values (24)
,partition sparrow_model_30 values (30)
,partition sparrow_model_35 values (35)
,partition sparrow_model_36 values (36)
,partition sparrow_model_41 values (41)
,partition sparrow_model_42 values (42)
,partition sparrow_model_43 values (43)
,partition sparrow_model_44 values (44)
,partition sparrow_model_49 values (49)
,partition sparrow_model_50 values (50)
,partition sparrow_model_51 values (51)
,partition sparrow_model_52 values (52)
,partition sparrow_model_53 values (53)
,partition sparrow_model_54 values (54)
,partition sparrow_model_55 values (55)
,partition sparrow_model_57 values (57)
,partition sparrow_model_58 values (58)
);
--rollback drop table model_calib_sites cascade constraints purge;

--changeset drsteini:tables2c
create table model_reach_attrib
(model_reach_id					number(9)								constraint nn_model_reach_attrib_pk not null
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
,sparrow_model_id				number(9)								constraint nn_reach_attrib_mdl_id not null
,constraint model_reach_attrib_pk
   primary key (model_reach_id)
,constraint model_rch_attrib_reach_fk
   foreign key (model_reach_id) 
     references model_reach (model_reach_id)
,constraint reach_attrib_model_fk 
   foreign key (sparrow_model_id) 
     references sparrow_model (sparrow_model_id)
)
partition by list (sparrow_model_id)
(partition sparrow_model_22 values (22)
,partition sparrow_model_23 values (23)
,partition sparrow_model_24 values (24)
,partition sparrow_model_30 values (30)
,partition sparrow_model_35 values (35)
,partition sparrow_model_36 values (36)
,partition sparrow_model_41 values (41)
,partition sparrow_model_42 values (42)
,partition sparrow_model_43 values (43)
,partition sparrow_model_44 values (44)
,partition sparrow_model_49 values (49)
,partition sparrow_model_50 values (50)
,partition sparrow_model_51 values (51)
,partition sparrow_model_52 values (52)
,partition sparrow_model_53 values (53)
,partition sparrow_model_54 values (54)
,partition sparrow_model_55 values (55)
,partition sparrow_model_57 values (57)
,partition sparrow_model_58 values (58)
);
--rollback drop table model_reach_attrib cascade constraints purge;

--changeset drsteini:tables2d
create table source_value
(source_value_id				number(9)								constraint nn_src_value_pk not null
,value							float(126)								constraint nn_src_value_value not null
,source_id						number(9)								constraint nn_src_value_src_id not null
,model_reach_id					number(9)								constraint nn_src_value_mdl_rch_id not null
,mean_pload						number
,se_pload						number
,mean_pload_inc					number
,se_pload_inc					number
,sparrow_model_id				number(9)								constraint nn_src_value_mdl_id not null
,constraint source_value_pk
   primary key (source_value_id)
,constraint source_value_uk_value
   unique (model_reach_id, source_id)
,constraint source_value_mdl_rch_fk 
   foreign key (model_reach_id) 
     references model_reach (model_reach_id)
,constraint source_value_source_fk 
   foreign key (source_id)
     references source (source_id)
,constraint src_value_sparrow_model_fk 
   foreign key (sparrow_model_id) 
     references sparrow_model (sparrow_model_id)
)
partition by list (sparrow_model_id)
(partition sparrow_model_22 values (22)
,partition sparrow_model_23 values (23)
,partition sparrow_model_24 values (24)
,partition sparrow_model_30 values (30)
,partition sparrow_model_35 values (35)
,partition sparrow_model_36 values (36)
,partition sparrow_model_41 values (41)
,partition sparrow_model_42 values (42)
,partition sparrow_model_43 values (43)
,partition sparrow_model_44 values (44)
,partition sparrow_model_49 values (49)
,partition sparrow_model_50 values (50)
,partition sparrow_model_51 values (51)
,partition sparrow_model_52 values (52)
,partition sparrow_model_53 values (53)
,partition sparrow_model_54 values (54)
,partition sparrow_model_55 values (55)
,partition sparrow_model_57 values (57)
,partition sparrow_model_58 values (58)
);
--rollback drop table source_value cascade constraints purge;

--changeset drsteini:tables2e
create table source_reach_coef
(source_reach_coef_id			number(9)								constraint nn_src_reach_coef_pk not null
,iteration						number(4)								constraint nn_src_reach_coef_iter not null
,value							float(126)								constraint nn_src_reach_coef_value not null
,source_id						number(9)								constraint nn_src_reach_coef_src_id not null
,model_reach_id					number(9)								constraint nn_src_reach_coef_mri not null
,sparrow_model_id				number(9)								constraint nn_src_reach_coef_mdl_id not null
,constraint source_reach_coef_pk
   primary key (source_reach_coef_id)
,constraint source_reach_coe_uk_value
   unique (model_reach_id, source_id, iteration)
,constraint source_reach_coef_fk 
   foreign key (model_reach_id) 
     references model_reach (model_reach_id)
,constraint source_reach_coef_src_fk 
   foreign key (source_id) 
     references source (source_id)
,constraint src_reach_coef_model_fk 
   foreign key (sparrow_model_id) 
     references sparrow_model (sparrow_model_id)
)
partition by list (sparrow_model_id)
(partition sparrow_model_22 values (22)
,partition sparrow_model_23 values (23)
,partition sparrow_model_24 values (24)
,partition sparrow_model_30 values (30)
,partition sparrow_model_35 values (35)
,partition sparrow_model_36 values (36)
,partition sparrow_model_41 values (41)
,partition sparrow_model_42 values (42)
,partition sparrow_model_43 values (43)
,partition sparrow_model_44 values (44)
,partition sparrow_model_49 values (49)
,partition sparrow_model_50 values (50)
,partition sparrow_model_51 values (51)
,partition sparrow_model_52 values (52)
,partition sparrow_model_53 values (53)
,partition sparrow_model_54 values (54)
,partition sparrow_model_55 values (55)
,partition sparrow_model_57 values (57)
,partition sparrow_model_58 values (58)
);
--rollback drop table source_reach_coef cascade constraints purge;

--changeset drsteini:tables2f
create table reach_coef
(reach_coef_id					number(9)								constraint nn_reach_coef_pk not null
,iteration						number(4)								constraint nn_reach_coef_iteration not null
,inc_delivery					float(126)								constraint nn_reach_coef_inc_delivery not null
,total_delivery					float(126)								constraint nn_reach_coef_tot_delivery not null
,boot_error						float(126)		default 0				constraint nn_reach_coef_boot_error not null
,model_reach_id					number(9)								constraint nn_reach_coef_mdl_reach_id not null
,sparrow_model_id				number(9)								constraint nn_reach_coef_mdl_id not null
,constraint reach_coef_fact_pk
   primary key (reach_coef_id)
,constraint reach_coef_uk_value
   unique (model_reach_id, iteration)
,constraint reach_ceof_model_reach_fk 
   foreign key (model_reach_id) 
     references model_reach (model_reach_id)
,constraint reach_coef_sparrow_model_fk 
   foreign key (sparrow_model_id) 
     references sparrow_model (sparrow_model_id)
)
partition by list (sparrow_model_id)
(partition sparrow_model_22 values (22)
,partition sparrow_model_23 values (23)
,partition sparrow_model_24 values (24)
,partition sparrow_model_30 values (30)
,partition sparrow_model_35 values (35)
,partition sparrow_model_36 values (36)
,partition sparrow_model_41 values (41)
,partition sparrow_model_42 values (42)
,partition sparrow_model_43 values (43)
,partition sparrow_model_44 values (44)
,partition sparrow_model_49 values (49)
,partition sparrow_model_50 values (50)
,partition sparrow_model_51 values (51)
,partition sparrow_model_52 values (52)
,partition sparrow_model_53 values (53)
,partition sparrow_model_54 values (54)
,partition sparrow_model_55 values (55)
,partition sparrow_model_57 values (57)
,partition sparrow_model_58 values (58)
);
--rollback drop table reach_coef cascade constraints purge;
