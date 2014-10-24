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
,staid filler
,meanq filler
,meanv filler
,rchtot filler
,hload filler
,rhload filler
,huc filler
,edacda2use filler
,demtarea filler
,demiarea filler
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
,PLOAD_TN_UMLOAD filler
,PLOAD_TIN_02_KG filler
,PLOAD_PCS_N02_T filler
,PLOAD_RESIDENT filler
,PLOAD_CONMANUREN filler
,PLOAD_MANU_N filler
,PLOAD_FERTN filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_TN_UMLOAD filler
,PLOAD_ND_TIN_02_KG filler
,PLOAD_ND_PCS_N02_T filler
,PLOAD_ND_RESIDENT filler
,PLOAD_ND_CONMANUREN filler
,PLOAD_ND_MANU_N filler
,PLOAD_ND_FERTN filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_TN_UMLOAD filler
,PLOAD_INC_TIN_02_KG filler
,PLOAD_INC_PCS_N02_T filler
,PLOAD_INC_RESIDENT filler
,PLOAD_INC_CONMANUREN filler
,PLOAD_INC_MANU_N filler
,PLOAD_INC_FERTN filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_TN_UMLOAD filler
,ci_hi_PLOAD_TN_UMLOAD filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_TIN_02_KG filler
,ci_hi_PLOAD_TIN_02_KG filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_PCS_N02_T filler
,ci_hi_PLOAD_PCS_N02_T filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_RESIDENT filler
,ci_hi_PLOAD_RESIDENT filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_CONMANUREN filler
,ci_hi_PLOAD_CONMANUREN filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_MANU_N filler
,ci_hi_PLOAD_MANU_N filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_FERTN filler
,ci_hi_PLOAD_FERTN filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_TN_UMLOAD filler
,SE_PLOAD_ND_TN_UMLOAD filler
,ci_lo_PLOAD_ND_TN_UMLOAD filler
,ci_hi_PLOAD_ND_TN_UMLOAD filler
,MEAN_PLOAD_ND_TIN_02_KG filler
,SE_PLOAD_ND_TIN_02_KG filler
,ci_lo_PLOAD_ND_TIN_02_KG filler
,ci_hi_PLOAD_ND_TIN_02_KG filler
,MEAN_PLOAD_ND_PCS_N02_T filler
,SE_PLOAD_ND_PCS_N02_T filler
,ci_lo_PLOAD_ND_PCS_N02_T filler
,ci_hi_PLOAD_ND_PCS_N02_T filler
,MEAN_PLOAD_ND_RESIDENT filler
,SE_PLOAD_ND_RESIDENT filler
,ci_lo_PLOAD_ND_RESIDENT filler
,ci_hi_PLOAD_ND_RESIDENT filler
,MEAN_PLOAD_ND_CONMANUREN filler
,SE_PLOAD_ND_CONMANUREN filler
,ci_lo_PLOAD_ND_CONMANUREN filler
,ci_hi_PLOAD_ND_CONMANUREN filler
,MEAN_PLOAD_ND_MANU_N filler
,SE_PLOAD_ND_MANU_N filler
,ci_lo_PLOAD_ND_MANU_N filler
,ci_hi_PLOAD_ND_MANU_N filler
,MEAN_PLOAD_ND_FERTN filler
,SE_PLOAD_ND_FERTN filler
,ci_lo_PLOAD_ND_FERTN filler
,ci_hi_PLOAD_ND_FERTN filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_TN_UMLOAD filler
,ci_hi_PLOAD_INC_TN_UMLOAD filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_TIN_02_KG filler
,ci_hi_PLOAD_INC_TIN_02_KG filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_PCS_N02_T filler
,ci_hi_PLOAD_INC_PCS_N02_T filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_RESIDENT filler
,ci_hi_PLOAD_INC_RESIDENT filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_CONMANUREN filler
,ci_hi_PLOAD_INC_CONMANUREN filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_MANU_N filler
,ci_hi_PLOAD_INC_MANU_N filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_FERTN filler
,ci_hi_PLOAD_INC_FERTN filler
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
,sh_tn_umload filler
,sh_tin_02_kg filler
,sh_pcs_n02_t filler
,sh_resident filler
,sh_conmanuren filler
,sh_manu_n filler
,sh_fertn filler
)
