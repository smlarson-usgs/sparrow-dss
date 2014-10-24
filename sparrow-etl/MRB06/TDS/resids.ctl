OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staid  filler
,STATION_ID
,STATION_NAME
,DEMTAREA filler
,MEANQ filler
,lat
,lon
,mrb_id
,arcnum filler
,WT filler
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
,BCRYSTALINE filler
,BMVOLC filler
,BFVOLC filler
,BEUGEOSYN filler
,BCENOHI filler
,BCENOLO filler
,BMESOHI filler
,BMESOMED filler
,BMESOLO filler
,BPALEOPREHI filler
,BPALEOPREMED filler
,BPALEOPRELO filler
,BCULTIV filler
,BPASTURE filler
,BIMPORT filler
,BLRUNOFF filler
,BLDRAINDEN filler
,BLBARRENPCT filler
,BRCHDECAY1 filler
,BRCHDECAY2 filler
,id filler
)
