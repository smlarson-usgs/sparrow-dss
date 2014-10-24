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
,LOAD_A_00600 filler
,PLOAD_TOTAL
,PLOAD_BNDRYN filler
,PLOAD_CANBAS filler
,PLOAD_ATLOAD filler
,PLOAD_PLOADN filler
,PLOAD_FARMN filler
,PLOAD_MANUREN filler
,PLOAD_DEVEL filler
,PLOAD_WFOREST filler
,PLOAD_EFOREST filler
,PLOAD_ALDER filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_BNDRYN filler
,PLOAD_ND_CANBAS filler
,PLOAD_ND_ATLOAD filler
,PLOAD_ND_PLOADN filler
,PLOAD_ND_FARMN filler
,PLOAD_ND_MANUREN filler
,PLOAD_ND_DEVEL filler
,PLOAD_ND_WFOREST filler
,PLOAD_ND_EFOREST filler
,PLOAD_ND_ALDER filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_BNDRYN filler
,PLOAD_INC_CANBAS filler
,PLOAD_INC_ATLOAD filler
,PLOAD_INC_PLOADN filler
,PLOAD_INC_FARMN filler
,PLOAD_INC_MANUREN filler
,PLOAD_INC_DEVEL filler
,PLOAD_INC_WFOREST filler
,PLOAD_INC_EFOREST filler
,PLOAD_INC_ALDER filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_BNDRYN filler
,ci_hi_PLOAD_BNDRYN filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_CANBAS filler
,ci_hi_PLOAD_CANBAS filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_ATLOAD filler
,ci_hi_PLOAD_ATLOAD filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_PLOADN filler
,ci_hi_PLOAD_PLOADN filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_FARMN filler
,ci_hi_PLOAD_FARMN filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_MANUREN filler
,ci_hi_PLOAD_MANUREN filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_DEVEL filler
,ci_hi_PLOAD_DEVEL filler
,MEAN_PLOAD_08
,SE_PLOAD_08
,ci_lo_PLOAD_WFOREST filler
,ci_hi_PLOAD_WFOREST filler
,MEAN_PLOAD_09
,SE_PLOAD_09
,ci_lo_PLOAD_EFOREST filler
,ci_hi_PLOAD_EFOREST filler
,MEAN_PLOAD_10
,SE_PLOAD_10
,ci_lo_PLOAD_ALDER filler
,ci_hi_PLOAD_ALDER filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_BNDRYN filler
,SE_PLOAD_ND_BNDRYN filler
,ci_lo_PLOAD_ND_BNDRYN filler
,ci_hi_PLOAD_ND_BNDRYN filler
,MEAN_PLOAD_ND_CANBAS filler
,SE_PLOAD_ND_CANBAS filler
,ci_lo_PLOAD_ND_CANBAS filler
,ci_hi_PLOAD_ND_CANBAS filler
,MEAN_PLOAD_ND_ATLOAD filler
,SE_PLOAD_ND_ATLOAD filler
,ci_lo_PLOAD_ND_ATLOAD filler
,ci_hi_PLOAD_ND_ATLOAD filler
,MEAN_PLOAD_ND_PLOADN filler
,SE_PLOAD_ND_PLOADN filler
,ci_lo_PLOAD_ND_PLOADN filler
,ci_hi_PLOAD_ND_PLOADN filler
,MEAN_PLOAD_ND_FARMN filler
,SE_PLOAD_ND_FARMN filler
,ci_lo_PLOAD_ND_FARMN filler
,ci_hi_PLOAD_ND_FARMN filler
,MEAN_PLOAD_ND_MANUREN filler
,SE_PLOAD_ND_MANUREN filler
,ci_lo_PLOAD_ND_MANUREN filler
,ci_hi_PLOAD_ND_MANUREN filler
,MEAN_PLOAD_ND_DEVEL filler
,SE_PLOAD_ND_DEVEL filler
,ci_lo_PLOAD_ND_DEVEL filler
,ci_hi_PLOAD_ND_DEVEL filler
,MEAN_PLOAD_ND_WFOREST filler
,SE_PLOAD_ND_WFOREST filler
,ci_lo_PLOAD_ND_WFOREST filler
,ci_hi_PLOAD_ND_WFOREST filler
,MEAN_PLOAD_ND_EFOREST filler
,SE_PLOAD_ND_EFOREST filler
,ci_lo_PLOAD_ND_EFOREST filler
,ci_hi_PLOAD_ND_EFOREST filler
,MEAN_PLOAD_ND_ALDER filler
,SE_PLOAD_ND_ALDER filler
,ci_lo_PLOAD_ND_ALDER filler
,ci_hi_PLOAD_ND_ALDER filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_BNDRYN filler
,ci_hi_PLOAD_INC_BNDRYN filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_CANBAS filler
,ci_hi_PLOAD_INC_CANBAS filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_ATLOAD filler
,ci_hi_PLOAD_INC_ATLOAD filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_PLOADN filler
,ci_hi_PLOAD_INC_PLOADN filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_FARMN filler
,ci_hi_PLOAD_INC_FARMN filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_MANUREN filler
,ci_hi_PLOAD_INC_MANUREN filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_DEVEL filler
,ci_hi_PLOAD_INC_DEVEL filler
,MEAN_PLOAD_INC_08
,SE_PLOAD_INC_08
,ci_lo_PLOAD_INC_WFOREST filler
,ci_hi_PLOAD_INC_WFOREST filler
,MEAN_PLOAD_INC_09
,SE_PLOAD_INC_09
,ci_lo_PLOAD_INC_EFOREST filler
,ci_hi_PLOAD_INC_EFOREST filler
,MEAN_PLOAD_INC_10
,SE_PLOAD_INC_10
,ci_lo_PLOAD_INC_ALDER filler
,ci_hi_PLOAD_INC_ALDER filler
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
,sh_bndryn filler
,sh_canbas filler
,sh_atload filler
,sh_ploadn filler
,sh_farmn filler
,sh_manuren filler
,sh_devel filler
,sh_wforest filler
,sh_eforest filler
,sh_alder filler
)
