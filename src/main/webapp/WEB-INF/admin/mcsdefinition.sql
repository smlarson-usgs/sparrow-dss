Rem
Rem $Header: mcsdefinition.sql 24-feb-2006.12:28:12 lqian Exp $
Rem
Rem sdomapdef.sql
Rem
Rem Copyright (c) 2001, 2006, Oracle. All rights reserved.  
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

