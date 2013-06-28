Rem
Rem $Header: lbs/ship/ear/web/WEB-INF/admin/csdefinition.sql /st_lbs_11.1.1_bi/1 2009/10/29 14:10:50 jxyang Exp $
Rem
Rem csdefinition.sql
Rem
Rem Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved. 
Rem
Rem    NAME
Rem      csdefinition.sql - <one-line expansion of the name>
Rem
Rem    DESCRIPTION
Rem      <short description of component this file declares/defines>
Rem
Rem    NOTES
Rem      <other useful comments, qualifications, etc.>
Rem
Rem    MODIFIED   (MM/DD/YY)
Rem    jxyang      10/27/09 - Created
Rem

-- This script defines the Ellipsoid Mercator coordinate system and
-- the Spherical Mercator coordinate system. It must be executed by 
-- a user with DBA privilege.


-- Define the Ellipsoid Mercator EPSG: 54004
INSERT INTO sdo_coord_ops ( 
coord_op_id, 
coord_op_name, 
coord_op_type,
source_srid, 
target_srid, 
coord_tfm_version, 
coord_op_variant,
coord_op_method_id, 
UOM_ID_SOURCE_OFFSETS, 
UOM_ID_TARGET_OFFSETS,
information_source, 
data_source, 
show_operation, 
is_legacy,
legacy_code, 
reverse_op, 
is_implemented_forward, 
is_implemented_reverse)
VALUES ( 
54004, 
'World Mercator', 
'CONVERSION', 
null, 
null, 
'', 
null,
9804, 
null, 
null, 
null, 
null, 
1, 
'FALSE', 
null, 
1, 
1, 
1);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS (
COORD_OP_ID, 
COORD_OP_METHOD_ID, 
PARAMETER_ID, 
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF, UOM_ID) 
VALUES (
54004, 
9804, 
8801, 
0,
NULL, 
9102);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS ( 
COORD_OP_ID,
COORD_OP_METHOD_ID, 
PARAMETER_ID, 
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF, 
UOM_ID) 
VALUES (
54004, 
9804, 
8802, 
0, 
NULL, 
9102);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS ( 
COORD_OP_ID,
COORD_OP_METHOD_ID, 
PARAMETER_ID, 
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF, 
UOM_ID) VALUES (
54004, 
9804, 
8805, 
1, 
NULL, 
9201);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS ( 
COORD_OP_ID,
COORD_OP_METHOD_ID, 
PARAMETER_ID, 
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF, 
UOM_ID) 
VALUES (
54004, 
9804, 
8806, 
0, 
NULL, 
9001);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS ( 
COORD_OP_ID,
COORD_OP_METHOD_ID, 
PARAMETER_ID, 
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF, 
UOM_ID) 
VALUES (
54004, 
9804, 
8807, 
0, 
NULL, 
9001);

INSERT INTO sdo_coord_ref_system ( 
srid, 
coord_ref_sys_name,
coord_ref_sys_kind, 
coord_sys_id, 
datum_id, 
geog_crs_datum_id,
source_geog_srid, 
projection_conv_id, 
cmpd_horiz_srid, 
cmpd_vert_srid,
information_source, 
data_source, 
is_legacy, 
legacy_code, 
legacy_wktext,
legacy_cs_bounds, 
is_valid, 
supports_sdo_geometry) 
VALUES (
54004, 
'World Mercator', 
'PROJECTED', 
4499, 
null, 
6326, 
4326, 
54004,
Null, 
Null, 
null, 
null, 
'FALSE', 
null, 
null, 
null, 
'TRUE', 
'TRUE');


-- Define the Spherical Mercator EPSG: 3785
insert into MDSYS.SDO_ELLIPSOIDS (
ELLIPSOID_ID,
ELLIPSOID_NAME,
SEMI_MAJOR_AXIS,
UOM_ID,
INV_FLATTENING,
SEMI_MINOR_AXIS,
INFORMATION_SOURCE,
DATA_SOURCE,
IS_LEGACY,
LEGACY_CODE)
VALUES (
7059,
'Popular Visualisation Sphere',
6378137,
9001,
1.0000E+12,
NULL,
null,
'EPSG',
'FALSE',
null);

insert into MDSYS.SDO_DATUMS (
DATUM_ID,
DATUM_NAME,
DATUM_TYPE,
ELLIPSOID_ID,
PRIME_MERIDIAN_ID,
INFORMATION_SOURCE,
DATA_SOURCE,
SHIFT_X,
SHIFT_Y,
SHIFT_Z,
ROTATE_X,
ROTATE_Y,
ROTATE_Z,
SCALE_ADJUST,
IS_LEGACY,
LEGACY_CODE)
VALUES (
6055,
'Popular Visualisation Datum',
'GEODETIC',
7059,
8901,
null,
'EPSG',
NULL,
NULL,
NULL,
NULL,
NULL,
NULL,
NULL,
'FALSE',
NULL);

