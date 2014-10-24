OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staid filler
,station_id
,station_name
,region filler
,demtarea filler
,meanq filler
,SE_66500 filler
,RUNOFF filler
,aratio filler
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
,BWFCORNSOY_P filler
,BWFALF_P filler
,BWFOTHER_P filler
,BTOTAL_WASTE filler
,BFOREST filler
,BBARRENTRANS filler
,BSHRUB filler
,BTILES_PERCENT filler
,BLN_S_PERM150 filler
,BLN_T_SLODINF filler
,BD_PPT1K filler
,BT_LN_SCA filler
,BMASSTRAN filler
,BRESLOAD1 filler
,id filler

)
