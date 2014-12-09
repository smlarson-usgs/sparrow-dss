OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_ANCIL
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(local_id
,std_id
,new_or_modified
,mrb_id :waterid
,hydseq "nvl(:hydseq,0)"
,sqkm :inc_area
,demtarea :tot_area
,headflag
,termflag
,pname :GNIS_Name
,meanq :mean_flow
,mavelu filler
,target filler
,MRB filler
,HUC8
,reachcode_comb filler
,edacode "substr(:NOAA_targetonly,1,5)"
,edaname :NOAA_Name
,ReachTOT filler
,rchtype :ReachType
,WBRchCd filler
,del_frac filler
,mean_del_frac filler
,se_del_frac filler
,ci_lo_del_frac filler
,ci_hi_del_frac filler
)