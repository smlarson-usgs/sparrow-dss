show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

alter table source_value_swap drop constraint source_value_swp_mdl_rch_fk;
alter table source_value_swap add constraint source_value_swp_mdl_rch_fk foreign key (model_reach_id) references model_reach (model_reach_id) on delete cascade disable;

--For now we are expecting the source table to not really change, so it is ok to point the fk to it rather than source_swap.
--alter table source_value_swap drop constraint source_value_swp_source_fk;
--alter table source_value_swap add constraint source_value_swp_source_fk foreign key (source_id) references source (source_id) on delete cascade;


begin
	declare model_id number;
	        exchange_ddl varchar2(4000 char);
begin
	select sparrow_model_id
	  into model_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';

	exchange_ddl := 'alter table source_value exchange partition sparrow_model_' || model_id ||' with table source_value_swap update indexes';
	dbms_output.put_line(exchange_ddl);
	execute immediate exchange_ddl;
	
    for i in (select index_name
                from user_indexes
               where table_name like 'SOURCE_VALUE%' and
                     status = 'UNUSABLE') loop
      dbms_output.put_line('Rebuilding index:' || i.index_name);
      execute immediate 'alter index ' || i.index_name || ' rebuild';
    end loop;
	
    dbms_stats.gather_table_stats(ownname=> user, tabname=> 'SOURCE_VALUE');
end;
end;
/

alter table source_value enable constraint source_value_mdl_rch_fk;

select 'end time: ' || systimestamp from dual;
