show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;


variable model_id number;
variable network_id number;
begin
	select sparrow_model_id, enh_network_id
	  into :model_id, :network_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';
	dbms_output.put_line('sparrow_model_id:' || :model_id || ':enh_network_id:' || :network_id || ':');
end;
/

truncate table model_reach_attrib_swap;

alter table model_reach_attrib_swap drop constraint model_rch_attrib_swp_reach_fk;

alter table model_reach_attrib_swap add constraint model_rch_attrib_swp_reach_fk foreign key (model_reach_id) references model_reach_swap (model_reach_id);

insert into model_reach_attrib_swap (model_reach_id, reach_name, meanq, meanv, catch_area, reach_length, huc2, huc4, huc6, huc8, shore_reach, term_trans, term_estuary, term_nonconnect, edaname, edacode, huc2_name, huc4_name, huc6_name, huc8_name, sparrow_model_id_partition, tot_Upstream_area, tot_contrib_area)
select a.model_reach_id, a.reach_name, a.meanq, a.meanv, a.catch_area, a.reach_length, a.huc2, a.huc4, a.huc6, a.huc8,
       a.shore_reach, a.term_trans, a.term_estuary, a.term_nonconnect, a.edaname, a.edacode, 
       huc2_lkp.name,
       huc4_lkp.name,
       huc6_lkp.name,
       huc8_lkp.name,
       :model_id,
       a.tot_upstream_area
  from (select model_reach_swap.model_reach_id,
               nvl(temp_ancil.pname, enh_reach_attrib.reach_name) reach_name,
               nvl(temp_ancil.meanq, enh_reach_attrib.meanq) meanq,
               nvl(temp_ancil.meanv, enh_reach_attrib.meanv) meanv,
               nvl(temp_ancil.sqkm, enh_reach_attrib.catch_area) catch_area,
               nvl(temp_ancil.demtarea, enh_reach_attrib.cum_catch_area) tot_upstream_area,
               nvl(temp_ancil.contrib_area, enh_reach_attrib.contrib_area) tot_contrib_area,
               nvl(temp_ancil.length_m, enh_reach_attrib.reach_length) reach_length,
               case 
                 when temp_ancil.huc8 is null 
                   then nvl(enh_reach_attrib.huc2, '00')
               	   else	substr(to_char(temp_ancil.huc8),0,2)
               end huc2,
               case 
                 when temp_ancil.huc8 is null 
                   then nvl(enh_reach_attrib.huc4, '0000')
               	   else	substr(to_char(temp_ancil.huc8),0,4)
               end huc4,
               case 
                 when temp_ancil.huc8 is null 
                   then nvl(enh_reach_attrib.huc6, '000000')
               	   else	substr(to_char(temp_ancil.huc8),0,6)
               end huc6,
               case 
                 when temp_ancil.huc8 is null 
                   then nvl(enh_reach_attrib.huc8, '00000000')
               	   else	substr(to_char(temp_ancil.huc8),0,8)
               end huc8,
               case temp_ancil.termflag
                 when 3 then 1
                 else 0
               end shore_reach,
               case temp_ancil.termflag
                 when 0 then 1
                 else 0
               end term_trans,
               case temp_ancil.termflag
                 when 1 then 1
                 else 0
               end term_estuary,
               case temp_ancil.termflag
                 when 2 then 1
                 else 0
               end term_nonconnect,
               nvl(temp_ancil.edaname, enh_reach.edaname) edaname,
               nvl(temp_ancil.edacode, enh_reach.edacda) edacode
          from model_reach_swap
               left join stream_network.enh_reach
                 on model_reach_swap.enh_reach_id = enh_reach.enh_reach_id and
                    :network_id = enh_reach.enh_network_id
               left join stream_network.enh_reach_attrib
                 on model_reach_swap.enh_reach_id = enh_reach_attrib.enh_reach_id
               join temp_ancil
                 on model_reach_swap.identifier = temp_ancil.local_id) a
       left join stream_network.huc2_lkp
         on a.huc2 = huc2_lkp.huc2
       left join stream_network.huc4_lkp
         on a.huc4 = huc4_lkp.huc4
       left join stream_network.huc6_lkp
         on a.huc6 = huc6_lkp.huc6
       left join stream_network.huc8_lkp
         on a.huc8 = huc8_lkp.huc8;

commit;

select 'end time: ' || systimestamp from dual;
