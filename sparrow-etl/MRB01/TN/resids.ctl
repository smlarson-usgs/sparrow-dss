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
,BNSEWER filler
,BCORN_SOY_ALFNF filler
,BTIN_02 filler
,BALFSOYFIX filler
,BDEVEL filler
,BMAN_N filler
,BOTHER_NFERT filler
,BNP filler
,BRV filler
,BLAWC filler
,BLATRATIO filler
,BLBFI filler
,BLTEMP filler
,BMEANFLOLEN filler
,BPDMNT filler
,BNECZ filler
,BMDACP filler
,BSOPLS filler
,BEGLHL filler
,BATLCP filler
,BRCHTOT1 filler
,BRCHTOT2 filler
,BRCHTOT3 filler
,BRESTOT filler
,id filler
)
