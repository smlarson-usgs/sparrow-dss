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
,huc8
,pname
,staid filler
,frac filler
,headflag
,termflag
,rchtype
,gltribnum filler
,grlake filler
,station_id filler
,del_frac filler
,mean_del_frac filler
,se_del_frac filler
,ci_lo_del_frac filler
,ci_hi_del_frac filler
)
