OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(station_id
,station_name
,demtarea filler
,demiarea filler
,meanq filler
,lat
,lon
,mrb_id
,arcid filler
,ls_weight2 filler
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
,BTPBACK filler
,BPCS_P filler
,BCONC_AG_P filler
,BMAN_P filler
,BUNCONF_P filler
,BDEVLAND_KM2 filler
,BFOREST_KM2 filler
,BPLANTED_KM2 filler
,BMANURE_P filler
,BPRECIP_LN filler
,BSANDAVE filler
,BPERC_TILES filler
,BPERC_WETLANDS filler
,BCLAYAVE filler
,BKFACT_PERC filler
,BPERM_LN filler
,BKFACT filler
,BRCHDECAY1 filler
,BRCHDECAY2 filler
,BRCHDECAY3 filler
,BRCHDECAY4 filler
,BIRESLOAD filler
,id filler
)
