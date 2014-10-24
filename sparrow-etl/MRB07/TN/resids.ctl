OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staid_num filler
,station_id
,station_name
,demtarea filler
,meanq filler
,lat
,lon
,mrb_id
,MRB7_ERF1_NUM filler
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
,BBOUNDARY_P filler
,BCANBASIN filler
,BPS_P_02_ALL filler
,BTOTAL_AG_P filler
,BNLCD_DEVEL_KM2 filler
,BNATURAL_P_LOAD filler
,BLN_SOIL_PERMAVE filler
,BLN_PREC_EFF_CM filler
,BHLR_HUMTPSI2_PCT filler
,BRCHDECAY1 filler
,BRCHDECAY2 filler
,BIRESLOAD filler
,id filler
)
