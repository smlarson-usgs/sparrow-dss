show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

alter table model_reach_watershed_swap drop constraint model_reach_id_swp_fk;
alter table model_reach_watershed_swap add constraint model_reach_id_swp_fk foreign key (model_reach_id) references model_reach (model_reach_id) disable;
-- alter table model_reach_watershed_swap disable CONSTRAINT model_reach_id_swp_fk;
alter table model_reach_watershed_swap disable CONSTRAINT mdl_rch_wshd_sw_model_fk;
alter table model_reach_watershed_swap disable CONSTRAINT watershed_id_swp_fk;

alter table model_reach_watershed disable CONSTRAINT model_reach_id_fk;
alter table model_reach_watershed disable CONSTRAINT model_reach_watershed_model_fk;
alter table model_reach_watershed disable CONSTRAINT watershed_id_fk;



begin
	declare model_id number;
	        exchange_ddl varchar2(4000 char);
begin
	select sparrow_model_id
	  into model_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';

	exchange_ddl := 'alter table model_reach_watershed exchange partition sparrow_model_' || model_id ||' with table model_reach_watershed_swap update indexes';
	dbms_output.put_line(exchange_ddl);
	execute immediate exchange_ddl;
	
    for i in (select index_name
                from user_indexes
               where table_name like 'MODEL_REACH_WATERSHED%' and
                     status = 'UNUSABLE') loop
      dbms_output.put_line('Rebuilding index:' || i.index_name);
      execute immediate 'alter index ' || i.index_name || ' rebuild';
    end loop;
	
    dbms_stats.gather_table_stats(ownname=> user, tabname=> 'MODEL_REACH_WATERSHED');
end;
end;
/

-- alter table model_reach_watershed_swap enable constraint model_reach_id_swp_fk;
-- commented out because parent model_reach_ids cannot be found in MODEL_REACH after the swap

alter table model_reach_watershed_swap enable CONSTRAINT mdl_rch_wshd_sw_model_fk;
alter table model_reach_watershed_swap enable CONSTRAINT watershed_id_swp_fk;

alter table model_reach_watershed enable constraint model_reach_id_fk;
alter table model_reach_watershed enable CONSTRAINT model_reach_watershed_model_fk;
alter table model_reach_watershed enable CONSTRAINT watershed_id_fk;

select 'end time: ' || systimestamp from dual;
