OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_ENH_REACH_ATTRIB
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(fnode filler
,tnode filler
,LPOLY_ filler
,RPOLY_ filler
,reach_length
,NHDWFLOW_M filler
,NHDWFLOW_1 filler
,FDATE filler
,RESOLUTION filler
,GNIS_ID filler
,GNIS_NAME filler
,LENGTHKM filler
,REACHCODE filler
,FLOWDIR filler
,WBAREACOMI filler
,FTYPEfiller
,FCODE filler
,SHAPE_LENG filler
,ENABLED filler
,CHK_FLOW filler
,CASE_ filler
,FREQUENCY filler
,GRID_CODE filler
,cum_catch_area
,VALUE filler
,COUNT filler
,PROD_UNIT filler
,NEWID filler
,FROMNODE filler
,TONODE filler
,hydseq filler
,DIVERGENCE filler
,AREAWTMAP filler
,AREAWTMAT filler
,H2 filler
,H4 filler
,TOTUP_DA filler
,COUNT100M2 filler
,MEAN_ANN_P filler
,MEAN_ANN_T filler
,HUC8
,HUC6	
)