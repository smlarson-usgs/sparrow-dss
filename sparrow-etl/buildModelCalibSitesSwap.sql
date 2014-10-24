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

truncate table model_calib_sites_swap;

alter table model_calib_sites_swap drop constraint model_calib_sites_swp_reach_fk;

alter table model_calib_sites_swap add constraint model_calib_sites_swp_reach_fk foreign key (model_reach_id) references model_reach_swap (model_reach_id);

insert into model_calib_sites_swap (model_reach_id, station_name, actual, site_geom, latitude, longitude, predict, station_id, sparrow_model_id_partition)
select model_reach_swap.model_reach_id,
       temp_resids.station_name,
       temp_resids.actual,
       mdsys.sdo_geometry(2001,8307,mdsys.sdo_point_type(temp_resids.lon,temp_resids.lat,null),null,null),
       temp_resids.lat latitude,
       temp_resids.lon longitude,
       temp_predict.pload_total predict,
       temp_resids.station_id,
       :model_id
  from temp_resids
       left join temp_predict
         on temp_resids.mrb_id = temp_predict.mrb_id
       join model_reach_swap
         on temp_resids.mrb_id = model_reach_swap.identifier
 where temp_resids.lat is not null and
       temp_resids.lon is not null and
       temp_resids.actual is not null;

commit;

select 'end time: ' || systimestamp from dual;
