OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_TOPO
TRUNCATE
FIELDS TERMINATED BY X'9'
(mrb_id
,fnode
,tnode
,iftran
,hydseq "nvl(:hydseq,0)"
,frac
)
