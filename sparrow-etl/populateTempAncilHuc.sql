show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

merge into temp_ancil ta
using (select comid, huc8_char from temp_huc_list) hl
on (ta.mrb_id = hl.comid)
when matched then update set ta.huc8 = hl.huc8_char;

commit;

select 'end time: ' || systimestamp from dual;
