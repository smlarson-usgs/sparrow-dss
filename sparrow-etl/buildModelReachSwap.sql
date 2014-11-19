show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;


variable model_id number;
variable network_id number;
variable mrb varchar2(40 char);
begin
	select mrb, sparrow_model_id, enh_network_id
	  into :mrb, :model_id, :network_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';
	dbms_output.put_line('mrb:' || :mrb || ':sparrow_model_id:' || :model_id || ':enh_network_id:' || :network_id || ':');
end;
/

alter table model_calib_sites_swap disable constraint model_calib_sites_swp_reach_fk;
alter table model_reach_attrib_swap disable constraint model_rch_attrib_swp_reach_fk;
alter table reach_coef_swap disable constraint reach_ceof_swp_model_reach_fk;
alter table source_reach_coef_swap disable constraint source_reach_coef_swp_fk;
alter table source_value_swap disable constraint source_value_swp_mdl_rch_fk;
  
truncate table model_reach_swap;

insert into model_reach_swap (model_reach_id, identifier, full_identifier, sparrow_model_id, enh_reach_id, hydseq, iftran, fnode, tnode, frac, reach_size, mean_pload, se_pload, mean_pload_inc, se_pload_inc, old_identifier)
select model_reach_seq.nextval,
       case :mrb
         when 'MRB01' then NVL(enh_reach.full_identifier, temp_ancil.mrb_id)
	 when 'MRB01Fake' then NVL(enh_reach.full_identifier, temp_ancil.mrb_id)
	 when 'Chesapeake' then NVL(enh_reach.full_identifier, temp_ancil.mrb_id)
         else to_char(temp_ancil.mrb_id)
       end,
       case :mrb
         when 'MRB01' then NVL(enh_reach.full_identifier, temp_ancil.mrb_id)
	 when 'MRB01Fake' then NVL(enh_reach.full_identifier, temp_ancil.mrb_id)
	 when 'Chesapeake' then NVL(enh_reach.full_identifier, temp_ancil.mrb_id)
         else to_char(temp_ancil.mrb_id)
       end,
       :model_id sparrow_model_id,
       enh_reach.enh_reach_id,
       temp_topo.hydseq,
       temp_topo.iftran,
       temp_topo.fnode,
       temp_topo.tnode,
       temp_topo.frac,
       5 reach_size,
       temp_predict.mean_pload_total,
       temp_predict.se_pload_total,
       temp_predict.mean_pload_inc_total,
       temp_predict.se_pload_inc_total,
       temp_ancil.mrb_id
  from temp_ancil
       left outer join stream_network.enh_reach
         on temp_ancil.local_id = enh_reach.identifier and
            :network_id = enh_reach.enh_network_id
       join temp_topo
         on temp_ancil.local_id = temp_topo.mrb_id
       join temp_predict
         on temp_ancil.local_id = temp_predict.mrb_id;

commit;

select 'end time: ' || systimestamp from dual;
