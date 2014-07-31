--liquibase formatted sql

--This is for the sparrow_dss schema
 
--changeset drsteini:tables1a
create table model_calib_sites
(model_reach_id					number										not null
,station_name					varchar2(60 byte)
,actual							number
,site_geom						mdsys.sdo_geometry
,latitude						number
,longitude						number
,predict						number
,station_id						varchar2(25 byte)
,constraint model_calib_sites_pk
   primary key (model_reach_id)
)
result_cache (mode default)
compress basic
;
--rollback drop table model_calib_sites cascade constraints purge;

--changeset drsteini:tables1b
create table model_reach
(model_reach_id					number(9)									not null
,identifier						number(9)									not null
,full_identifier				varchar2(40 byte)							not null
,sparrow_model_id				number(9)									not null
,enh_reach_id					number(9)
,hydseq							number(11)									not null
,iftran							number(1)									not null
,fnode							number(9)
,tnode							number(9)
,frac							number(10,9)
,reach_size						number(1)				default 5			not null
,mean_pload						number
,se_pload						number
,mean_pload_inc					number
,se_pload_inc					number
,constraint model_reach_pk
   primary key (model_reach_id)
,constraint model_reach_uk_identifier
   unique (sparrow_model_id, identifier)
,constraint model_reach_rsize_chk
   check (reach_size between 1 and 5)
)
result_cache (mode default)
compress basic
;
--rollback drop table model_reach cascade constraints purge;

--changeset drsteini:tables1c
comment on column model_reach.reach_size is 'An arbitrary indicator of at what scale a reach should be shown on a map:  5:  National, 4: Regional, 3: State, 2 Multi-County, 1: County.';
--rollback select 'norollback' from dual;

--changeset drsteini:tables1d
create table model_reach_attrib
(model_reach_id					number(9)									not null
,reach_name						varchar2(60 byte)
,open_water_name				varchar2(60 byte)
,meanq							number(16,6)
,meanv							number(16,6)
,catch_area						number(16,6)
,cum_catch_area					number(16,6)
,reach_length					number(16)
,huc2							varchar2(2 byte)
,huc4							varchar2(4 byte)
,huc6							varchar2(6 byte)
,huc8							varchar2(8 byte)
,head_reach						number(1)
,shore_reach					number(1)
,term_trans						number(1)
,term_estuary					number(1)
,term_nonconnect				number(1)
,edaname						varchar2(60 byte)
,edacode						varchar2(10 byte)
,huc2_name						varchar2(60 byte)
,huc4_name						varchar2(60 byte)
,huc6_name						varchar2(60 byte)
,huc8_name						varchar2(60 byte)
,constraint model_reach_attrib_pk
   primary key (model_reach_id)
)
result_cache (mode default)
compress basic
;
--rollback drop table model_reach_attrib cascade constraints purge;

--changeset drsteini:tables1e
comment on column model_reach_attrib.reach_name is 'The primary name for the reach';
comment on column model_reach_attrib.open_water_name is 'The primary name for any associated open water, such as a lake or pond.';
comment on column model_reach_attrib.meanq is 'Mean Flow in Cubic Feet / Second';
comment on column model_reach_attrib.meanv is 'Velocity corresponding to mean streamflow for reach, in feet per second';
comment on column model_reach_attrib.catch_area is 'Incremental drainage area for a given reach, in square kilometers';
comment on column model_reach_attrib.cum_catch_area is 'Cumulative drainage area for a given reach, in square kilometers';
comment on column model_reach_attrib.reach_length is 'Reach length in meters';
--rollback select 'norollback' from dual;

--changeset drsteini:tables1f
create table model_reach_geom
(model_reach_id					number(9)									not null
,reach_geom						mdsys.sdo_geometry
,catch_geom						mdsys.sdo_geometry
,watershed_geom					mdsys.sdo_geometry
,constraint model_reach_geom_pk
   primary key (model_reach_id)
)
result_cache (mode default)
compress basic
;
--rollback drop table model_reach_geom cascade constraints purge;

