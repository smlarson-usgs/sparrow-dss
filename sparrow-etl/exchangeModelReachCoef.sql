show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

alter table reach_coef_swap drop constraint reach_ceof_swp_model_reach_fk;
alter table reach_coef_swap add constraint reach_ceof_swp_model_reach_fk foreign key (model_reach_id) references model_reach (model_reach_id) on delete cascade disable;

begin
	declare model_id number;
	        exchange_ddl varchar2(4000 char);
begin
	select sparrow_model_id
	  into model_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';

	exchange_ddl := 'alter table reach_coef exchange partition sparrow_model_' || model_id ||' with table reach_coef_swap update indexes';
	dbms_output.put_line(exchange_ddl);
	execute immediate exchange_ddl;
	
    for i in (select index_name
                from user_indexes
               where table_name like 'REACH_COEF%' and
                     status = 'UNUSABLE') loop
      dbms_output.put_line('Rebuilding index:' || i.index_name);
      execute immediate 'alter index ' || i.index_name || ' rebuild';
    end loop;
	
    dbms_stats.gather_table_stats(ownname=> user, tabname=> 'REACH_COEF');
end;
end;
/

alter table reach_coef enable constraint reach_ceof_model_reach_fk;

select 'end time: ' || systimestamp from dual;
