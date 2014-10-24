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
,WT filler
,LU_class filler
,DEPVARKG filler
,PLOAD_TOTAL
,PLOAD_CRYSTALINE filler
,PLOAD_MVOLC filler
,PLOAD_FVOLC filler
,PLOAD_EUGEOSYN filler
,PLOAD_CENOHI filler
,PLOAD_CENOLO filler
,PLOAD_MESOHI filler
,PLOAD_MESOMED filler
,PLOAD_MESOLO filler
,PLOAD_PALEOPREHI filler
,PLOAD_PALEOPREMED filler
,PLOAD_PALEOPRELO filler
,PLOAD_CULTIV filler
,PLOAD_PASTURE filler
,PLOAD_IMPORT filler
,PLOAD_ND_TOTAL filler
,PLOAD_ND_CRYSTALINE filler
,PLOAD_ND_MVOLC filler
,PLOAD_ND_FVOLC filler
,PLOAD_ND_EUGEOSYN filler
,PLOAD_ND_CENOHI filler
,PLOAD_ND_CENOLO filler
,PLOAD_ND_MESOHI filler
,PLOAD_ND_MESOMED filler
,PLOAD_ND_MESOLO filler
,PLOAD_ND_PALEOPREHI filler
,PLOAD_ND_PALEOPREMED filler
,PLOAD_ND_PALEOPRELO filler
,PLOAD_ND_CULTIV filler
,PLOAD_ND_PASTURE filler
,PLOAD_ND_IMPORT filler
,PLOAD_INC_TOTAL filler
,PLOAD_INC_CRYSTALINE filler
,PLOAD_INC_MVOLC filler
,PLOAD_INC_FVOLC filler
,PLOAD_INC_EUGEOSYN filler
,PLOAD_INC_CENOHI filler
,PLOAD_INC_CENOLO filler
,PLOAD_INC_MESOHI filler
,PLOAD_INC_MESOMED filler
,PLOAD_INC_MESOLO filler
,PLOAD_INC_PALEOPREHI filler
,PLOAD_INC_PALEOPREMED filler
,PLOAD_INC_PALEOPRELO filler
,PLOAD_INC_CULTIV filler
,PLOAD_INC_PASTURE filler
,PLOAD_INC_IMPORT filler
,RES_DECAY filler
,DEL_FRAC filler
,MEAN_PLOAD_TOTAL
,SE_PLOAD_TOTAL
,ci_lo_PLOAD_TOTAL filler
,ci_hi_PLOAD_TOTAL filler
,MEAN_PLOAD_01
,SE_PLOAD_01
,ci_lo_PLOAD_CRYSTALINE filler
,ci_hi_PLOAD_CRYSTALINE filler
,MEAN_PLOAD_02
,SE_PLOAD_02
,ci_lo_PLOAD_MVOLC filler
,ci_hi_PLOAD_MVOLC filler
,MEAN_PLOAD_03
,SE_PLOAD_03
,ci_lo_PLOAD_FVOLC filler
,ci_hi_PLOAD_FVOLC filler
,MEAN_PLOAD_04
,SE_PLOAD_04
,ci_lo_PLOAD_EUGEOSYN filler
,ci_hi_PLOAD_EUGEOSYN filler
,MEAN_PLOAD_05
,SE_PLOAD_05
,ci_lo_PLOAD_CENOHI filler
,ci_hi_PLOAD_CENOHI filler
,MEAN_PLOAD_06
,SE_PLOAD_06
,ci_lo_PLOAD_CENOLO filler
,ci_hi_PLOAD_CENOLO filler
,MEAN_PLOAD_07
,SE_PLOAD_07
,ci_lo_PLOAD_MESOHI filler
,ci_hi_PLOAD_MESOHI filler
,MEAN_PLOAD_08
,SE_PLOAD_08
,ci_lo_PLOAD_MESOMED filler
,ci_hi_PLOAD_MESOMED filler
,MEAN_PLOAD_09
,SE_PLOAD_09
,ci_lo_PLOAD_MESOLO filler
,ci_hi_PLOAD_MESOLO filler
,MEAN_PLOAD_10
,SE_PLOAD_10
,ci_lo_PLOAD_PALEOPREHI filler
,ci_hi_PLOAD_PALEOPREHI filler
,MEAN_PLOAD_11
,SE_PLOAD_11
,ci_lo_PLOAD_PALEOPREMED filler
,ci_hi_PLOAD_PALEOPREMED filler
,MEAN_PLOAD_12
,SE_PLOAD_12
,ci_lo_PLOAD_PALEOPRELO filler
,ci_hi_PLOAD_PALEOPRELO filler
,MEAN_PLOAD_13
,SE_PLOAD_13
,ci_lo_PLOAD_CULTIV filler
,ci_hi_PLOAD_CULTIV filler
,MEAN_PLOAD_14
,SE_PLOAD_14
,ci_lo_PLOAD_PASTURE filler
,ci_hi_PLOAD_PASTURE filler
,MEAN_PLOAD_15
,SE_PLOAD_15
,ci_lo_PLOAD_IMPORT filler
,ci_hi_PLOAD_IMPORT filler
,MEAN_PLOAD_ND_TOTAL filler
,SE_PLOAD_ND_TOTAL filler
,ci_lo_PLOAD_ND_TOTAL filler
,ci_hi_PLOAD_ND_TOTAL filler
,MEAN_PLOAD_ND_CRYSTALINE filler
,SE_PLOAD_ND_CRYSTALINE filler
,ci_lo_PLOAD_ND_CRYSTALINE filler
,ci_hi_PLOAD_ND_CRYSTALINE filler
,MEAN_PLOAD_ND_MVOLC filler
,SE_PLOAD_ND_MVOLC filler
,ci_lo_PLOAD_ND_MVOLC filler
,ci_hi_PLOAD_ND_MVOLC filler
,MEAN_PLOAD_ND_FVOLC filler
,SE_PLOAD_ND_FVOLC filler
,ci_lo_PLOAD_ND_FVOLC filler
,ci_hi_PLOAD_ND_FVOLC filler
,MEAN_PLOAD_ND_EUGEOSYN filler
,SE_PLOAD_ND_EUGEOSYN filler
,ci_lo_PLOAD_ND_EUGEOSYN filler
,ci_hi_PLOAD_ND_EUGEOSYN filler
,MEAN_PLOAD_ND_CENOHI filler
,SE_PLOAD_ND_CENOHI filler
,ci_lo_PLOAD_ND_CENOHI filler
,ci_hi_PLOAD_ND_CENOHI filler
,MEAN_PLOAD_ND_CENOLO filler
,SE_PLOAD_ND_CENOLO filler
,ci_lo_PLOAD_ND_CENOLO filler
,ci_hi_PLOAD_ND_CENOLO filler
,MEAN_PLOAD_ND_MESOHI filler
,SE_PLOAD_ND_MESOHI filler
,ci_lo_PLOAD_ND_MESOHI filler
,ci_hi_PLOAD_ND_MESOHI filler
,MEAN_PLOAD_ND_MESOMED filler
,SE_PLOAD_ND_MESOMED filler
,ci_lo_PLOAD_ND_MESOMED filler
,ci_hi_PLOAD_ND_MESOMED filler
,MEAN_PLOAD_ND_MESOLO filler
,SE_PLOAD_ND_MESOLO filler
,ci_lo_PLOAD_ND_MESOLO filler
,ci_hi_PLOAD_ND_MESOLO filler
,MEAN_PLOAD_ND_PALEOPREHI filler
,SE_PLOAD_ND_PALEOPREHI filler
,ci_lo_PLOAD_ND_PALEOPREHI filler
,ci_hi_PLOAD_ND_PALEOPREHI filler
,MEAN_PLOAD_ND_PALEOPREMED filler
,SE_PLOAD_ND_PALEOPREMED filler
,ci_lo_PLOAD_ND_PALEOPREMED filler
,ci_hi_PLOAD_ND_PALEOPREMED filler
,MEAN_PLOAD_ND_PALEOPRELO filler
,SE_PLOAD_ND_PALEOPRELO filler
,ci_lo_PLOAD_ND_PALEOPRELO filler
,ci_hi_PLOAD_ND_PALEOPRELO filler
,MEAN_PLOAD_ND_CULTIV filler
,SE_PLOAD_ND_CULTIV filler
,ci_lo_PLOAD_ND_CULTIV filler
,ci_hi_PLOAD_ND_CULTIV filler
,MEAN_PLOAD_ND_PASTURE filler
,SE_PLOAD_ND_PASTURE filler
,ci_lo_PLOAD_ND_PASTURE filler
,ci_hi_PLOAD_ND_PASTURE filler
,MEAN_PLOAD_ND_IMPORT filler
,SE_PLOAD_ND_IMPORT filler
,ci_lo_PLOAD_ND_IMPORT filler
,ci_hi_PLOAD_ND_IMPORT filler
,MEAN_PLOAD_INC_TOTAL
,SE_PLOAD_INC_TOTAL
,ci_lo_PLOAD_INC_TOTAL filler
,ci_hi_PLOAD_INC_TOTAL filler
,MEAN_PLOAD_INC_01
,SE_PLOAD_INC_01
,ci_lo_PLOAD_INC_CRYSTALINE filler
,ci_hi_PLOAD_INC_CRYSTALINE filler
,MEAN_PLOAD_INC_02
,SE_PLOAD_INC_02
,ci_lo_PLOAD_INC_MVOLC filler
,ci_hi_PLOAD_INC_MVOLC filler
,MEAN_PLOAD_INC_03
,SE_PLOAD_INC_03
,ci_lo_PLOAD_INC_FVOLC filler
,ci_hi_PLOAD_INC_FVOLC filler
,MEAN_PLOAD_INC_04
,SE_PLOAD_INC_04
,ci_lo_PLOAD_INC_EUGEOSYN filler
,ci_hi_PLOAD_INC_EUGEOSYN filler
,MEAN_PLOAD_INC_05
,SE_PLOAD_INC_05
,ci_lo_PLOAD_INC_CENOHI filler
,ci_hi_PLOAD_INC_CENOHI filler
,MEAN_PLOAD_INC_06
,SE_PLOAD_INC_06
,ci_lo_PLOAD_INC_CENOLO filler
,ci_hi_PLOAD_INC_CENOLO filler
,MEAN_PLOAD_INC_07
,SE_PLOAD_INC_07
,ci_lo_PLOAD_INC_MESOHI filler
,ci_hi_PLOAD_INC_MESOHI filler
,MEAN_PLOAD_INC_08
,SE_PLOAD_INC_08
,ci_lo_PLOAD_INC_MESOMED filler
,ci_hi_PLOAD_INC_MESOMED filler
,MEAN_PLOAD_INC_09
,SE_PLOAD_INC_09
,ci_lo_PLOAD_INC_MESOLO filler
,ci_hi_PLOAD_INC_MESOLO filler
,MEAN_PLOAD_INC_10
,SE_PLOAD_INC_10
,ci_lo_PLOAD_INC_PALEOPREHI filler
,ci_hi_PLOAD_INC_PALEOPREHI filler
,MEAN_PLOAD_INC_11
,SE_PLOAD_INC_11
,ci_lo_PLOAD_INC_PALEOPREMED filler
,ci_hi_PLOAD_INC_PALEOPREMED filler
,MEAN_PLOAD_INC_12
,SE_PLOAD_INC_12
,ci_lo_PLOAD_INC_PALEOPRELO filler
,ci_hi_PLOAD_INC_PALEOPRELO filler
,MEAN_PLOAD_INC_13
,SE_PLOAD_INC_13
,ci_lo_PLOAD_INC_CULTIV filler
,ci_hi_PLOAD_INC_CULTIV filler
,MEAN_PLOAD_INC_14
,SE_PLOAD_INC_14
,ci_lo_PLOAD_INC_PASTURE filler
,ci_hi_PLOAD_INC_PASTURE filler
,MEAN_PLOAD_INC_15
,SE_PLOAD_INC_15
,ci_lo_PLOAD_INC_IMPORT filler
,ci_hi_PLOAD_INC_IMPORT filler
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
,sh_CRYSTALINE filler
,sh_MVOLC filler
,sh_FVOLC filler
,sh_EUGEOSYN filler
,sh_CENOHI filler
,sh_CENOLO filler
,sh_MESOHI filler
,sh_MESOMED filler
,sh_MESOLO filler
,sh_PALEOPREHI filler
,sh_PALEOPREMED filler
,sh_PALEOPRELO filler
,sh_CULTIV filler
,sh_PASTURE filler
,sh_IMPORT filler
)
