OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_SRC
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id ":gridcode"
,src01
,src02
,src03
,src04 filler
,src05
,src06
,src07
,gridcode boundfiller
)
