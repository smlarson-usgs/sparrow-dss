OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_ANCIL
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(local_id
,std_id,new_or_modified,mrb_id
,hydseq "nvl(:hydseq,0)",sqkm ,demtarea 
,meanq
,target filler
,MRB filler
,ReachTOT filler
,rchtype
,reachcode_comb filler
,HUC8 "lpad(:HUC8,8,'0')"
,WBRchCd filler
,mavelu filler,headflag
,pname 
,edacode
,edaname
,termflag,length_m
,del_frac filler,mean_del_frac filler
,se_del_frac filler
,ci_lo_del_frac filler
,ci_hi_del_frac filler
,contrib_area ":demtarea"
)