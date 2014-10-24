OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(monitoring_station_00600 filler
,station_id
,station_name
,altsite_id_00600 filler
,flow_station_id_00600 filler
,area_00600 filler
,flow_station_area_00600 filler
,altsite_area_00600 filler
,lat
,lon
,reach
,ls_weight_00600 filler
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
,BCBP_NPOINT_02 filler
,BTOTAL_NFERTFIX filler
,BTOT_MANURE_N filler
,BTIN_02 filler
,BURBAN_KM2 filler
,BLMEAN_ANN_EVI filler
,BLRCHRG_MOD filler
,BLAWC filler
,BLPERPCA filler
,BIRESHLOAD_N1 filler
,BRCHTOT_SMALL filler
,BRCHTOT_LGWRM filler
,BRCHTOT_LGCLD filler
,id filler
)
