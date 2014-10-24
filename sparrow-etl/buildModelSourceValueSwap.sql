show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;


variable model_id number;
variable number_of_sources number;
begin
	select sparrow_model_id, number_of_sources
	  into :model_id, :number_of_sources
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';
	dbms_output.put_line('sparrow_model_id:' || :model_id || ':number_of_sources:' || :number_of_sources || ':');
end;
/

truncate table source_value_swap;

alter table source_value_swap drop constraint source_value_swp_mdl_rch_fk;

alter table source_value_swap add constraint source_value_swp_mdl_rch_fk foreign key (model_reach_id) references model_reach_swap (model_reach_id) on delete cascade;

--For now we are expecting the source table to not really change, so it is ok to point the fk to it rather than source_swap.
--alter table source_value_swap drop constraint source_value_swp_source_fk;
--alter table source_value_swap add constraint source_value_swp_source_fk foreign key (source_id) references source_swap (source_id) on delete cascade;

insert into source_value_swap (source_value_id, value, source_id, model_reach_id, mean_pload, se_pload, mean_pload_inc, se_pload_inc, sparrow_model_id_partition)
with data_predict as
(select *
   from (select mrb_id, to_number(substr(position, -2)) position, substr(position, 1, (length(position) - 3)) tp, val
           from temp_predict
                unpivot (val for position in (mean_pload_01,            mean_pload_02,            mean_pload_03,            mean_pload_04,
                                              mean_pload_05,            mean_pload_06,            mean_pload_07,            mean_pload_08,
                                              mean_pload_09,            mean_pload_10,            mean_pload_11,            mean_pload_12,
                                              mean_pload_13,            mean_pload_14,            mean_pload_15,            mean_pload_16,
					      mean_pload_17,            mean_pload_18,            mean_pload_19,            mean_pload_20,
                                              mean_pload_21,            mean_pload_22,            mean_pload_23,            mean_pload_24,
                                              mean_pload_25,            mean_pload_26,            mean_pload_27,            mean_pload_28,
                                              mean_pload_29,            mean_pload_30,            mean_pload_31,            mean_pload_32,
                                              mean_pload_33,            mean_pload_34,            mean_pload_35,            mean_pload_36,
					      mean_pload_37,
                                              se_pload_01,                se_pload_02,            se_pload_03,            se_pload_04,
                                              se_pload_05,                se_pload_06,            se_pload_07,            se_pload_08,
                                              se_pload_09,                se_pload_10,            se_pload_11,            se_pload_12,
                                              se_pload_13,                se_pload_14,            se_pload_15,            se_pload_16,
					      se_pload_17,            	se_pload_18,            se_pload_19,            se_pload_20,
                                              se_pload_21,            	se_pload_22,            se_pload_23,            se_pload_24,
                                              se_pload_25,            	se_pload_26,            se_pload_27,            se_pload_28,
                                              se_pload_29,            	se_pload_30,            se_pload_31,            se_pload_32,
                                              se_pload_33,            	se_pload_34,            se_pload_35,            se_pload_36,
					      se_pload_37,
                                              mean_pload_inc_01,        mean_pload_inc_02,        mean_pload_inc_03,        mean_pload_inc_04,
                                              mean_pload_inc_05,        mean_pload_inc_06,        mean_pload_inc_07,        mean_pload_inc_08,
                                              mean_pload_inc_09,        mean_pload_inc_10,        mean_pload_inc_11,        mean_pload_inc_12,
                                              mean_pload_inc_13,        mean_pload_inc_14,        mean_pload_inc_15,        mean_pload_inc_16,
					      mean_pload_inc_17,            mean_pload_inc_18,            mean_pload_inc_19,            mean_pload_inc_20,
                                              mean_pload_inc_21,            mean_pload_inc_22,            mean_pload_inc_23,            mean_pload_inc_24,
                                              mean_pload_inc_25,            mean_pload_inc_26,            mean_pload_inc_27,            mean_pload_inc_28,
                                              mean_pload_inc_29,            mean_pload_inc_30,            mean_pload_inc_31,            mean_pload_inc_32,
                                              mean_pload_inc_33,            mean_pload_inc_34,            mean_pload_inc_35,            mean_pload_inc_36,
					      mean_pload_inc_37,
                                              se_pload_inc_01,            se_pload_inc_02,        se_pload_inc_03,        se_pload_inc_04,
                                              se_pload_inc_05,            se_pload_inc_06,        se_pload_inc_07,        se_pload_inc_08,
                                              se_pload_inc_09,            se_pload_inc_10,        se_pload_inc_11,        se_pload_inc_12,
                                              se_pload_inc_13,            se_pload_inc_14,        se_pload_inc_15,        se_pload_inc_16,
					      se_pload_inc_17,            se_pload_inc_18,            se_pload_inc_19,            se_pload_inc_20,
                                              se_pload_inc_21,            se_pload_inc_22,            se_pload_inc_23,            se_pload_inc_24,
                                              se_pload_inc_25,            se_pload_inc_26,            se_pload_inc_27,            se_pload_inc_28,
                                              se_pload_inc_29,            se_pload_inc_30,            se_pload_inc_31,            se_pload_inc_32,
                                              se_pload_inc_33,            se_pload_inc_34,            se_pload_inc_35,            se_pload_inc_36,
					      se_pload_inc_37
                        )
               )
 where to_number(substr(position,-2)) <= :number_of_sources)
 pivot (sum(val) for tp in ('MEAN_PLOAD' as mean_pload, 'SE_PLOAD' as se_pload, 'MEAN_PLOAD_INC' as mean_pload_inc, 'SE_PLOAD_INC' as se_pload_inc))),
data_src as
(select mrb_id, to_number(substr(position,-2)) position, val
  from temp_src
       unpivot (val for position in (src01, src02, src03, src04, src05,
                                     src06, src07, src08, src09, src10,
                                     src11, src12, src13, src14, src15,
				     src16, src17, src18, src19, src20,
                                     src21, src22, src23, src24, src25,
                                     src26, src27, src28, src29, src30,
                                     src31, src32, src33, src34, src35,
                                     src36, src37)
               )
 where to_number(substr(position,-2)) <= :number_of_sources)
select source_value_seq.nextval,
       data_src.val,
       source.source_id,
       model_reach_swap.model_reach_id,
       data_predict.mean_pload,
       data_predict.se_pload,
       data_predict.mean_pload_inc,
       data_predict.se_pload_inc,
       :model_id
  from data_src
       join model_reach_swap
         on data_src.mrb_id = model_reach_swap.identifier
       join source
         on data_src.position = source.identifier and
            :model_id = source.sparrow_model_id
       join data_predict
         on data_src.mrb_id = data_predict.mrb_id and
            data_src.position = data_predict.position;

commit;

select 'end time: ' || systimestamp from dual;
