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
,hydseq
,sqkm
,demtarea
,meanq
,delivery_target filler
,PNAME
,STATION_ID filler
,STATE1 filler
,HEADFLAG
,TERMFLAG
,HUC8
,HUC2 filler
,FRAC filler
,RCHTYPE
,GOOLSBY filler
,staid filler
,del_frac filler
,mean_del_frac filler
,se_del_frac filler
,ci_lo_del_frac filler
,ci_hi_del_frac filler
)
