-- Not generic enough yet.


select
enh_reach_id
,81 enh_network_id
,nom_reach_id
,identifier
,identifier full_identifier
,hydseq
,fnode 
,tnode
,frac 
,reach_size 
,head_reach 
,shore_reach
,term_trans 
,term_estuary
,term_nonconnect 
,edaname  
,edacda 
,old_identifier
from temp_enh_reach;


insert into enh_reach_attrib 
select
er.enh_reach_id
,ter.reach_name
,ter.open_water_name
,ter.meanq 
,ter.meanv
,ter.catch_area  
,ter.cum_catch_area
,ter.reach_length
,ter.huc2  
,ter.huc4  
,ter.huc6 
,ter.huc8   
,ter.huc2_name 
,ter.huc4_name
,ter.huc6_name
,ter.huc8_name
from enh_reach er, temp_enh_reach_attrib ter
where er.identifier = ter.identifier