OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(statid filler
,station_id
,station_name
,demtarea filler
,meanq filler
,pct_error filler
,n_predict_days filler
,n_wq_obs_80154 filler
,pct_along filler
,detrend_code filler
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
,BURBAN filler
,BFOREST filler
,BFEDLAND filler
,BAGLAND filler
,BOTHER filler
,BRCHLEN filler
,BPERM filler
,BSLOPE filler
,BRFACT filler
,BKFACT filler
,BLMEANQ1 filler
,BLMEANQ2 filler
,BRCHTOT1 filler
,BRCHTOT2 filler
,BRCHTOT3 filler
,BRES_SETVEL filler
,id filler
)
