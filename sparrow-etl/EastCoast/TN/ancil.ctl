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
,headflag
,termflag
,pname 
,meanq
,mavelu filler
,target filler
,MRB filler
,HUC8
,reachcode_comb filler
,edacode "substr(:edacode,1,5)"
,edaname
,ReachTOT filler
,rchtype
,WBRchCd filler
,del_frac filler
,mean_del_frac filler
,se_del_frac filler
,ci_lo_del_frac filler
,ci_hi_del_frac filler
)