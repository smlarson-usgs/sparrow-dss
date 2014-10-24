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
,if_target filler
,staid filler
,ifres filler
,del_frac filler
,gridcode
)
