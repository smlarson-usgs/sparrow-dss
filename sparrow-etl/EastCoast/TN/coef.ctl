OPTIONS (SKIP=1)
LOAD DATA
INTO TABLE TEMP_COEF
TRUNCATE
FIELDS TERMINATED BY X'9'
TRAILING NULLCOLS
(mrb_id :waterid
,iter
,inc_delivf
,tot_delivf
,boot_error
,c_source01
,c_source02
,c_source03
,c_source04
,c_source05
,c_source06
,c_source07
,c_source08
,c_source09
)
