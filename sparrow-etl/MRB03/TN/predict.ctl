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
,gltribnum filler
,grlake filler
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
,LOAD_A_00600 filler
,PLOAD_TOTAL
,PLOAD_PCS_N02_T filler
,PLOAD_TIN_02 filler
,PLOAD_MANC_N filler
,PLOAD_FARM_N filler
,PLOAD_FIXATION filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_PCS_N02_T filler
,PLOAD_ND_TIN_02 filler
,PLOAD_ND_MANC_N filler
,PLOAD_ND_FARM_N filler
,PLOAD_ND_FIXATION filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_PCS_N02_T filler
,PLOAD_INC_TIN_02 filler
,PLOAD_INC_MANC_N filler
,PLOAD_INC_FARM_N filler
,PLOAD_INC_FIXATION filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_PCS_N02_T filler
,ci_hi_PLOAD_PCS_N02_T filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_TIN_02 filler
,ci_hi_PLOAD_TIN_02 filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_MANC_N filler
,ci_hi_PLOAD_MANC_N filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_FARM_N filler
,ci_hi_PLOAD_FARM_N filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_FIXATION filler
,ci_hi_PLOAD_FIXATION filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_PCS_N02_T filler
,SE_PLOAD_ND_PCS_N02_T filler
,ci_lo_PLOAD_ND_PCS_N02_T filler
,ci_hi_PLOAD_ND_PCS_N02_T filler
,MEAN_PLOAD_ND_TIN_02 filler
,SE_PLOAD_ND_TIN_02 filler
,ci_lo_PLOAD_ND_TIN_02 filler
,ci_hi_PLOAD_ND_TIN_02 filler
,MEAN_PLOAD_ND_MANC_N filler
,SE_PLOAD_ND_MANC_N filler
,ci_lo_PLOAD_ND_MANC_N filler
,ci_hi_PLOAD_ND_MANC_N filler
,MEAN_PLOAD_ND_FARM_N filler
,SE_PLOAD_ND_FARM_N filler
,ci_lo_PLOAD_ND_FARM_N filler
,ci_hi_PLOAD_ND_FARM_N filler
,MEAN_PLOAD_ND_FIXATION filler
,SE_PLOAD_ND_FIXATION filler
,ci_lo_PLOAD_ND_FIXATION filler
,ci_hi_PLOAD_ND_FIXATION filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_PCS_N02_T filler
,ci_hi_PLOAD_INC_PCS_N02_T filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_TIN_02 filler
,ci_hi_PLOAD_INC_TIN_02 filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_MANC_N filler
,ci_hi_PLOAD_INC_MANC_N filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_FARM_N filler
,ci_hi_PLOAD_INC_FARM_N filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_FIXATION filler
,ci_hi_PLOAD_INC_FIXATION filler
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
,sh_pcs_n02_t filler
,sh_TIN_02 filler
,sh_manc_n filler
,sh_Farm_n filler
,sh_fixation filler
)
