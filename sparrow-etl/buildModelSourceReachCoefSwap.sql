show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;


variable model_id number;
variable iteration number;
variable number_of_sources number;
begin
	select sparrow_model_id, iteration, number_of_sources
	  into :model_id, :iteration, :number_of_sources
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';
	dbms_output.put_line('sparrow_model_id:' || :model_id || ':iteration:' || :iteration || ':number_of_sources:' || :number_of_sources || ':');
end;
/

truncate table source_reach_coef_swap;

alter table source_reach_coef_swap drop constraint source_reach_coef_swp_fk;

alter table source_reach_coef_swap add constraint source_reach_coef_swp_fk foreign key (model_reach_id) references model_reach_swap (model_reach_id) on delete cascade;

--For now we are expecting the source table to not really change, so it is ok to point the fk to it rather than source_swap.
--alter table source_reach_coef_swap drop constraint source_reach_coef_swp_src_fk;
--alter table source_reach_coef_swap add constraint source_reach_coef_swp_src_fk foreign key (source_id) references source_swap (source_id) on delete cascade;

insert into source_reach_coef_swap (source_reach_coef_id, iteration, value, source_id, model_reach_id, sparrow_model_id_partition)
with data as
(select mrb_id,iter,to_number(substr(position,-2))pos,val
  from temp_coef
       unpivot (val for position in (c_source01,c_source02,c_source03,c_source04,c_source05,
                                     c_source06,c_source07,c_source08,c_source09,c_source10,
                                     c_source11,c_source12,c_source13,c_source14,c_source15,
				     c_source16,c_source17,c_source18,c_source19,c_source20,
                                     c_source21,c_source22,c_source23,c_source24,c_source25,
                                     c_source26,c_source27,c_source28,c_source29,c_source30,
                                     c_source31,c_source32,c_source33,c_source34,c_source35,
                                     c_source36,c_source37)
               )
 where iter = :iteration and
       to_number(substr(position,-2)) <= :number_of_sources)
select source_reach_coef_seq.nextval,
       data.iter,
       data.val,
       source.source_id,
       model_reach_swap.model_reach_id,
       :model_id
  from data
       join source
         on data.pos = source.identifier and
            :model_id = source.sparrow_model_id
       join model_reach_swap
         on data.mrb_id = model_reach_swap.identifier;

commit;

select 'end time: ' || systimestamp from dual;
