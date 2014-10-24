OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staidnum filler
,demtarea filler
,meanq filler
,station_id
,sep_pct filler
,station_name
,huc filler
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
,BUMISSTN filler
,BATMDEP filler
,BPOINT filler
,BRESIDENT filler
,BCONMANUREN filler
,BUNMANUREN filler
,BFERTN filler
,BINFILX filler
,BPRECIP filler
,BRCHDECAY1 filler
,BRCHDECAY2 filler
,BRCHDECAY3 filler
,BRESDECAY filler
,id filler
)
