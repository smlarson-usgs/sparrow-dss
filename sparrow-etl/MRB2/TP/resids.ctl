OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_RESIDS"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(staidnum filler
,station_id
,station_name
,demtarea filler
,sqkm filler
,meanq filler
,huc4 filler
,dominanthydroland filler
,area filler
,obsyld filler
,obsconc filler
,sload_a_66500 filler
,tp_evaluation filler
,method_66500 filler
,se_pct_66500 filler
,n_wq_obs_66500 filler
,recordcover_66500 filler
,n_predict_yrs filler
,flow_trend_peryr filler
,ratio_df_66500 filler
,mean_dflow_por filler
,basin_delineation filler
,lat
,lon
,mrb_id
,arcnum filler
,ls_weight filler
,nested_area filler
,actual
,predict filler
,ln_actual filler
,ln_predict filler
,ln_pred_yield filler
,ln_resid filler
,weighted_ln_resid filler
,map_resid filler
,boot_resid filler
,leverage filler
,z_map_resid filler
,bpoint filler
,bdev filler
,bnatural filler
,bmines filler
,bmant filler
,bagr filler
,bkfact filler
,bprecip_mm filler
,bomh filler
,bwtdh filler
,bph filler
,bcontrchdecay filler
,bresdecay filler
,id filler

)
