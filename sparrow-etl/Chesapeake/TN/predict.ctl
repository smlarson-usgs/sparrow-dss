OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id ":gridcode"
,reachtype filler
,demtarea filler
,demiarea filler
,maflowu filler
,cfromnode filler
,ctonode filler
,hydroseq filler
,mrb1_frac filler
,iftran filler
,terminalfl filler
,ls_weight_00600 filler
,LU_class filler
,MONITORING_STATION_00600 filler
,OBSERVED_N filler
,PLOAD_TOTAL
,PLOAD_CBP_NPOINT_02 filler
,PLOAD_TOTAL_NFERTFIX filler
,PLOAD_TOT_MANURE_N filler
,PLOAD_TIN_02 filler
,PLOAD_URBAN_KM2 filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_CBP_NPOINT_02 filler
,PLOAD_ND_TOTAL_NFERTFIX filler
,PLOAD_ND_TOT_MANURE_N filler
,PLOAD_ND_TIN_02 filler
,PLOAD_ND_URBAN_KM2 filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_CBP_NPOINT_02 filler
,PLOAD_INC_TOTAL_NFERTFIX filler
,PLOAD_INC_TOT_MANURE_N filler
,PLOAD_INC_TIN_02 filler
,PLOAD_INC_URBAN_KM2 filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_CBP_NPOINT_02 filler
,ci_hi_PLOAD_CBP_NPOINT_02 filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_TOTAL_NFERTFIX filler
,ci_hi_PLOAD_TOTAL_NFERTFIX filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_TOT_MANURE_N filler
,ci_hi_PLOAD_TOT_MANURE_N filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_TIN_02 filler
,ci_hi_PLOAD_TIN_02 filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_URBAN_KM2 filler
,ci_hi_PLOAD_URBAN_KM2 filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_CBP_NPOINT_02 filler
,SE_PLOAD_ND_CBP_NPOINT_02 filler
,ci_lo_PLOAD_ND_CBP_NPOINT_02 filler
,ci_hi_PLOAD_ND_CBP_NPOINT_02 filler
,MEAN_PLOAD_ND_TOTAL_NFERTFIX filler
,SE_PLOAD_ND_TOTAL_NFERTFIX filler
,ci_lo_PLOAD_ND_TOTAL_NFERTFIX filler
,ci_hi_PLOAD_ND_TOTAL_NFERTFIX filler
,MEAN_PLOAD_ND_TOT_MANURE_N filler
,SE_PLOAD_ND_TOT_MANURE_N filler
,ci_lo_PLOAD_ND_TOT_MANURE_N filler
,ci_hi_PLOAD_ND_TOT_MANURE_N filler
,MEAN_PLOAD_ND_TIN_02 filler
,SE_PLOAD_ND_TIN_02 filler
,ci_lo_PLOAD_ND_TIN_02 filler
,ci_hi_PLOAD_ND_TIN_02 filler
,MEAN_PLOAD_ND_URBAN_KM2 filler
,SE_PLOAD_ND_URBAN_KM2 filler
,ci_lo_PLOAD_ND_URBAN_KM2 filler
,ci_hi_PLOAD_ND_URBAN_KM2 filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_CBP_NPOINT_02 filler
,ci_hi_PLOAD_INC_CBP_NPOINT_02 filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_TOTAL_NFERTFIX filler
,ci_hi_PLOAD_INC_TOTAL_NFERTFIX filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_TOT_MANURE_N filler
,ci_hi_PLOAD_INC_TOT_MANURE_N filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_TIN_02 filler
,ci_hi_PLOAD_INC_TIN_02 filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_URBAN_KM2 filler
,ci_hi_PLOAD_INC_URBAN_KM2 filler
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
,sh_cbp_npoint_02 filler
,sh_total_nfertfix filler
,sh_tot_manure_n filler
,sh_tin_02 filler
,sh_urban_km2 filler
,GRIDCODE boundfiller
)

