OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_HUC_LIST
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id filler
,comid
,permieter filler
,huc_2 filler
,huc_4 filler
,huc_6 filler
,huc8_int 
,huc8_char filler
,huc_name filler
)
