show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

alter table model_calib_sites disable constraint model_calib_sites_reach_fk;
alter table model_reach_attrib disable constraint model_rch_attrib_reach_fk;
alter table reach_coef disable constraint reach_ceof_model_reach_fk;
alter table source_reach_coef disable constraint source_reach_coef_fk;
alter table source_value disable constraint source_value_mdl_rch_fk;
alter table model_reach_watershed disable constraint model_reach_id_fk;
  
alter table model_calib_sites_swap disable constraint model_calib_sites_swp_reach_fk;
alter table model_reach_attrib_swap disable constraint model_rch_attrib_swp_reach_fk;
alter table reach_coef_swap disable constraint reach_ceof_swp_model_reach_fk;
alter table source_reach_coef_swap disable constraint source_reach_coef_swp_fk;
alter table source_value_swap disable constraint source_value_swp_mdl_rch_fk;
alter table model_reach_watershed_swap disable constraint model_reach_id_swp_fk;

begin
	declare model_id number;
	        exchange_ddl varchar2(4000 char);
begin
	select sparrow_model_id
	  into model_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';

	exchange_ddl := 'alter table model_reach exchange partition sparrow_model_' || model_id ||' with table model_reach_swap update indexes';
	dbms_output.put_line(exchange_ddl);
	execute immediate exchange_ddl;
	
    for i in (select index_name
                from user_indexes
               where table_name like 'MODEL_REACH%' and
                     status = 'UNUSABLE') loop
      dbms_output.put_line('Rebuilding index:' || i.index_name);
      execute immediate 'alter index ' || i.index_name || ' rebuild';
    end loop;
	
    dbms_stats.gather_table_stats(ownname=> user, tabname=> 'MODEL_REACH');
end;
end;
/

select 'end time: ' || systimestamp from dual;
