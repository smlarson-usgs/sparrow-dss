OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_SRC_METADATA
TRUNCATE
FIELDS TERMINATED BY X'9'
(id
,sort_order
,source_name
,short_name "nvl(:short_name,:source_name)"
,long_name
,source_units "nvl(:source_units,'Unknown')"
,precision "nvl(:precision,'2')"
,is_point_source "nvl(:is_point_source,'F')"
,description
)
