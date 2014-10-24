show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

alter table source_reach_coef_swap drop constraint source_reach_coef_swp_fk;
alter table source_reach_coef_swap add constraint source_reach_coef_swp_fk foreign key (model_reach_id) references model_reach (model_reach_id) on delete cascade disable;

--For now we are expecting the source table to not really change, so it is ok to point the fk to it rather than source_swap.
--alter table source_reach_coef_swap drop constraint source_reach_coef_swp_src_fk;
--alter table source_reach_coef_swap add constraint source_reach_coef_swp_src_fk foreign key (source_id) references source (source_id) on delete cascade;


begin
	declare model_id number;
	        exchange_ddl varchar2(4000 char);
begin
	select sparrow_model_id
	  into model_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';

	exchange_ddl := 'alter table source_reach_coef exchange partition sparrow_model_' || model_id ||' with table source_reach_coef_swap update indexes';
	dbms_output.put_line(exchange_ddl);
	execute immediate exchange_ddl;
	
    for i in (select index_name
                from user_indexes
               where table_name like 'SOURCE_REACH_COEF%' and
                     status = 'UNUSABLE') loop
      dbms_output.put_line('Rebuilding index:' || i.index_name);
      execute immediate 'alter index ' || i.index_name || ' rebuild';
    end loop;
	
    dbms_stats.gather_table_stats(ownname=> user, tabname=> 'SOURCE_REACH_COEF');
end;
end;
/

alter table source_reach_coef enable constraint source_reach_coef_fk;

select 'end time: ' || systimestamp from dual;
