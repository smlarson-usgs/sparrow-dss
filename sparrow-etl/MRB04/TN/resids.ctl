OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staid_n filler
,station_id
,station_name
,lon
,lat
,huc filler
,demtarea filler
,meanq filler
,nlcd_crops filler
,nlcd_pastr filler
,nlcd_forst filler
,nlcd_range filler
,nlcd_devlp filler
,nlcd_devlpol filler
,nlcd_devlpmh filler
,nlcd_totag filler
,nlcd_nonagurb filler
,nlcd_wetl filler
,SLOAD_A_00600 filler
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
,BTIN_02 filler
,BMAN_N filler
,BFARM_N filler
,BPCS_N filler
,BURBAN filler
,BLPPT30MEAN filler
,BIRRIG_PCT filler
,BTAVG30 filler
,BSGEO_LOESS2 filler
,BRCHDECAY1 filler
,BRCHDECAY2 filler
,BRESDECAY filler
,id filler
)
