OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id
,pname filler
,rchtype filler
,headflag filler
,termflag filler
,station_id filler
,demtarea filler
,demiarea filler
,meanq filler
,MRB7_ERF1_NUM filler
,fnode filler
,tnode filler
,hydseq filler
,frac filler
,iftran filler
,delivery_target filler
,ls_weight filler
,STAID_NUM filler
,LOAD_A_00665 filler
,PLOAD_TOTAL
,PLOAD_BOUNDARY_P filler
,PLOAD_CANBASIN filler
,PLOAD_PS_P_02_ALL filler
,PLOAD_TOTAL_AG_P filler
,PLOAD_NLCD_DEVEL_KM2 filler
,PLOAD_NATURAL_P_LOAD filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_BOUNDARY_P filler
,PLOAD_ND_CANBASIN filler
,PLOAD_ND_PS_P_02_ALL filler
,PLOAD_ND_TOTAL_AG_P filler
,PLOAD_ND_NLCD_DEVEL_KM2 filler
,PLOAD_ND_NATURAL_P_LOAD filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_BOUNDARY_P filler
,PLOAD_INC_CANBASIN filler
,PLOAD_INC_PS_P_02_ALL filler
,PLOAD_INC_TOTAL_AG_P filler
,PLOAD_INC_NLCD_DEVEL_KM2 filler
,PLOAD_INC_NATURAL_P_LOAD filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_BOUNDARY_P filler
,ci_hi_PLOAD_BOUNDARY_P filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_CANBASIN filler
,ci_hi_PLOAD_CANBASIN filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_PS_P_02_ALL filler
,ci_hi_PLOAD_PS_P_02_ALL filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_TOTAL_AG_P filler
,ci_hi_PLOAD_TOTAL_AG_P filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_NLCD_DEVEL_KM2 filler
,ci_hi_PLOAD_NLCD_DEVEL_KM2 filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_NATURAL_P_LOAD filler
,ci_hi_PLOAD_NATURAL_P_LOAD filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_BOUNDARY_P filler
,SE_PLOAD_ND_BOUNDARY_P filler
,ci_lo_PLOAD_ND_BOUNDARY_P filler
,ci_hi_PLOAD_ND_BOUNDARY_P filler
,MEAN_PLOAD_ND_CANBASIN filler
,SE_PLOAD_ND_CANBASIN filler
,ci_lo_PLOAD_ND_CANBASIN filler
,ci_hi_PLOAD_ND_CANBASIN filler
,MEAN_PLOAD_ND_PS_P_02_ALL filler
,SE_PLOAD_ND_PS_P_02_ALL filler
,ci_lo_PLOAD_ND_PS_P_02_ALL filler
,ci_hi_PLOAD_ND_PS_P_02_ALL filler
,MEAN_PLOAD_ND_TOTAL_AG_P filler
,SE_PLOAD_ND_TOTAL_AG_P filler
,ci_lo_PLOAD_ND_TOTAL_AG_P filler
,ci_hi_PLOAD_ND_TOTAL_AG_P filler
,MEAN_PLOAD_ND_NLCD_DEVEL_KM2 filler
,SE_PLOAD_ND_NLCD_DEVEL_KM2 filler
,ci_lo_PLOAD_ND_NLCD_DEVEL_KM2 filler
,ci_hi_PLOAD_ND_NLCD_DEVEL_KM2 filler
,MEAN_PLOAD_ND_NATURAL_P_LOAD filler
,SE_PLOAD_ND_NATURAL_P_LOAD filler
,ci_lo_PLOAD_ND_NATURAL_P_LOAD filler
,ci_hi_PLOAD_ND_NATURAL_P_LOAD filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_BOUNDARY_P filler
,ci_hi_PLOAD_INC_BOUNDARY_P filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_CANBASIN filler
,ci_hi_PLOAD_INC_CANBASIN filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_PS_P_02_ALL filler
,ci_hi_PLOAD_INC_PS_P_02_ALL filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_TOTAL_AG_P filler
,ci_hi_PLOAD_INC_TOTAL_AG_P filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_NLCD_DEVEL_KM2 filler
,ci_hi_PLOAD_INC_NLCD_DEVEL_KM2 filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_NATURAL_P_LOAD filler
,ci_hi_PLOAD_INC_NATURAL_P_LOAD filler
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
,sh_boundary_p filler
,sh_canbasin filler
,sh_ps_p_02_all filler
,sh_total_ag_p filler
,sh_nlcd_devel_km2 filler
,sh_natural_p_load filler
)
