OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(monitoring_station_00600 filler
,station_id
,station_name
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
,BCBP_PPOINT_02 filler
,BTOTAL_PFERT filler
,BTOT_MANURE_P filler
,BURBAN_KM2 filler
,BSILICICL filler
,BX_ROCK filler
,BKFACT filler
,BLSOILA filler
,BLAND_CP filler
,BLPRECIP filler
,BRCHTOT_P1 filler
,BIRESHLOAD filler
,id filler
)
