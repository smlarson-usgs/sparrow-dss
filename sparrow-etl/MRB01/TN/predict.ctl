OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id ":gridcode"
,ifres filler
,staid filler
,demtarea filler
,area filler
,TOT_CFS filler
,fnode2 filler
,tnode2 filler
,seqnum filler
,rfrac filler
,if_tran filler
,if_target filler
,ls_weight filler
,LOAD_A_00600 filler
,PLOAD_TOTAL
,PLOAD_NSEWER filler
,PLOAD_CORN_SOY_ALFNF filler
,PLOAD_TIN_02 filler
,PLOAD_ALFSOYFIX filler
,PLOAD_MAN_N filler
,PLOAD_DEVEL filler
,PLOAD_OTHER_NFERT filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_NSEWER filler
,PLOAD_ND_CORN_SOY_ALFNF filler
,PLOAD_ND_TIN_02 filler
,PLOAD_ND_ALFSOYFIX filler
,PLOAD_ND_MAN_N filler
,PLOAD_ND_DEVEL filler
,PLOAD_ND_OTHER_NFERT filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_NSEWER filler
,PLOAD_INC_CORN_SOY_ALFNF filler
,PLOAD_INC_TIN_02 filler
,PLOAD_INC_ALFSOYFIX filler
,PLOAD_INC_MAN_N filler
,PLOAD_INC_DEVEL filler
,PLOAD_INC_OTHER_NFERT filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_NSEWER filler
,ci_hi_PLOAD_NSEWER filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_CORN_SOY_ALFNF filler
,ci_hi_PLOAD_CORN_SOY_ALFNF filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_TIN_02 filler
,ci_hi_PLOAD_TIN_02 filler
,MEAN_PLOAD_04 filler
,SE_PLOAD_04 filler
,ci_lo_PLOAD_ALFSOYFIX filler
,ci_hi_PLOAD_ALFSOYFIX filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_MAN_N filler
,ci_hi_PLOAD_MAN_N filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_DEVEL filler
,ci_hi_PLOAD_DEVEL filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_OTHER_NFERT filler
,ci_hi_PLOAD_OTHER_NFERT filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_NSEWER filler
,SE_PLOAD_ND_NSEWER filler
,ci_lo_PLOAD_ND_NSEWER filler
,ci_hi_PLOAD_ND_NSEWER filler
,MEAN_PLOAD_ND_CORN_SOY_ALFNF filler
,SE_PLOAD_ND_CORN_SOY_ALFNF filler
,ci_lo_PLOAD_ND_CORN_SOY_ALFNF filler
,ci_hi_PLOAD_ND_CORN_SOY_ALFNF filler
,MEAN_PLOAD_ND_TIN_02 filler
,SE_PLOAD_ND_TIN_02 filler
,ci_lo_PLOAD_ND_TIN_02 filler
,ci_hi_PLOAD_ND_TIN_02 filler
,MEAN_PLOAD_ND_ALFSOYFIX filler
,SE_PLOAD_ND_ALFSOYFIX filler
,ci_lo_PLOAD_ND_ALFSOYFIX filler
,ci_hi_PLOAD_ND_ALFSOYFIX filler
,MEAN_PLOAD_ND_MAN_N filler
,SE_PLOAD_ND_MAN_N filler
,ci_lo_PLOAD_ND_MAN_N filler
,ci_hi_PLOAD_ND_MAN_N filler
,MEAN_PLOAD_ND_DEVEL filler
,SE_PLOAD_ND_DEVEL filler
,ci_lo_PLOAD_ND_DEVEL filler
,ci_hi_PLOAD_ND_DEVEL filler
,MEAN_PLOAD_ND_OTHER_NFERT filler
,SE_PLOAD_ND_OTHER_NFERT filler
,ci_lo_PLOAD_ND_OTHER_NFERT filler
,ci_hi_PLOAD_ND_OTHER_NFERT filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_NSEWER filler
,ci_hi_PLOAD_INC_NSEWER filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_CORN_SOY_ALFNF filler
,ci_hi_PLOAD_INC_CORN_SOY_ALFNF filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_TIN_02 filler
,ci_hi_PLOAD_INC_TIN_02 filler
,MEAN_PLOAD_INC_04 filler
,SE_PLOAD_INC_04 filler
,ci_lo_PLOAD_INC_ALFSOYFIX filler
,ci_hi_PLOAD_INC_ALFSOYFIX filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_MAN_N filler
,ci_hi_PLOAD_INC_MAN_N filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_DEVEL filler
,ci_hi_PLOAD_INC_DEVEL filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_OTHER_NFERT filler
,ci_hi_PLOAD_INC_OTHER_NFERT filler
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
,sh_NSEWER filler
,sh_corn_soy_alfnf filler
,sh_TIN_02 filler
,sh_alfsoyfix filler
,sh_man_n filler
,sh_DEVEL filler
,sh_OTHER_NFERT filler
,gridcode boundfiller
)

