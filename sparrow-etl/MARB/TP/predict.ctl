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
,frac filler
,station_id filler
,staid filler
,huc filler
,huc2 filler
,state1 filler
,goolsby filler
,demtarea filler
,demiarea filler
,meanq filler
,arcnum filler
,fnode filler
,tnode filler
,hydseq filler
,iftran filler
,delivery_target filler
,ls_weight filler
,STAIDNUM filler
,LOAD_A_00665 filler
,PLOAD_TOTAL
,PLOAD_PCS_P02_S filler
,PLOAD_MANT_P filler
,PLOAD_FARM_P filler
,PLOAD_WETWOOD filler
,PLOAD_URBAN filler
,PLOAD_CHANNEL filler
,PLOAD_SGEOI_ES filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_PCS_P02_S filler
,PLOAD_ND_MANT_P filler
,PLOAD_ND_FARM_P filler
,PLOAD_ND_WETWOOD filler
,PLOAD_ND_URBAN filler
,PLOAD_ND_CHANNEL filler
,PLOAD_ND_SGEOI_ES filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_PCS_P02_S filler
,PLOAD_INC_MANT_P filler
,PLOAD_INC_FARM_P filler
,PLOAD_INC_WETWOOD filler
,PLOAD_INC_URBAN filler
,PLOAD_INC_CHANNEL filler
,PLOAD_INC_SGEOI_ES filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_PCS_P02_S filler
,ci_hi_PLOAD_PCS_P02_S filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_MANT_P filler
,ci_hi_PLOAD_MANT_P filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_FARM_P filler
,ci_hi_PLOAD_FARM_P filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_WETWOOD filler
,ci_hi_PLOAD_WETWOOD filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_URBAN filler
,ci_hi_PLOAD_URBAN filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_CHANNEL filler
,ci_hi_PLOAD_CHANNEL filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_SGEOI_ES filler
,ci_hi_PLOAD_SGEOI_ES filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_PCS_P02_S filler
,SE_PLOAD_ND_PCS_P02_S filler
,ci_lo_PLOAD_ND_PCS_P02_S filler
,ci_hi_PLOAD_ND_PCS_P02_S filler
,MEAN_PLOAD_ND_MANT_P filler
,SE_PLOAD_ND_MANT_P filler
,ci_lo_PLOAD_ND_MANT_P filler
,ci_hi_PLOAD_ND_MANT_P filler
,MEAN_PLOAD_ND_FARM_P filler
,SE_PLOAD_ND_FARM_P filler
,ci_lo_PLOAD_ND_FARM_P filler
,ci_hi_PLOAD_ND_FARM_P filler
,MEAN_PLOAD_ND_WETWOOD filler
,SE_PLOAD_ND_WETWOOD filler
,ci_lo_PLOAD_ND_WETWOOD filler
,ci_hi_PLOAD_ND_WETWOOD filler
,MEAN_PLOAD_ND_URBAN filler
,SE_PLOAD_ND_URBAN filler
,ci_lo_PLOAD_ND_URBAN filler
,ci_hi_PLOAD_ND_URBAN filler
,MEAN_PLOAD_ND_CHANNEL filler
,SE_PLOAD_ND_CHANNEL filler
,ci_lo_PLOAD_ND_CHANNEL filler
,ci_hi_PLOAD_ND_CHANNEL filler
,MEAN_PLOAD_ND_SGEOI_ES filler
,SE_PLOAD_ND_SGEOI_ES filler
,ci_lo_PLOAD_ND_SGEOI_ES filler
,ci_hi_PLOAD_ND_SGEOI_ES filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_PCS_P02_S filler
,ci_hi_PLOAD_INC_PCS_P02_S filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_MANT_P filler
,ci_hi_PLOAD_INC_MANT_P filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_FARM_P filler
,ci_hi_PLOAD_INC_FARM_P filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_WETWOOD filler
,ci_hi_PLOAD_INC_WETWOOD filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_URBAN filler
,ci_hi_PLOAD_INC_URBAN filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_CHANNEL filler
,ci_hi_PLOAD_INC_CHANNEL filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_SGEOI_ES filler
,ci_hi_PLOAD_INC_SGEOI_ES filler
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
,sh_pcs_p02_s filler
,sh_manT_p filler
,sh_Farm_P filler
,sh_wetwood filler
,sh_urban filler
,sh_channel filler
,sh_sgeoI_es filler
)
