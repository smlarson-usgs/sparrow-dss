show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;


variable model_id number;
variable iteration number;
begin
	select sparrow_model_id, iteration
	  into :model_id, :iteration
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';
	dbms_output.put_line('sparrow_model_id:' || :model_id || ':iteration:' || :iteration || ':');
end;
/

truncate table reach_coef_swap;

alter table reach_coef_swap drop constraint reach_ceof_swp_model_reach_fk;

alter table reach_coef_swap add constraint reach_ceof_swp_model_reach_fk foreign key (model_reach_id) references model_reach_swap (model_reach_id) on delete cascade;

insert into reach_coef_swap (reach_coef_id, iteration, inc_delivery, total_delivery, boot_error, model_reach_id, sparrow_model_id_partition)
select reach_coef_seq.nextval,
       temp_coef.iter,
       temp_coef.inc_delivf,
       temp_coef.tot_delivf,
       temp_coef.boot_error,
       model_reach_swap.model_reach_id,
       :model_id
  from temp_coef
       join model_reach_swap
         on temp_coef.mrb_id = model_reach_swap.identifier
 where temp_coef.iter = :iteration;

commit;

select 'end time: ' || systimestamp from dual;
