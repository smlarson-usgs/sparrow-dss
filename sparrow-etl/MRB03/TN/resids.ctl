OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staidnum filler
,station_id
,station_name
,demtarea2 filler
,meanq filler
,SLOAD_A_00600 filler
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
,BPCS_N02_T filler
,BTIN_02 filler
,BMANC_N filler
,BFARM_N filler
,BFIXATION filler
,BLDRAINDEN filler
,BPPT30MEAN filler
,BMEANTEMP filler
,BTILES_PERC filler
,BSOIL_CLAYAVE filler
,BRCHDECAY1 filler
,BRCHDECAY2 filler
,BRCHDECAY3 filler
,BRESDECAY filler
,id filler
)
