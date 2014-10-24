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
,headflag filler
,meanq filler
,huc4 filler
,huc8 filler
,rr filler
,area filler
,obsyld filler
,obsconc filler
,sload_a_60000 filler
,tn_evaluation filler
,method_60000 filler
,n_wq_obs_60000 filler
,recordcover_60000 filler
,n_predict_yrs filler
,flow_trend_peryr filler
,ratio_df_60000 filler
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
,bnadpkg filler
,bimpsur_km filler
,bffert_02 filler
,bman_02 filler
,blperml filler
,blsaqcondct2 filler
,bkfact_up filler
,blrockdepl filler
,bsndl filler
,blprecip_mm filler
,bhlper1 filler
,bhlper2 filler
,bhlper3 filler
,bhlper4 filler
,bhlper6911 filler
,bhlper7 filler
,bhlper9 filler
,bhlper11 filler
,bhlper12 filler
,bhlper16 filler
,brchdecay12 filler
,brchdecay3 filler
,bresdecay filler
,id filler
)
