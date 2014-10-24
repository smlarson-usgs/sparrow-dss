show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

update temp_resids
   set mrb_id = (select gridcode
                   from temp_grid_codes
                  where temp_resids.reach = temp_grid_codes.comid);

commit;

select 'end time: ' || systimestamp from dual;