--changeset drsteini:tables1g
create table model_reach_watershed
(model_reach_id					number(9)
,watershed_id					number(9)
)
result_cache (mode default)
;
--rollback drop table model_reach_watershed cascade constraints purge;

--changeset drsteini:tables1h
create table my_user_sdo_geom_metadata
(table_name						varchar2(32 byte)
,column_name					varchar2(1024 byte)
,diminfo						mdsys.sdo_dim_array
,srid							number
)
result_cache (mode default)
compress basic 
;
--rollback drop table my_user_sdo_geom_metadata cascade constraints purge;

--changeset drsteini:tables1i
create table my_user_sdo_styles
(name							varchar2(32 byte)							not null
,type							varchar2(32 byte)							not null
,description					varchar2(4000 byte)
,definition						clob
,image							blob
,geometry						mdsys.sdo_geometry
)
result_cache (mode default)
compress basic 
;
--rollback drop table my_user_sdo_styles cascade constraints purge;

--changeset drsteini:tables1j
create table my_user_sdo_themes
(name							varchar2(32 byte)							not null
,description					varchar2(4000 byte)
,base_table						varchar2(64 byte)							not null
,geometry_column				varchar2(2048 byte)							not null
,styling_rules					clob
)
result_cache (mode default)
compress basic 
;
--rollback drop table my_user_sdo_themes cascade constraints purge;

--changeset drsteini:tables1k
create table my2_styles
(name							varchar2(32 byte)							not null
,type							varchar2(32 byte)							not null
,description					varchar2(4000 byte)
,definition						clob
,image							blob
,geometry						mdsys.sdo_geometry
)
result_cache (mode default)
compress basic 
;
--rollback drop table my2_styles cascade constraints purge;

--changeset drsteini:tables1l
create table predefined_watershed
(watershed_id					number(9)
,name							varchar2(50 byte)
,description					varchar2(200 char)
,date_added						date
,sparrow_model_id  number(9)
,constraint predefined_watershed_pk
   primary key (watershed_id)
)
result_cache (mode default)
;
--rollback drop table predefined_watershed cascade constraints purge;

--changeset drsteini:tables1m
create table reach_coef
(reach_coef_id					number(9)									not null
,iteration						number(4)									not null
,inc_delivery					float(126)									not null
,total_delivery					float(126)									not null
,boot_error						float(126)				default 0			not null
,model_reach_id					number(9)									not null
,constraint reach_coef_fact_pk
   primary key (reach_coef_id)
,constraint reach_coef_uk_value
   unique (model_reach_id, iteration)
)
result_cache (mode default)
compress basic 
;
--rollback drop table reach_coef cascade constraints purge;

--changeset drsteini:tables1n
comment on column reach_coef.boot_error is 'Would 1 be a more reasonable value for this? ';
--rollback select 'norollback' from dual;

--changeset drsteini:tables1o
create table source
(source_id						number(9)									not null
,name							varchar2(60 char)							not null
,description					clob
,sort_order						number(3)				default 1			not null
,sparrow_model_id				number(9)									not null
,identifier						number(3)									not null
,display_name					varchar2(31 char)
,constituent					varchar2(30 char)							not null
,units							varchar2(30 char)							not null
,precision						number(2)									not null
,is_point_source				varchar2(1 char)							not null
,constraint source_pk
   primary key (source_id)
,constraint source_uk_identifier
   unique (source_id, identifier)
,constraint source_uk_source_name
   unique (sparrow_model_id, name)
,constraint source_chk_is_point_source
   check (is_point_source in ('T','F'))
)
result_cache (mode default)
compress basic 
;
--rollback drop table source cascade constraints purge;

