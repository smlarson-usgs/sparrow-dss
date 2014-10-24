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
,code_finalcomb_huc4estuary filler
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
,load_a_66500 filler
,pload_total
,pload_kgp_02_v2 filler
,pload_lc2_sqkm filler
,pload_pnat_v17 filler
,pload_pmines_v17 filler
,pload_mant_p filler
,pload_lc8_sqkm filler
,pload_nd_total filler
,pload_nd_kgp_02_v2 filler
,pload_nd_lc2_sqkm filler
,pload_nd_pnat_v17 filler
,pload_nd_pmines_v17 filler
,pload_nd_mant_p filler
,pload_nd_lc8_sqkm filler
,pload_inc_total filler
,pload_inc_kgp_02_v2 filler
,pload_inc_lc2_sqkm filler
,pload_inc_pnat_v17 filler
,pload_inc_pmines_v17 filler
,pload_inc_mant_p filler
,pload_inc_lc8_sqkm filler
,res_decay filler
,del_frac filler
,mean_pload_total
,se_pload_total
,ci_lo_pload_total filler
,ci_hi_pload_total filler
,mean_pload_01
,se_pload_01
,ci_lo_pload_kgp_02_v2 filler
,ci_hi_pload_kgp_02_v2 filler
,mean_pload_02
,se_pload_02
,ci_lo_pload_lc2_sqkm filler
,ci_hi_pload_lc2_sqkm filler
,mean_pload_03
,se_pload_03
,ci_lo_pload_pnat_v17 filler
,ci_hi_pload_pnat_v17 filler
,mean_pload_04
,se_pload_04
,ci_lo_pload_pmines_v17 filler
,ci_hi_pload_pmines_v17 filler
,mean_pload_05
,se_pload_05
,ci_lo_pload_mant_p filler
,ci_hi_pload_mant_p filler
,mean_pload_06
,se_pload_06
,ci_lo_pload_lc8_sqkm filler
,ci_hi_pload_lc8_sqkm filler
,mean_pload_nd_total filler
,se_pload_nd_total filler
,ci_lo_pload_nd_total filler
,ci_hi_pload_nd_total filler
,mean_pload_nd_kgp_02_v2 filler
,se_pload_nd_kgp_02_v2 filler
,ci_lo_pload_nd_kgp_02_v2 filler
,ci_hi_pload_nd_kgp_02_v2 filler
,mean_pload_nd_lc2_sqkm filler
,se_pload_nd_lc2_sqkm filler
,ci_lo_pload_nd_lc2_sqkm filler
,ci_hi_pload_nd_lc2_sqkm filler
,mean_pload_nd_pnat_v17 filler
,se_pload_nd_pnat_v17 filler
,ci_lo_pload_nd_pnat_v17 filler
,ci_hi_pload_nd_pnat_v17 filler
,mean_pload_nd_pmines_v17 filler
,se_pload_nd_pmines_v17 filler
,ci_lo_pload_nd_pmines_v17 filler
,ci_hi_pload_nd_pmines_v17 filler
,mean_pload_nd_mant_p filler
,se_pload_nd_mant_p filler
,ci_lo_pload_nd_mant_p filler
,ci_hi_pload_nd_mant_p filler
,mean_pload_nd_lc8_sqkm filler
,se_pload_nd_lc8_sqkm filler
,ci_lo_pload_nd_lc8_sqkm filler
,ci_hi_pload_nd_lc8_sqkm filler
,mean_pload_inc_total
,se_pload_inc_total
,ci_lo_pload_inc_total filler
,ci_hi_pload_inc_total filler
,mean_pload_inc_01
,se_pload_inc_01
,ci_lo_pload_inc_kgp_02_v2 filler
,ci_hi_pload_inc_kgp_02_v2 filler
,mean_pload_inc_02
,se_pload_inc_02
,ci_lo_pload_inc_lc2_sqkm filler
,ci_hi_pload_inc_lc2_sqkm filler
,mean_pload_inc_03
,se_pload_inc_03
,ci_lo_pload_inc_pnat_v17 filler
,ci_hi_pload_inc_pnat_v17 filler
,mean_pload_inc_04
,se_pload_inc_04
,ci_lo_pload_inc_pmines_v17 filler
,ci_hi_pload_inc_pmines_v17 filler
,mean_pload_inc_05
,se_pload_inc_05
,ci_lo_pload_inc_mant_p filler
,ci_hi_pload_inc_mant_p filler
,mean_pload_inc_06
,se_pload_inc_06
,ci_lo_pload_inc_lc8_sqkm filler
,ci_hi_pload_inc_lc8_sqkm filler
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
,sh_kgp_02_v2 filler
,sh_lc2_sqkm filler
,sh_pnat_v17 filler
,sh_pmines_v17 filler
,sh_mant_p filler
,sh_lc8_sqkm filler
)
