OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(station_id
,demtarea filler
,tot_cfs filler
,lat
,lon
,reach
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
,BPSEWER filler
,BFOREST filler
,BCORN_SOY_ALFPF filler
,BDEVEL filler
,BMAN_P filler
,BOTHER_PFERT filler
,BLBFI filler
,BLBIOMASS filler
,BMFLOLEN filler
,BNP filler
,BRV filler
,BPDMNT filler
,BNECZ filler
,BMDACP filler
,BSOPLS filler
,BEGLHL filler
,BATLCP filler
,BRCHTOT1 filler
,BRCHTOT2 filler
,BRCHTOT3 filler
,BRESTOT1 filler
,BRESTOT2 filler
,id filler
)
