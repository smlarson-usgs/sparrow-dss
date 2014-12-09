OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staid filler
,mrb_id
,MRB filler
,reachcode_comb filler
,station_id 
,station_name filler
,lat
,lon
,inc_area filler
,tot_area filler
,mean_flow filler
,obsyld filler
,arcid filler
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
,BPT_MUN_SMJ filler
,BPT_MUN_CB filler
,BPT_MUN_NWENG filler
,BPT_MUN_DELHUD filler
,BCMAQ2002KG filler
,BURBLAND_SN filler
,BFERTROTATION filler
,BFERT_OTHER filler
,BMANUREALL filler
,BLPRECIP filler
,BLTEMP filler
,BLROCKDEPL filler
,BLPERML filler
,BOVERLANDFLOWDIST filler
,BLKFACT_UP filler
,BLRECHARGE filler
,BBFI_MEAN filler
,BLCECL filler
,BLAWCL filler
,BLSLOPEL filler
,BLGAPVAL_9224 filler
,BLGAPVAL_9501 filler
,BLGAPVAL_9240 filler
,BLGAPVAL_9214 filler
,BLCES203_375AC filler
,BLCES203_375A filler
,BLCES203_265 filler
,BLCES203_267 filler
,BLCES203_304A filler
,BLCES411_381 filler
,BLGAPVAL_9213 filler
,BLGAPVAL_9212 filler
,BLCES203_304B filler
,BLCES_384_384A filler
,BLCES203_384A filler
,BLCES203_251 filler
,BLCES203_077 filler
,BLCES203_493 filler
,BLCES203_247A filler
,BLCES203_250 filler
,BLCES203_249 filler
,BLCES203_489A filler
,BLCES203_559 filler
,BLCES202_706 filler
,BLCES202_323 filler
,BLCES203_501 filler
,BLGAPVAL_9843 filler
,BLCES_9801_247A filler
,BLGAPVAL_9818 filler
,BLGAPVAL_9819 filler
,BLGAPVAL_9820 filler
,BLCES_9841_323 filler
,BRCHTOT1_LT70 filler
,BRCHTOT2_30100 filler
,BRCHTOT3_GE100 filler
,BR203_501 filler
,BR9801_247A filler
,BR9841_323 filler
,BINVERSEHYDLOAD filler
,id filler
)
