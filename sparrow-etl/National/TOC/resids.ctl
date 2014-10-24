OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staid filler
,station_id
,station_name
,dectime_start filler
,dectime_end filler
,SE_60000 filler
,demtarea filler
,meanq filler
,RUNOFF filler
,ARATIO filler
,program filler
,AGRIC filler
,FOREST filler
,URBAN filler
,SHRUB filler
,GRASS filler
,POPDEN filler
,lat
,lon
,mrb_id
,arcnum filler
,ls_weight filler
,NESTED_AREA filler
,ACTUAL
,PREDICT filler
,LN_ACTUAL filler
,LN_PREDICT filler
,LN_PRED_YIELD filler
,LN_RESID filler
,WEIGHTED_LN_RESID filler
,MAP_RESID filler
,BOOT_RESID filler
,LEVERAGE filler
,Z_MAP_RESID filler
,BSUM90POP filler
,BNO3DEP filler
,BWFCORNSOY_NALL filler
,BWFALF_NALL filler
,BWFWHEAT_N filler
,BWFOTHER_N filler
,BTOTAL_WASTE filler
,BFOREST filler
,BBARREN filler
,BSHRUB filler
,BTILES_PERCENT filler
,BLN_S_PERM150 filler
,BTEMP filler
,BD_PPT1K filler
,BT_LN_SCA filler
,BLN_DRAINDEN filler
,BKFACTOR filler
,BMASSTRAN filler
,BRESLOAD1 filler
,id filler
)
