OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_ENH_REACH
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(enh_reach_id filler
,enh_network_id "81"
,nom_reach_id filler
,identifier ":comid"
,full_identifier ":comid"
,hydseq ":hydroseq"
,fnode ":fromnode"
,tnode ":tonode"
,frac filler
,reach_size filler
,head_reach filler
,shore_reach filler
,term_trans filler
,term_estuary filler
,term_nonconnect filler
,edaname filler
,edacda filler
,old_identifier filler)