OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id
,rr filler
,pname filler
,rescode filler
,rchtype filler
,headflag filler
,termflag filler
,station_id filler
,statid filler
,contflag filler
,regab filler
,est_cd filler
,demtarea filler
,demiarea filler
,meanq filler
,arcnum filler
,fnode filler
,tnode filler
,hydseq filler
,frac filler
,iftran filler
,delivery_target filler
,ls_weight filler
,LU_class filler
,LOAD_A_80154_S filler
,PLOAD_TOTAL
,PLOAD_URBAN_92 filler
,PLOAD_FOREST_92 filler
,PLOAD_FED filler
,PLOAD_CROPPASTOR_92 filler
,PLOAD_OTHER_92 filler
,PLOAD_RCHLEN filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_URBAN_92 filler
,PLOAD_ND_FOREST_92 filler
,PLOAD_ND_FED filler
,PLOAD_ND_CROPPASTOR_92 filler
,PLOAD_ND_OTHER_92 filler
,PLOAD_ND_RCHLEN filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_URBAN_92 filler
,PLOAD_INC_FOREST_92 filler
,PLOAD_INC_FED filler
,PLOAD_INC_CROPPASTOR_92 filler
,PLOAD_INC_OTHER_92 filler
,PLOAD_INC_RCHLEN filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_URBAN_92 filler
,ci_hi_PLOAD_URBAN_92 filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_FOREST_92 filler
,ci_hi_PLOAD_FOREST_92 filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_FED filler
,ci_hi_PLOAD_FED filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_CROPPASTOR_92 filler
,ci_hi_PLOAD_CROPPASTOR_92 filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_OTHER_92 filler
,ci_hi_PLOAD_OTHER_92 filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_RCHLEN filler
,ci_hi_PLOAD_RCHLEN filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_URBAN_92 filler
,SE_PLOAD_ND_URBAN_92 filler
,ci_lo_PLOAD_ND_URBAN_92 filler
,ci_hi_PLOAD_ND_URBAN_92 filler
,MEAN_PLOAD_ND_FOREST_92 filler
,SE_PLOAD_ND_FOREST_92 filler
,ci_lo_PLOAD_ND_FOREST_92 filler
,ci_hi_PLOAD_ND_FOREST_92 filler
,MEAN_PLOAD_ND_FED filler
,SE_PLOAD_ND_FED filler
,ci_lo_PLOAD_ND_FED filler
,ci_hi_PLOAD_ND_FED filler
,MEAN_PLOAD_ND_CROPPASTOR_92 filler
,SE_PLOAD_ND_CROPPASTOR_92 filler
,ci_lo_PLOAD_ND_CROPPASTOR_92 filler
,ci_hi_PLOAD_ND_CROPPASTOR_92 filler
,MEAN_PLOAD_ND_OTHER_92 filler
,SE_PLOAD_ND_OTHER_92 filler
,ci_lo_PLOAD_ND_OTHER_92 filler
,ci_hi_PLOAD_ND_OTHER_92 filler
,MEAN_PLOAD_ND_RCHLEN filler
,SE_PLOAD_ND_RCHLEN filler
,ci_lo_PLOAD_ND_RCHLEN filler
,ci_hi_PLOAD_ND_RCHLEN filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_URBAN_92 filler
,ci_hi_PLOAD_INC_URBAN_92 filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_FOREST_92 filler
,ci_hi_PLOAD_INC_FOREST_92 filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_FED filler
,ci_hi_PLOAD_INC_FED filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_CROPPASTOR_92 filler
,ci_hi_PLOAD_INC_CROPPASTOR_92 filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_OTHER_92 filler
,ci_hi_PLOAD_INC_OTHER_92 filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_RCHLEN filler
,ci_hi_PLOAD_INC_RCHLEN filler
,MEAN_RES_DECAY filler
,SE_RES_DECAY filler
,ci_lo_RES_DECAY filler
,ci_hi_RES_DECAY filler
,MEAN_DEL_FRAC filler
,SE_DEL_FRAC filler
,ci_lo_DEL_FRAC filler
,ci_hi_DEL_FRAC filler
,total_yield filler
,inc_total_yield filler
,concentration filler
,map_del_frac filler
,sh_urban_92 filler
,sh_forest_92 filler
,sh_fed filler
,sh_croppastor_92 filler
,sh_other_92 filler
,sh_rchlen filler
)
