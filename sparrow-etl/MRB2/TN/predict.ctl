OPTIONS ( SKIP=1)
LOAD DATA
TRUNCATE
INTO TABLE "SPARROW_DSS"."TEMP_PREDICT"
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id
,headflag filler
,termflag filler
,pname filler
,rr filler
,huc8 filler
,huc6 filler
,edacode filler
,edaname filler
,rchtype filler
,rchtot filler
,meanv filler
,length_m filler
,station_id filler
,station_name filler
,basin_delineation filler
,ex_calib filler
,sqkm_revtofullpolygon filler
,demtarea_revtosqkm filler
,fullpolygon_notinmodel filler
,local_id filler
,std_id filler
,new_or_modified filler
,termrchforeda filler
,demtarea filler
,sqkm filler
,meanq filler
,arcnum filler
,fnode filler
,tnode filler
,hydseq filler
,frac filler
,iftran filler
,delivery_target filler
,ls_weight filler
,staidnum filler
,load_a_60000 filler
,pload_total
,pload_kgn_02 filler
,pload_nadp_kg filler
,pload_is_km_01nlcd filler
,pload_fert02_01nlcd filler
,pload_man02_01nlcd filler
,pload_nd_total filler
,pload_nd_kgn_02 filler
,pload_nd_nadp_kg filler
,pload_nd_is_km_01nlcd filler
,pload_nd_fert02_01nlcd filler
,pload_nd_man02_01nlcd filler
,pload_inc_total filler
,pload_inc_kgn_02 filler
,pload_inc_nadp_kg filler
,pload_inc_is_km_01nlcd filler
,pload_inc_fert02_01nlcd filler
,pload_inc_man02_01nlcd filler
,res_decay filler
,del_frac filler
,mean_pload_total
,se_pload_total
,ci_lo_pload_total filler
,ci_hi_pload_total filler
,mean_pload_01
,se_pload_01
,ci_lo_pload_kgn_02 filler
,ci_hi_pload_kgn_02 filler
,mean_pload_02
,se_pload_02
,ci_lo_pload_nadp_kg filler
,ci_hi_pload_nadp_kg filler
,mean_pload_03
,se_pload_03
,ci_lo_pload_is_km_01nlcd filler
,ci_hi_pload_is_km_01nlcd filler
,mean_pload_04
,se_pload_04
,ci_lo_pload_fert02_01nlcd filler
,ci_hi_pload_fert02_01nlcd filler
,mean_pload_05
,se_pload_05
,ci_lo_pload_man02_01nlcd filler
,ci_hi_pload_man02_01nlcd filler
,mean_pload_nd_total filler
,se_pload_nd_total filler
,ci_lo_pload_nd_total filler
,ci_hi_pload_nd_total filler
,mean_pload_nd_kgn_02 filler
,se_pload_nd_kgn_02 filler
,ci_lo_pload_nd_kgn_02 filler
,ci_hi_pload_nd_kgn_02 filler
,mean_pload_nd_nadp_kg filler
,se_pload_nd_nadp_kg filler
,ci_lo_pload_nd_nadp_kg filler
,ci_hi_pload_nd_nadp_kg filler
,mean_pload_nd_is_km_01nlcd filler
,se_pload_nd_is_km_01nlcd filler
,ci_lo_pload_nd_is_km_01nlcd filler
,ci_hi_pload_nd_is_km_01nlcd filler
,mean_pload_nd_fert02_01nlcd filler
,se_pload_nd_fert02_01nlcd filler
,ci_lo_pload_nd_fert02_01nlcd filler
,ci_hi_pload_nd_fert02_01nlcd filler
,mean_pload_nd_man02_01nlcd filler
,se_pload_nd_man02_01nlcd filler
,ci_lo_pload_nd_man02_01nlcd filler
,ci_hi_pload_nd_man02_01nlcd filler
,mean_pload_inc_total
,se_pload_inc_total
,ci_lo_pload_inc_total filler
,ci_hi_pload_inc_total filler
,mean_pload_inc_01
,se_pload_inc_01
,ci_lo_pload_inc_kgn_02 filler
,ci_hi_pload_inc_kgn_02 filler
,mean_pload_inc_02
,se_pload_inc_02
,ci_lo_pload_inc_nadp_kg filler
,ci_hi_pload_inc_nadp_kg filler
,mean_pload_inc_03
,se_pload_inc_03
,ci_lo_pload_inc_is_km_01nlcd filler
,ci_hi_pload_inc_is_km_01nlcd filler
,mean_pload_inc_04
,se_pload_inc_04
,ci_lo_pload_inc_fert02_01nlcd filler
,ci_hi_pload_inc_fert02_01nlcd filler
,mean_pload_inc_05
,se_pload_inc_05
,ci_lo_pload_inc_man02_01nlcd filler
,ci_hi_pload_inc_man02_01nlcd filler
,mean_res_decay filler
,se_res_decay filler
,ci_lo_res_decay filler
,ci_hi_res_decay filler
,mean_del_frac filler
,se_del_frac filler
,ci_lo_del_frac filler
,ci_hi_del_frac filler
,total_yield filler
,inc_total_yield filler
,concentration filler
,map_del_frac filler
,sh_kgn_02 filler
,sh_nadp_kg filler
,sh_is_km_01nlcd filler
,sh_fert02_01nlcd filler
,sh_man02_01nlcd filler
)
