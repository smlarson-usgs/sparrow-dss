OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id
,MRB  filler
,reachcode_comb filler
,huc8 filler
,headflag filler
,termflag filler
,GNIS_NAME filler
,MAVELU filler
,EstTarget_Code filler
,EstTarget_Name filler
,ReachTOT filler
,ReachType filler
,WBRchCd filler
,length_m filler
,local_id filler
,std_id filler
,new_or_modified filler
,hydseq filler
,tot_area filler
,inc_area filler
,mean_flow filler
,arcid filler
,fnode filler
,tnode filler
,frac filler
,iftran filler
,target filler
,ls_weight filler
,STAID filler
,DEPVAR filler
,PLOAD_TOTAL
,PLOAD_PT_MUN_SMJ filler
,PLOAD_PT_MUN_CB filler
,PLOAD_PT_MUN_NWENG filler
,PLOAD_PT_MUN_DELHUD filler
,PLOAD_CMAQ2002KG filler
,PLOAD_URBLAND_SN filler
,PLOAD_FERTROTATION filler
,PLOAD_FERT_OTHER filler
,PLOAD_MANUREALL filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_PT_MUN_SMJ filler
,PLOAD_ND_PT_MUN_CB filler
,PLOAD_ND_PT_MUN_NWENG filler
,PLOAD_ND_PT_MUN_DELHUD filler
,PLOAD_ND_CMAQ2002KG filler
,PLOAD_ND_URBLAND_SN filler
,PLOAD_ND_FERTROTATION filler
,PLOAD_ND_FERT_OTHER filler
,PLOAD_ND_MANUREALL filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_PT_MUN_SMJ filler
,PLOAD_INC_PT_MUN_CB filler
,PLOAD_INC_PT_MUN_NWENG filler
,PLOAD_INC_PT_MUN_DELHUD filler
,PLOAD_INC_CMAQ2002KG filler
,PLOAD_INC_URBLAND_SN filler
,PLOAD_INC_FERTROTATION filler
,PLOAD_INC_FERT_OTHER filler
,PLOAD_INC_MANUREALL filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_PT_MUN_SMJ filler
,ci_hi_PLOAD_PT_MUN_SMJ filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_PT_MUN_CB filler
,ci_hi_PLOAD_PT_MUN_CB filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_PT_MUN_NWENG filler
,ci_hi_PLOAD_PT_MUN_NWENG filler
,MEAN_PLOAD_04 
,SE_PLOAD_04
,ci_lo_PLOAD_PT_MUN_DELHUD filler
,ci_hi_PLOAD_PT_MUN_DELHUD filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_CMAQ2002KG filler
,ci_hi_PLOAD_CMAQ2002KG filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_URBLAND_SN filler
,ci_hi_PLOAD_URBLAND_SN filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_FERTROTATION filler
,ci_hi_PLOAD_FERTROTATION filler
,MEAN_PLOAD_08
,SE_PLOAD_08
,ci_lo_PLOAD_FERT_OTHER filler
,ci_hi_PLOAD_FERT_OTHER filler
,MEAN_PLOAD_09
,SE_PLOAD_09
,ci_lo_PLOAD_MANUREALL filler
,ci_hi_PLOAD_MANUREALL filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_PT_MUN_SMJ filler
,SE_PLOAD_ND_PT_MUN_SMJ filler
,ci_lo_PLOAD_ND_PT_MUN_SMJ filler
,ci_hi_PLOAD_ND_PT_MUN_SMJ filler
,MEAN_PLOAD_ND_PT_MUN_CB filler
,SE_PLOAD_ND_PT_MUN_CB filler
,ci_lo_PLOAD_ND_PT_MUN_CB filler
,ci_hi_PLOAD_ND_PT_MUN_CB filler
,MEAN_PLOAD_ND_PT_MUN_NWENG filler
,SE_PLOAD_ND_PT_MUN_NWENG filler
,ci_lo_PLOAD_ND_PT_MUN_NWENG filler
,ci_hi_PLOAD_ND_PT_MUN_NWENG filler
,MEAN_PLOAD_ND_PT_MUN_DELHUD filler
,SE_PLOAD_ND_PT_MUN_DELHUD filler
,ci_lo_PLOAD_ND_PT_MUN_DELHUD filler
,ci_hi_PLOAD_ND_PT_MUN_DELHUD filler
,MEAN_PLOAD_ND_CMAQ2002KG filler
,SE_PLOAD_ND_CMAQ2002KG filler
,ci_lo_PLOAD_ND_CMAQ2002KG filler
,ci_hi_PLOAD_ND_CMAQ2002KG filler
,MEAN_PLOAD_ND_URBLAND_SN filler
,SE_PLOAD_ND_URBLAND_SN filler
,ci_lo_PLOAD_ND_URBLAND_SN filler
,ci_hi_PLOAD_ND_URBLAND_SN filler
,MEAN_PLOAD_ND_FERTROTATION filler
,SE_PLOAD_ND_FERTROTATION filler
,ci_lo_PLOAD_ND_FERTROTATION filler
,ci_hi_PLOAD_ND_FERTROTATION filler
,MEAN_PLOAD_ND_FERT_OTHER filler
,SE_PLOAD_ND_FERT_OTHER filler
,ci_lo_PLOAD_ND_FERT_OTHER filler
,ci_hi_PLOAD_ND_FERT_OTHER filler
,MEAN_PLOAD_ND_MANUREALL filler
,SE_PLOAD_ND_MANUREALL filler
,ci_lo_PLOAD_ND_MANUREALL filler
,ci_hi_PLOAD_ND_MANUREALL filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_PT_MUN_SMJ filler
,ci_hi_PLOAD_INC_PT_MUN_SMJ filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_PT_MUN_CB filler
,ci_hi_PLOAD_INC_PT_MUN_CB filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_PT_MUN_NWENG filler
,ci_hi_PLOAD_INC_PT_MUN_NWENG filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_PT_MUN_DELHUD filler
,ci_hi_PLOAD_INC_PT_MUN_DELHUD filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_CMAQ2002KG filler
,ci_hi_PLOAD_INC_CMAQ2002KG filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_URBLAND_SN filler
,ci_hi_PLOAD_INC_URBLAND_SN filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_FERTROTATION filler
,ci_hi_PLOAD_INC_FERTROTATION filler
,MEAN_PLOAD_INC_08
,SE_PLOAD_INC_08
,ci_lo_PLOAD_INC_FERT_OTHER filler
,ci_hi_PLOAD_INC_FERT_OTHER filler
,MEAN_PLOAD_INC_09
,SE_PLOAD_INC_09
,ci_lo_PLOAD_INC_MANUREALL filler
,ci_hi_PLOAD_INC_MANUREALL filler
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
,sh_PT_MUN_SMJ filler
,sh_PT_MUN_CB filler
,sh_PT_MUN_NWENG filler
,sh_PT_MUN_DELHUD filler
,sh_CMAQ2002KG filler
,sh_UrbLand_SN filler
,sh_FertRotation filler
,sh_Fert_other filler
,sh_ManureAll filler
)
