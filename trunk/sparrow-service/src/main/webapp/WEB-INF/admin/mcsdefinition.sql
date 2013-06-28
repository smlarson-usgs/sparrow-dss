Rem
Rem $Header: lbs/ship/ear/web/WEB-INF/admin/mcsdefinition.sql /main/4 2009/03/24 23:08:49 jxyang Exp $
Rem
Rem sdomapdef.sql
Rem
Rem Copyright (c) 2001, 2009, Oracle and/or its affiliates. 
Rem All rights reserved. 
Rem
Rem    NAME
Rem      mcsdefinition.sql - SDO MAP DEFinitions
Rem
Rem    DESCRIPTION
Rem      Defines the metadata tables and views for SDO_CACHED_MAPS
Rem
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem     jxyang     03/03/09 - add sdo_tile type
Rem     jxyang     10/31/08 - add sdo_tile_admin_tasks_table
Rem     jxyang     02/24/06 - add trigger to drop cache instance when user is 
Rem                           dropped 
Rem     jxyang     12/15/05 - create
Rem     jxyang     12/15/05 - create
Rem     jxyang     12/13/05 - create
Rem

declare
begin
  begin
   execute immediate 
'create table mdsys.sdo_cached_maps_table(
  SDO_OWNER VARCHAR2(32) default sys_context(''userenv'', ''CURRENT_SCHEMA''),
  name varchar2(32),
  description varchar2(4000), 
  tiles_table varchar2(32), 
  is_online varchar2(3) not null,
  is_internal varchar2(4) not null,
  definition clob not null,
  base_map varchar2(32),
  map_adapter blob,
  CONSTRAINT unique_cached_maps
    PRIMARY KEY (SDO_OWNER, NAME)) ';
   exception when others then NULL;
  end;
end;
/

Create or replace  View mdsys.USER_SDO_CACHED_MAPS AS
SELECT NAME, DESCRIPTION, tiles_table, is_online, is_internal, DEFINITION, base_map, map_adapter 
FROM mdsys.SDO_CACHED_MAPS_TABLE
WHERE sdo_owner = sys_context('userenv', 'CURRENT_SCHEMA');

Create or replace  View mdsys.ALL_SDO_CACHED_MAPS AS
SELECT SDO_OWNER OWNER, NAME, DESCRIPTION, tiles_table, is_online, is_internal, DEFINITION, base_map, map_adapter 
FROM mdsys.SDO_CACHED_MAPS_TABLE ;

Create or replace  View mdsys.DBA_SDO_CACHED_MAPS AS
SELECT SDO_OWNER OWNER, NAME, DESCRIPTION, tiles_table, is_online, is_internal, DEFINITION, base_map, map_adapter 
FROM mdsys.SDO_CACHED_MAPS_TABLE ;

grant select,insert,delete,update on mdsys.user_sdo_cached_maps to public;
grant select on mdsys.all_sdo_cached_maps to public;
grant select on mdsys.dba_sdo_cached_maps to public;

create  or replace public synonym user_sdo_cached_maps for mdsys.user_sdo_cached_maps;
create  or replace public synonym all_sdo_cached_maps for mdsys.all_sdo_cached_maps;
create  or replace public synonym dba_sdo_cached_maps for mdsys.dba_sdo_cached_maps;

create or replace trigger mdsys.sdo_cached_map_drop_user
after drop on DATABASE
declare 
   stmt varchar2(200);
BEGIN
     if dictionary_obj_type = 'USER' THEN
       stmt := 'DELETE FROM SDO_CACHED_MAPS_TABLE ' ||
    ' WHERE ''"''||SDO_OWNER||''"'' = ''"' || dictionary_obj_name || '"'' ';
       EXECUTE IMMEDIATE stmt;
    end if;
end;
/

begin
  begin
   execute immediate 
'create table mdsys.sdo_tile_admin_tasks_table(
  SDO_OWNER VARCHAR2(32) default sys_context(''userenv'', ''CURRENT_SCHEMA''),
  id number primary key,
  description varchar2(200), 
  submitted_by varchar2(100) not null, 
  cached_map_name varchar2(32) not null,
  type varchar2(10) not null,
  bound sdo_geometry,
  zoom_levels sdo_number_array,
  schedule clob,
  status varchar2(20),
  progress clob)';
   exception when others then NULL;
  end;
end;
/

Create or replace  View mdsys.USER_SDO_TILE_ADMIN_TASKS AS
SELECT ID, DESCRIPTION, submitted_by, cached_map_name, type, bound, zoom_levels, schedule, status, progress 
FROM mdsys.SDO_TILE_ADMIN_TASKS_TABLE
WHERE sdo_owner = sys_context('userenv', 'CURRENT_SCHEMA');

Create or replace  View mdsys.ALL_SDO_TILE_ADMIN_TASKS AS
SELECT SDO_OWNER, ID, DESCRIPTION, submitted_by, cached_map_name, type, bound, zoom_levels, schedule, status, progress 
FROM mdsys.SDO_TILE_ADMIN_TASKS_TABLE ;

Create or replace  View mdsys.DBA_SDO_TILE_ADMIN_TASKS AS
SELECT SDO_OWNER, ID, DESCRIPTION, submitted_by, cached_map_name, type, bound, zoom_levels, schedule, status, progress 
FROM mdsys.SDO_TILE_ADMIN_TASKS_TABLE ;

grant select,insert,delete,update on mdsys.USER_SDO_TILE_ADMIN_TASKS to public;
grant select on mdsys.all_sdo_TILE_ADMIN_TASKS to public;
grant select on mdsys.dba_sdo_TILE_ADMIN_TASKS to public;

create  or replace public synonym user_sdo_TILE_ADMIN_TASKS for mdsys.user_sdo_TILE_ADMIN_TASKS;
create  or replace public synonym all_sdo_TILE_ADMIN_TASKS for mdsys.all_sdo_TILE_ADMIN_TASKS;
create  or replace public synonym dba_sdo_TILE_ADMIN_TASKS for mdsys.dba_sdo_TILE_ADMIN_TASKS;

create or replace trigger mdsys.sdo_tile_admin_tasks_drop_user
after drop on DATABASE
declare 
   stmt varchar2(200);
BEGIN
     if dictionary_obj_type = 'USER' THEN
       stmt := 'DELETE FROM SDO_TILE_ADMIN_TASKS_TABLE ' ||
    ' WHERE ''"''||SDO_OWNER||''"'' = ''"' || dictionary_obj_name || '"'' ';
       EXECUTE IMMEDIATE stmt;
    end if;
end;
/

declare
begin
  begin
   execute immediate 
     'create sequence mdsys.sdo_tile_admin_task_seq start with 1 increment by 1' ;
   exception when others then NULL;
  end;
end;
/

create or replace trigger mdsys.sdo_tile_admin_tasks_insert before insert on 
  mdsys.sdo_tile_admin_tasks_table for each row 
begin
  select mdsys.sdo_tile_admin_task_seq.nextval into :new.id from dual;
end;
/

create or replace trigger mdsys.sdo_cached_map_delete after DELETE on mdsys.sdo_cached_maps_table for each row
declare 
   stmt varchar2(200);
BEGIN
   DELETE FROM SDO_TILE_ADMIN_TASKS_TABLE where sdo_owner=:old.sdo_owner and cached_map_name=:old.name ;
end;
/

create or replace type mdsys.sdo_tile as object
(
  tile_layer varchar2(32),
  zoom_level number(2),
  x number(8),
  y number(8),
  modified timestamp(0),
  data blob
);
/ 

grant execute on mdsys.sdo_tile to public ;
