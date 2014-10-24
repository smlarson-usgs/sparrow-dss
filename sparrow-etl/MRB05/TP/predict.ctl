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
,edacda2use filler
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
,PLOAD_TP_UMLOAD filler
,PLOAD_PCS_P02_T filler
,PLOAD_RESIDENT filler
,PLOAD_FERTP filler
,PLOAD_MANUREP filler
,PLOAD_SLENGTH filler
,PLOAD_NLCDBACK filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_TP_UMLOAD filler
,PLOAD_ND_PCS_P02_T filler
,PLOAD_ND_RESIDENT filler
,PLOAD_ND_FERTP filler
,PLOAD_ND_MANUREP filler
,PLOAD_ND_SLENGTH filler
,PLOAD_ND_NLCDBACK filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_TP_UMLOAD filler
,PLOAD_INC_PCS_P02_T filler
,PLOAD_INC_RESIDENT filler
,PLOAD_INC_FERTP filler
,PLOAD_INC_MANUREP filler
,PLOAD_INC_SLENGTH filler
,PLOAD_INC_NLCDBACK filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_TP_UMLOAD filler
,ci_hi_PLOAD_TP_UMLOAD filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_PCS_P02_T filler
,ci_hi_PLOAD_PCS_P02_T filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_RESIDENT filler
,ci_hi_PLOAD_RESIDENT filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_FERTP filler
,ci_hi_PLOAD_FERTP filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_MANUREP filler
,ci_hi_PLOAD_MANUREP filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_SLENGTH filler
,ci_hi_PLOAD_SLENGTH filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_NLCDBACK filler
,ci_hi_PLOAD_NLCDBACK filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_TP_UMLOAD filler
,SE_PLOAD_ND_TP_UMLOAD filler
,ci_lo_PLOAD_ND_TP_UMLOAD filler
,ci_hi_PLOAD_ND_TP_UMLOAD filler
,MEAN_PLOAD_ND_PCS_P02_T filler
,SE_PLOAD_ND_PCS_P02_T filler
,ci_lo_PLOAD_ND_PCS_P02_T filler
,ci_hi_PLOAD_ND_PCS_P02_T filler
,MEAN_PLOAD_ND_RESIDENT filler
,SE_PLOAD_ND_RESIDENT filler
,ci_lo_PLOAD_ND_RESIDENT filler
,ci_hi_PLOAD_ND_RESIDENT filler
,MEAN_PLOAD_ND_FERTP filler
,SE_PLOAD_ND_FERTP filler
,ci_lo_PLOAD_ND_FERTP filler
,ci_hi_PLOAD_ND_FERTP filler
,MEAN_PLOAD_ND_MANUREP filler
,SE_PLOAD_ND_MANUREP filler
,ci_lo_PLOAD_ND_MANUREP filler
,ci_hi_PLOAD_ND_MANUREP filler
,MEAN_PLOAD_ND_SLENGTH filler
,SE_PLOAD_ND_SLENGTH filler
,ci_lo_PLOAD_ND_SLENGTH filler
,ci_hi_PLOAD_ND_SLENGTH filler
,MEAN_PLOAD_ND_NLCDBACK filler
,SE_PLOAD_ND_NLCDBACK filler
,ci_lo_PLOAD_ND_NLCDBACK filler
,ci_hi_PLOAD_ND_NLCDBACK filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_TP_UMLOAD filler
,ci_hi_PLOAD_INC_TP_UMLOAD filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_PCS_P02_T filler
,ci_hi_PLOAD_INC_PCS_P02_T filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_RESIDENT filler
,ci_hi_PLOAD_INC_RESIDENT filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_FERTP filler
,ci_hi_PLOAD_INC_FERTP filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_MANUREP filler
,ci_hi_PLOAD_INC_MANUREP filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_SLENGTH filler
,ci_hi_PLOAD_INC_SLENGTH filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_NLCDBACK filler
,ci_hi_PLOAD_INC_NLCDBACK filler
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
,sh_tp_umload filler
,sh_pcs_p02_t filler
,sh_resident filler
,sh_fertp filler
,sh_manurep filler
,sh_slength filler
,sh_nlcdback filler
)
