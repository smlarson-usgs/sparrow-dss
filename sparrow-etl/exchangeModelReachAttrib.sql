show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

alter table model_reach_attrib_swap drop constraint model_rch_attrib_swp_reach_fk;
alter table model_reach_attrib_swap add constraint model_rch_attrib_swp_reach_fk foreign key (model_reach_id) references model_reach (model_reach_id) disable;

begin
	declare model_id number;
	        exchange_ddl varchar2(4000 char);
begin
	select sparrow_model_id
	  into model_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';

	exchange_ddl := 'alter table model_reach_attrib exchange partition sparrow_model_' || model_id ||' with table model_reach_attrib_swap update indexes';
	dbms_output.put_line(exchange_ddl);
	execute immediate exchange_ddl;
	
    for i in (select index_name
                from user_indexes
               where table_name like 'MODEL_REACH_ATTRIB%' and
                     status = 'UNUSABLE') loop
      dbms_output.put_line('Rebuilding index:' || i.index_name);
      execute immediate 'alter index ' || i.index_name || ' rebuild';
    end loop;
	
    dbms_stats.gather_table_stats(ownname=> user, tabname=> 'MODEL_REACH_ATTRIB');
end;
end;
/

alter table model_reach_attrib enable constraint model_rch_attrib_reach_fk;

select 'end time: ' || systimestamp from dual;