insert into MDSYS.SDO_COORD_REF_SYSTEM (
SRID,
COORD_REF_SYS_NAME,
COORD_REF_SYS_KIND,
COORD_SYS_ID,
DATUM_ID,
geog_crs_datum_id,
SOURCE_GEOG_SRID,
PROJECTION_CONV_ID,
CMPD_HORIZ_SRID,
CMPD_VERT_SRID,
INFORMATION_SOURCE,
DATA_SOURCE,
IS_LEGACY,
LEGACY_CODE,
LEGACY_WKTEXT,
LEGACY_CS_BOUNDS,
is_valid,
supports_sdo_geometry)
VALUES (
4055,
'Popular Visualisation CRS',
'GEOGRAPHIC2D',
6422,
6055,
6055,
NULL,
NULL,
NULL,
NULL,
null,
'EPSG',
'FALSE',
NULL,
NULL,
NULL,
'TRUE',
'TRUE');

INSERT INTO sdo_coord_ops (
coord_op_id,
coord_op_name,
coord_op_type,
source_srid,
target_srid,
coord_tfm_version,
coord_op_variant,
coord_op_method_id,
UOM_ID_SOURCE_OFFSETS,
UOM_ID_TARGET_OFFSETS,
information_source,
data_source,
show_operation,
is_legacy,
legacy_code,
reverse_op,
is_implemented_forward,
is_implemented_reverse)
VALUES (
19847,
'Popular Visualisation Mercator',
'CONVERSION',
null,
null,
'',
null,
9804,
null,
null,
null,
null,
1,
'FALSE',
null,
1,
1,
1);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS (
COORD_OP_ID,
COORD_OP_METHOD_ID,
PARAMETER_ID,
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF,
UOM_ID)
VALUES (
19847,
9804,
8801, -- Latitude of natural origin
0,
NULL,
9102);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS (
COORD_OP_ID,
COORD_OP_METHOD_ID,
PARAMETER_ID,
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF,
UOM_ID)
VALUES (
19847,
9804,
8802, -- longitude of natural origin
0,
NULL,
9102);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS (
COORD_OP_ID,
COORD_OP_METHOD_ID,
PARAMETER_ID,
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF,
UOM_ID)
VALUES (
19847,
9804,
8805, -- scale factor at natural origin
1,
NULL,
9201);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS (
COORD_OP_ID,
COORD_OP_METHOD_ID,
PARAMETER_ID,
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF,
UOM_ID)
VALUES (
19847,
9804,
8806, -- false easting
0,
NULL,
9001);

insert into MDSYS.SDO_COORD_OP_PARAM_VALS (
COORD_OP_ID,
COORD_OP_METHOD_ID,
PARAMETER_ID,
PARAMETER_VALUE,
PARAM_VALUE_FILE_REF,
UOM_ID)
VALUES (
19847,
9804,
8807, -- false northing
0,
NULL,
9001);

INSERT INTO sdo_coord_ref_system (
srid,
coord_ref_sys_name,
coord_ref_sys_kind,
coord_sys_id,
datum_id,
geog_crs_datum_id,
source_geog_srid,
projection_conv_id,
cmpd_horiz_srid,
cmpd_vert_srid,
information_source,
data_source,
is_legacy,
legacy_code,
legacy_wktext,
legacy_cs_bounds,
is_valid,
supports_sdo_geometry)
VALUES (
3785,
'Popular Visualisation CRS / Mercator',
'PROJECTED',
4499,
null,
6055,
4055,
19847,
Null,
Null,
null,
null,
'FALSE',
null,
null,
null,
'TRUE',
'TRUE');


-- Create the tfm_plans, i.e. transformation rule.
-- Note: This will result in an incorrect conversion since it ignores a datum shift
-- between the ellipsoid and the sphere. However the data will match up better
-- on google maps

-- For wgs84 (8307)
call sdo_cs.create_pref_concatenated_op( 83073785, 'CONCATENATED OPERATION 8307 3785', TFM_PLAN(SDO_TFM_CHAIN(8307, 1000000000, 4055, 19847, 3785)), NULL);

-- For 4326, EPSG equivalent of 8307
call sdo_cs.create_pref_concatenated_op( 43263785, 'CONCATENATED_OPERATION_4326_3785', TFM_PLAN(SDO_TFM_CHAIN(4326, 1000000000, 4055, 19847, 3785)), NULL); 

-- For OS BNG, Oracle SRID 81989
call sdo_cs.create_pref_concatenated_op( 819893785, 'CONCATENATED OPERATION 81989 3785', TFM_PLAN(SDO_TFM_CHAIN(81989, -19916, 2000021, 1000000000, 4055, 19847, 3785)), NULL); 

-- For 27700, EPSG equivalent of 81989
call sdo_cs.create_pref_concatenated_op( 277003785, 'CONCATENATED_OPERATION_27700_3785', TFM_PLAN(SDO_TFM_CHAIN(27700, -19916, 4277, 1000000000, 4055, 19847, 3785)), NULL);
commit; 
