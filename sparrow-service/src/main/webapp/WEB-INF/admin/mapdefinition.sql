Rem
Rem $Header: mapdefinition.sql 16-sep-2005.10:54:16 lqian Exp $
Rem
Rem sdomapdef.sql
Rem
Rem Copyright (c) 2001, 2005, Oracle. All rights reserved.  
Rem
Rem    NAME
Rem      sdomapdef.sql - SDO MAP DEFinitions
Rem
Rem    DESCRIPTION
Rem  Defines the metadata tables and views for SDO_MAPS, SDO_STYLES, 
Rem  and SDO_Themes under MDSYS.
Rem
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem     lqian      09/16/05 - lqian_reorg_ear_files
Rem     jxyang     09/18/01 - remove instead of triggers
Rem     pfwang     08/15/01 - Merged pfwang_add_admin_sql
Rem     pfwang     08/14/01 - Trigger match case
Rem     pfwang     08/08/01 - Add views for dba_, all_.
Rem     jxyang     08/07/01 - Modify user_sdo_themes synonym
Rem    sravada     07/02/01 - Merged sravada_sdomap_definition
Rem    sravada     06/19/01 - Created
Rem

Create Table SDO_MAPS_TABLE (
  SDO_OWNER VARCHAR2(32) default sys_context('userenv', 'CURRENT_SCHEMA'),
  NAME  VARCHAR2(32) NOT NULL,
  DESCRIPTION VARCHAR2(4000),
  DEFINITION  CLOB NOT NULL,
  CONSTRAINT unique_maps
    PRIMARY KEY (SDO_OWNER, NAME)
  );

drop view USER_SDO_MAPS;
Create View USER_SDO_MAPS AS
SELECT NAME, DESCRIPTION,DEFINITION
FROM SDO_MAPS_TABLE
WHERE sdo_owner = sys_context('userenv', 'CURRENT_SCHEMA');

drop view ALL_SDO_MAPS;
Create View ALL_SDO_MAPS AS
SELECT SDO_OWNER OWNER, NAME, DESCRIPTION,DEFINITION
FROM SDO_MAPS_TABLE;

drop view DBA_SDO_MAPS;
Create View DBA_SDO_MAPS AS
SELECT SDO_OWNER OWNER, NAME, DESCRIPTION,DEFINITION
FROM SDO_MAPS_TABLE;
 
Create Table SDO_STYLES_TABLE (
  SDO_OWNER VARCHAR2(32) default sys_context('userenv', 'CURRENT_SCHEMA'),
  NAME  VARCHAR2(32) NOT NULL,
  TYPE  VARCHAR2(32) NOT NULL,
  DESCRIPTION VARCHAR2(4000),
  DEFINITION  CLOB NOT NULL,
  IMAGE  BLOB,
  GEOMETRY   MDSYS.SDO_GEOMETRY,
    CONSTRAINT unique_styles
     PRIMARY KEY (SDO_OWNER, NAME)
    );

drop view USER_SDO_STYLES;
Create View USER_SDO_STYLES AS
SELECT NAME, TYPE, DESCRIPTION,DEFINITION, IMAGE,GEOMETRY
FROM SDO_STYLES_TABLE
WHERE sdo_owner = sys_context('userenv', 'CURRENT_SCHEMA');

drop view ALL_SDO_STYLES;
Create View ALL_SDO_STYLES AS
SELECT SDO_OWNER OWNER, NAME, TYPE, DESCRIPTION,DEFINITION, 
IMAGE,GEOMETRY
FROM SDO_STYLES_TABLE;

drop view DBA_SDO_STYLES;
Create View DBA_SDO_STYLES AS
SELECT SDO_OWNER OWNER, NAME, TYPE, DESCRIPTION,DEFINITION, 
IMAGE,GEOMETRY
FROM SDO_STYLES_TABLE;


Create Table SDO_THEMES_TABLE (
  SDO_OWNER VARCHAR2(32) default sys_context('userenv', 'CURRENT_SCHEMA'),
  NAME  VARCHAR2(32) NOT NULL,
  DESCRIPTION VARCHAR2(4000),
  BASE_TABLE  VARCHAR2(32) NOT NULL,
  GEOMETRY_COLUMN  VARCHAR2(2048) NOT NULL,
  STYLING_RULES CLOB NOT NULL,
    CONSTRAINT unique_themes
        PRIMARY KEY (SDO_OWNER, NAME)
 );
CREATE INDEX SDO_THEMES_IDX ON SDO_THEMES_TABLE(SDO_OWNER,BASE_TABLE);


drop view USER_SDO_THEMES;
Create View USER_SDO_THEMES AS
SELECT NAME, DESCRIPTION, BASE_TABLE, GEOMETRY_COLUMN, STYLING_RULES
FROM SDO_THEMES_TABLE
WHERE sdo_owner = sys_context('userenv', 'CURRENT_SCHEMA');

drop view ALL_SDO_THEMES;
Create View ALL_SDO_THEMES AS
SELECT SDO_OWNER OWNER, NAME, DESCRIPTION, BASE_TABLE, 
                  GEOMETRY_COLUMN, STYLING_RULES
FROM SDO_THEMES_TABLE
WHERE
(exists
   (select table_name from all_tables
    where table_name=base_table
      and owner = sdo_owner
    union all
      select table_name from all_object_tables
      where table_name=base_table
      and owner = sdo_owner
    union all
    select view_name table_name from all_views
    where  view_name=base_table
      and owner = sdo_owner));

drop view DBA_SDO_THEMES;
Create View DBA_SDO_THEMES AS
SELECT SDO_OWNER OWNER, NAME, DESCRIPTION, BASE_TABLE, 
              GEOMETRY_COLUMN, STYLING_RULES
FROM SDO_THEMES_TABLE
WHERE
(exists
   (select table_name from dba_tables
    where table_name=base_table
    union all
      select table_name from dba_object_tables
      where table_name=base_table
    union all
    select view_name table_name from dba_views
    where  view_name=base_table));

grant select,insert,delete,update on user_sdo_maps to public;
grant select,insert,delete,update on user_sdo_styles to public;
grant select,insert,delete,update on user_sdo_themes to public;

grant select on all_sdo_maps to public;
grant select on all_sdo_styles to public;
grant select on all_sdo_themes to public;

grant select on dba_sdo_maps to public;
grant select on dba_sdo_styles to public;
grant select on dba_sdo_themes to public;

create public synonym user_sdo_maps for mdsys.user_sdo_maps;
create public synonym user_sdo_styles for mdsys.user_sdo_styles;
create public synonym user_sdo_themes for mdsys.user_sdo_themes;

create public synonym all_sdo_maps for mdsys.all_sdo_maps;
create public synonym all_sdo_styles for mdsys.all_sdo_styles;
create public synonym all_sdo_themes for mdsys.all_sdo_themes;

create public synonym dba_sdo_maps for mdsys.dba_sdo_maps;
create public synonym dba_sdo_styles for mdsys.dba_sdo_styles;
create public synonym dba_sdo_themes for mdsys.dba_sdo_themes;
