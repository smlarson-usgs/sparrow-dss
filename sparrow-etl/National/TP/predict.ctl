OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id
,rr filler
,pname filler
,rchtype filler
,headflag filler
,termflag filler
,station_id filler
,staid filler
,huc filler
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
,LOAD_A_66500 filler
,PLOAD_TOTAL
,PLOAD_SUM90POP filler
,PLOAD_WFCORNSOY_P filler
,PLOAD_WFALF_P filler
,PLOAD_WFOTHER_P filler
,PLOAD_TOTAL_WASTE filler
,PLOAD_FOREST filler
,PLOAD_BARRENTRANS filler
,PLOAD_SHRUB filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_SUM90POP filler
,PLOAD_ND_WFCORNSOY_P filler
,PLOAD_ND_WFALF_P filler
,PLOAD_ND_WFOTHER_P filler
,PLOAD_ND_TOTAL_WASTE filler
,PLOAD_ND_FOREST filler
,PLOAD_ND_BARRENTRANS filler
,PLOAD_ND_SHRUB filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_SUM90POP filler
,PLOAD_INC_WFCORNSOY_P filler
,PLOAD_INC_WFALF_P filler
,PLOAD_INC_WFOTHER_P filler
,PLOAD_INC_TOTAL_WASTE filler
,PLOAD_INC_FOREST filler
,PLOAD_INC_BARRENTRANS filler
,PLOAD_INC_SHRUB filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_SUM90POP filler
,ci_hi_PLOAD_SUM90POP filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_WFCORNSOY_P filler
,ci_hi_PLOAD_WFCORNSOY_P filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_WFALF_P filler
,ci_hi_PLOAD_WFALF_P filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_WFOTHER_P filler
,ci_hi_PLOAD_WFOTHER_P filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_TOTAL_WASTE filler
,ci_hi_PLOAD_TOTAL_WASTE filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_FOREST filler
,ci_hi_PLOAD_FOREST filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_BARRENTRANS filler
,ci_hi_PLOAD_BARRENTRANS filler
,MEAN_PLOAD_08
,SE_PLOAD_08
,ci_lo_PLOAD_SHRUB filler
,ci_hi_PLOAD_SHRUB filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_SUM90POP filler
,SE_PLOAD_ND_SUM90POP filler
,ci_lo_PLOAD_ND_SUM90POP filler
,ci_hi_PLOAD_ND_SUM90POP filler
,MEAN_PLOAD_ND_WFCORNSOY_P filler
,SE_PLOAD_ND_WFCORNSOY_P filler
,ci_lo_PLOAD_ND_WFCORNSOY_P filler
,ci_hi_PLOAD_ND_WFCORNSOY_P filler
,MEAN_PLOAD_ND_WFALF_P filler
,SE_PLOAD_ND_WFALF_P filler
,ci_lo_PLOAD_ND_WFALF_P filler
,ci_hi_PLOAD_ND_WFALF_P filler
,MEAN_PLOAD_ND_WFOTHER_P filler
,SE_PLOAD_ND_WFOTHER_P filler
,ci_lo_PLOAD_ND_WFOTHER_P filler
,ci_hi_PLOAD_ND_WFOTHER_P filler
,MEAN_PLOAD_ND_TOTAL_WASTE filler
,SE_PLOAD_ND_TOTAL_WASTE filler
,ci_lo_PLOAD_ND_TOTAL_WASTE filler
,ci_hi_PLOAD_ND_TOTAL_WASTE filler
,MEAN_PLOAD_ND_FOREST filler
,SE_PLOAD_ND_FOREST filler
,ci_lo_PLOAD_ND_FOREST filler
,ci_hi_PLOAD_ND_FOREST filler
,MEAN_PLOAD_ND_BARRENTRANS filler
,SE_PLOAD_ND_BARRENTRANS filler
,ci_lo_PLOAD_ND_BARRENTRANS filler
,ci_hi_PLOAD_ND_BARRENTRANS filler
,MEAN_PLOAD_ND_SHRUB filler
,SE_PLOAD_ND_SHRUB filler
,ci_lo_PLOAD_ND_SHRUB filler
,ci_hi_PLOAD_ND_SHRUB filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_SUM90POP filler
,ci_hi_PLOAD_INC_SUM90POP filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_WFCORNSOY_P filler
,ci_hi_PLOAD_INC_WFCORNSOY_P filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_WFALF_P filler
,ci_hi_PLOAD_INC_WFALF_P filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_WFOTHER_P filler
,ci_hi_PLOAD_INC_WFOTHER_P filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_TOTAL_WASTE filler
,ci_hi_PLOAD_INC_TOTAL_WASTE filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_FOREST filler
,ci_hi_PLOAD_INC_FOREST filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_BARRENTRANS filler
,ci_hi_PLOAD_INC_BARRENTRANS filler
,MEAN_PLOAD_INC_08
,SE_PLOAD_INC_08
,ci_lo_PLOAD_INC_SHRUB filler
,ci_hi_PLOAD_INC_SHRUB filler
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
,sh_SUM90POP filler
,sh_WFCORNSOY_P filler
,sh_WFALF_P filler
,sh_WFOTHER_P filler
,sh_TOTAL_WASTE filler
,sh_FOREST filler
,sh_BARRENTRANS filler
,sh_SHRUB filler
)
