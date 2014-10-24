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
,LOAD_A_00665 filler
,PLOAD_TOTAL
,PLOAD_PSEWER filler
,PLOAD_FOREST filler
,PLOAD_CORN_SOY_ALFPF filler
,PLOAD_DEVEL filler
,PLOAD_MAN_P filler
,PLOAD_OTHER_PFERT filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_PSEWER filler
,PLOAD_ND_FOREST filler
,PLOAD_ND_CORN_SOY_ALFPF filler
,PLOAD_ND_DEVEL filler
,PLOAD_ND_MAN_P filler
,PLOAD_ND_OTHER_PFERT filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_PSEWER filler
,PLOAD_INC_FOREST filler
,PLOAD_INC_CORN_SOY_ALFPF filler
,PLOAD_INC_DEVEL filler
,PLOAD_INC_MAN_P filler
,PLOAD_INC_OTHER_PFERT filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_PSEWER filler
,ci_hi_PLOAD_PSEWER filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_FOREST filler
,ci_hi_PLOAD_FOREST filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_CORN_SOY_ALFPF filler
,ci_hi_PLOAD_CORN_SOY_ALFPF filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_DEVEL filler
,ci_hi_PLOAD_DEVEL filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_MAN_P filler
,ci_hi_PLOAD_MAN_P filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_OTHER_PFERT filler
,ci_hi_PLOAD_OTHER_PFERT filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_PSEWER filler
,SE_PLOAD_ND_PSEWER filler
,ci_lo_PLOAD_ND_PSEWER filler
,ci_hi_PLOAD_ND_PSEWER filler
,MEAN_PLOAD_ND_FOREST filler
,SE_PLOAD_ND_FOREST filler
,ci_lo_PLOAD_ND_FOREST filler
,ci_hi_PLOAD_ND_FOREST filler
,MEAN_PLOAD_ND_CORN_SOY_ALFPF filler
,SE_PLOAD_ND_CORN_SOY_ALFPF filler
,ci_lo_PLOAD_ND_CORN_SOY_ALFPF filler
,ci_hi_PLOAD_ND_CORN_SOY_ALFPF filler
,MEAN_PLOAD_ND_DEVEL filler
,SE_PLOAD_ND_DEVEL filler
,ci_lo_PLOAD_ND_DEVEL filler
,ci_hi_PLOAD_ND_DEVEL filler
,MEAN_PLOAD_ND_MAN_P filler
,SE_PLOAD_ND_MAN_P filler
,ci_lo_PLOAD_ND_MAN_P filler
,ci_hi_PLOAD_ND_MAN_P filler
,MEAN_PLOAD_ND_OTHER_PFERT filler
,SE_PLOAD_ND_OTHER_PFERT filler
,ci_lo_PLOAD_ND_OTHER_PFERT filler
,ci_hi_PLOAD_ND_OTHER_PFERT filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_PSEWER filler
,ci_hi_PLOAD_INC_PSEWER filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_FOREST filler
,ci_hi_PLOAD_INC_FOREST filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_CORN_SOY_ALFPF filler
,ci_hi_PLOAD_INC_CORN_SOY_ALFPF filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_DEVEL filler
,ci_hi_PLOAD_INC_DEVEL filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_MAN_P filler
,ci_hi_PLOAD_INC_MAN_P filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_OTHER_PFERT filler
,ci_hi_PLOAD_INC_OTHER_PFERT filler
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
,sh_pSEWER filler
,sh_forest filler
,sh_corn_soy_alfpf filler
,sh_DEVEL filler
,sh_man_p filler
,sh_OTHER_pFERT filler
,gridcode boundfiller
)

