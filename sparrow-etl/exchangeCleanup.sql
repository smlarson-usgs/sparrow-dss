show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

alter table reach_coef_swap enable constraint reach_ceof_swp_model_reach_fk;
alter table source_reach_coef_swap enable constraint source_reach_coef_swp_fk;
alter table source_value_swap enable constraint source_value_swp_mdl_rch_fk;
alter table model_reach_attrib_swap enable constraint model_rch_attrib_swp_model_fk;   

alter table source_reach_coef_swap enable constraint source_reach_coef_swp_src_fk; 
alter table source_value_swap enable constraint source_value_swp_source_fk;

select 'end time: ' || systimestamp from dual;
