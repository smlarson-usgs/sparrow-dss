OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id
,pname  filler
,rchtype  filler
,headflag  filler
,termflag  filler
,staid  filler
,length  filler
,demtarea  filler
,demiarea  filler
,meanq  filler
,arcid  filler
,fnode  filler
,tnode  filler
,hydseq  filler
,frac  filler
,iftran  filler
,delivery_target  filler
,ls_weight2  filler
,TP_LOAD_02  filler
,PLOAD_TOTAL
,PLOAD_TPBACK  filler
,PLOAD_PCS_P  filler
,PLOAD_CONC_AG_P  filler
,PLOAD_MAN_P  filler
,PLOAD_UNCONF_P  filler
,PLOAD_DEVLAND_KM2  filler
,PLOAD_FOREST_KM2  filler
,PLOAD_PLANTED_KM2  filler
,PLOAD_MANURE_P  filler
,PLOAD_ND_TOTAL  filler
,PLOAD_ND_TPBACK  filler
,PLOAD_ND_PCS_P  filler
,PLOAD_ND_CONC_AG_P  filler
,PLOAD_ND_MAN_P  filler
,PLOAD_ND_UNCONF_P  filler
,PLOAD_ND_DEVLAND_KM2  filler
,PLOAD_ND_FOREST_KM2  filler
,PLOAD_ND_PLANTED_KM2  filler
,PLOAD_ND_MANURE_P  filler
,PLOAD_INC_TOTAL  filler
,PLOAD_INC_TPBACK  filler
,PLOAD_INC_PCS_P  filler
,PLOAD_INC_CONC_AG_P  filler
,PLOAD_INC_MAN_P  filler
,PLOAD_INC_UNCONF_P  filler
,PLOAD_INC_DEVLAND_KM2  filler
,PLOAD_INC_FOREST_KM2  filler
,PLOAD_INC_PLANTED_KM2  filler
,PLOAD_INC_MANURE_P  filler
,RES_DECAY  filler
,DEL_FRAC  filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL  filler
,ci_hi_PLOAD_TOTAL  filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_TPBACK  filler
,ci_hi_PLOAD_TPBACK  filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_PCS_P  filler
,ci_hi_PLOAD_PCS_P  filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_CONC_AG_P  filler
,ci_hi_PLOAD_CONC_AG_P  filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_MAN_P  filler
,ci_hi_PLOAD_MAN_P  filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_UNCONF_P  filler
,ci_hi_PLOAD_UNCONF_P  filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_DEVLAND_KM2  filler
,ci_hi_PLOAD_DEVLAND_KM2  filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_FOREST_KM2  filler
,ci_hi_PLOAD_FOREST_KM2  filler
,MEAN_PLOAD_08
,SE_PLOAD_08
,ci_lo_PLOAD_PLANTED_KM2  filler
,ci_hi_PLOAD_PLANTED_KM2  filler
,MEAN_PLOAD_09
,SE_PLOAD_09
,ci_lo_PLOAD_MANURE_P  filler
,ci_hi_PLOAD_MANURE_P  filler
,MEAN_PLOAD_ND_TOTAL  filler
,SE_PLOAD_ND_TOTAL  filler
,ci_lo_PLOAD_ND_TOTAL  filler
,ci_hi_PLOAD_ND_TOTAL  filler
,MEAN_PLOAD_ND_TPBACK  filler
,SE_PLOAD_ND_TPBACK  filler
,ci_lo_PLOAD_ND_TPBACK  filler
,ci_hi_PLOAD_ND_TPBACK  filler
,MEAN_PLOAD_ND_PCS_P  filler
,SE_PLOAD_ND_PCS_P  filler
,ci_lo_PLOAD_ND_PCS_P  filler
,ci_hi_PLOAD_ND_PCS_P  filler
,MEAN_PLOAD_ND_CONC_AG_P  filler
,SE_PLOAD_ND_CONC_AG_P  filler
,ci_lo_PLOAD_ND_CONC_AG_P  filler
,ci_hi_PLOAD_ND_CONC_AG_P  filler
,MEAN_PLOAD_ND_MAN_P  filler
,SE_PLOAD_ND_MAN_P  filler
,ci_lo_PLOAD_ND_MAN_P  filler
,ci_hi_PLOAD_ND_MAN_P  filler
,MEAN_PLOAD_ND_UNCONF_P  filler
,SE_PLOAD_ND_UNCONF_P  filler
,ci_lo_PLOAD_ND_UNCONF_P  filler
,ci_hi_PLOAD_ND_UNCONF_P  filler
,MEAN_PLOAD_ND_DEVLAND_KM2  filler
,SE_PLOAD_ND_DEVLAND_KM2  filler
,ci_lo_PLOAD_ND_DEVLAND_KM2  filler
,ci_hi_PLOAD_ND_DEVLAND_KM2  filler
,MEAN_PLOAD_ND_FOREST_KM2  filler
,SE_PLOAD_ND_FOREST_KM2  filler
,ci_lo_PLOAD_ND_FOREST_KM2  filler
,ci_hi_PLOAD_ND_FOREST_KM2  filler
,MEAN_PLOAD_ND_PLANTED_KM2  filler
,SE_PLOAD_ND_PLANTED_KM2  filler
,ci_lo_PLOAD_ND_PLANTED_KM2  filler
,ci_hi_PLOAD_ND_PLANTED_KM2  filler
,MEAN_PLOAD_ND_MANURE_P  filler
,SE_PLOAD_ND_MANURE_P  filler
,ci_lo_PLOAD_ND_MANURE_P  filler
,ci_hi_PLOAD_ND_MANURE_P  filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL  filler
,ci_hi_PLOAD_INC_TOTAL  filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_TPBACK  filler
,ci_hi_PLOAD_INC_TPBACK  filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_PCS_P  filler
,ci_hi_PLOAD_INC_PCS_P  filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_CONC_AG_P  filler
,ci_hi_PLOAD_INC_CONC_AG_P  filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_MAN_P  filler
,ci_hi_PLOAD_INC_MAN_P  filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_UNCONF_P  filler
,ci_hi_PLOAD_INC_UNCONF_P  filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_DEVLAND_KM2  filler
,ci_hi_PLOAD_INC_DEVLAND_KM2  filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_FOREST_KM2  filler
,ci_hi_PLOAD_INC_FOREST_KM2  filler
,MEAN_PLOAD_INC_08
,SE_PLOAD_INC_08
,ci_lo_PLOAD_INC_PLANTED_KM2  filler
,ci_hi_PLOAD_INC_PLANTED_KM2  filler
,MEAN_PLOAD_INC_09
,SE_PLOAD_INC_09
,ci_lo_PLOAD_INC_MANURE_P  filler
,ci_hi_PLOAD_INC_MANURE_P  filler
,MEAN_RES_DECAY  filler
,SE_RES_DECAY  filler
,ci_lo_RES_DECAY  filler
,ci_hi_RES_DECAY  filler
,MEAN_DEL_FRAC  filler
,SE_DEL_FRAC  filler
,ci_lo_DEL_FRAC  filler
,ci_hi_DEL_FRAC  filler
,total_yield  filler
,inc_total_yield  filler
,concentration  filler
,map_del_frac  filler
,sh_TPback  filler
,sh_PCS_p  filler
,sh_conc_ag_p  filler
,sh_Man_P  filler
,sh_UNCONF_p  filler
,sh_devland_km2  filler
,sh_forest_km2  filler
,sh_planted_km2  filler
,sh_manure_p  filler
)
