OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_ANCIL
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(local_id
,std_id
,new_or_modified
,mrb_id
,hydseq "nvl(:hydseq,0)"
,sqkm
,demtarea
,meanq
,delivery_target filler
,pname
,headflag
,termflag
,rchtype
,meanv
,rchtot filler
,length_m
,edaname
,edacode
,code_finalcomb_huc4estuary filler
,rr filler
,huc6 filler
,station_id filler
,station_name filler
,basin_delineation filler
,ex_calib filler
,fullpolygon_notinmodel filler
,demtarea_revtosqkm filler
,sqkm_revtofullpolygon filler
,huc8
,termrchforeda filler
,del_frac filler
,mean_del_frac filler
,se_del_frac filler
,ci_lo_del_frac filler
,ci_hi_del_frac filler
)
