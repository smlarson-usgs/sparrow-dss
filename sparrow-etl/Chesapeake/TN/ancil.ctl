OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_ANCIL
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(local_id ":gridcode"
,std_id ":gridcode"
,new_or_modified
,mrb_id ":gridcode"
,hydseq "nvl(:hydseq,0)"
,sqkm
,demtarea
,meanq
,terminalfl filler
,reachtype filler
,del_frac filler
,mean_del_frac filler
,se_del_frac filler
,ci_lo_del_frac filler
,ci_hi_del_frac filler
,gridcode
)