--changeset drsteini:tables1p
comment on column sparrow_dss.source.source_id is 'Uniquely id''s a source globally - this is the UUID db key.';
comment on column sparrow_dss.source.name is 'The full name of the source. ';
comment on column sparrow_dss.source.description is 'A detailed description of the source.';
comment on column sparrow_dss.source.sort_order is 'Sequential numbers used to sort the sources (low to high) when the sources are displayed to the user. Can just be a copy of the SOURCE_ID values if the sort order does not need to be changed.';
comment on column sparrow_dss.source.identifier is 'Normally a sequentially numbered ID for the source, starting at 1. Used to refer to a source within a model.';
comment on column sparrow_dss.source.display_name is 'A shortened name for the source that is easier to display in space limited areas ';
comment on column sparrow_dss.source.constituent is 'The name of the thing the source values are measuring. ';
comment on column sparrow_dss.source.units is 'The units in which the constituent is measured in ';
comment on column sparrow_dss.source.precision is 'The number of decimal places used in reports of the source value. Full precision is used for calculations. ';
comment on column sparrow_dss.source.is_point_source is 'A yes or no flag (values are Y or N) to indicate if this is a point source. This flag may be used to inhibit reports of yield calculations based on point sources. ';
--rollback select 'norollback' from dual;

--changeset drsteini:tables1q
create table source_reach_coef
(source_reach_coef_id			number(9)									not null
,iteration						number(4)									not null
,value							float(126)									not null
,source_id						number(9)									not null
,model_reach_id					number(9)									not null
,constraint source_reach_coef_pk
   primary key (source_reach_coef_id)
,constraint source_reach_coef_uk_value
   unique (model_reach_id, source_id, iteration)
)
result_cache (mode default)
compress basic 
;
--rollback drop table source_reach_coef cascade constraints purge;

--changeset drsteini:tables1r
create table source_value
(source_value_id				number(9)									not null
,value							float(126)									not null
,source_id						number(9)									not null
,model_reach_id					number(9)									not null
,mean_pload						number
,se_pload						number
,mean_pload_inc					number
,se_pload_inc					number
,constraint source_value_pk
   primary key (source_value_id)
,constraint source_value_uk_value
   unique (model_reach_id, source_id)
)
result_cache (mode default)
compress basic 
;
--rollback drop table source_value cascade constraints purge;

--changeset drsteini:tables1s
create table sparrow_model
(sparrow_model_id				number(9)									not null
,is_approved					char(1 byte)			default 'F'			not null
,is_public						char(1 byte)			default 'F'			not null
,is_archived					char(1 byte)			default 'F'			not null
,name							varchar2(40 byte)							not null
,description					clob
,date_added						date					default sysdate		not null
,contact_id						number(9)									not null
,enh_network_id					number(9)									not null
,url							varchar2(400 byte)
,bound_north					number(9,6)
,bound_east						number(9,6)
,bound_south					number(9,6)
,bound_west						number(9,6)
,date_exported					date
,date_tested					date
,status							varchar2(30 byte)
,constituent					varchar2(30 byte)
,units							varchar2(30 byte)
,theme_name						varchar2(30 char)
,constraint sparrow_model_pk
   primary key (sparrow_model_id)
,constraint sparrow_model_uk_name
   unique (enh_network_id, is_approved, is_public, is_archived, name)
,constraint sparrow_model_bd_east
   check (bound_east <= 180 and bound_east > -180)
,constraint sparrow_model_bd_north
   check (bound_north < 90 and bound_north > -90)
,constraint sparrow_model_bd_south
   check (bound_south < 90 and bound_south > -90)
,constraint sparrow_model_bd_west
   check (bound_west >= -180 and bound_west < 180)
,constraint sparrow_model_chk_is_app
   check (is_approved in ('T','F'))
,constraint sparrow_model_chk_is_arc
   check (is_archived in ('T', 'F'))
,constraint sparrow_model_chk_is_public
   check (is_public in ('T', 'F'))
)
result_cache (mode default)
compress basic 
;
--rollback drop table sparrow_model cascade constraints purge;

