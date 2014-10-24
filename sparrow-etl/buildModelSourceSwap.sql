show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;


variable model_id number;
begin
	select sparrow_model_id
	  into :model_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';
	dbms_output.put_line('sparrow_model_id:' || :model_id || ':');
end;
/

truncate table source_swap;

insert into source_swap (source_id, name, description, sort_order, sparrow_model_id, identifier, display_name, constituent, units, precision, is_point_source)
select source_seq.nextval,
       temp_src_metadata.source_name,
       temp_src_metadata.long_name,
       temp_src_metadata.sort_order,
       :model_id sparrow_model_id,
       temp_src_metadata.id,
       temp_src_metadata.short_name,
       'Unknown',
       temp_src_metadata.source_units,
       temp_src_metadata.precision,
       temp_src_metadata.is_point_source
  from temp_src_metadata;

commit;

select 'end time: ' || systimestamp from dual;
