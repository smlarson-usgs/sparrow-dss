OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staid filler
,mrb_id :waterid
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
,BLCES203_251
,BLCES203_077
,BLCES203_493
,BLCES203_247A
,BLCES203_250
,BLCES203_249
,BLCES203_489A
,BLCES203_559
,BLCES202_706
,BLCES202_323
,BLCES203_501
,BLGAPVAL_9843
,BLCES_9801_247A
,BLGAPVAL_9818
,BLGAPVAL_9819
,BLGAPVAL_9820
,BLCES_9841_323
,BRCHTOT1_LT70
,BRCHTOT2_30100
,BRCHTOT3_GE100
,BR203_501
,BR9801_247A
,BR9841_323
,BINVERSEHYDLOAD
,id
)
