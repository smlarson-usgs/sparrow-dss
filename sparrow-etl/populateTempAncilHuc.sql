show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;

update temp_ancil
   set huc8 = (select huc8_char
                 from temp_huc_list
                where temp_ancil.local_id = temp_huc_list.comid);

commit;

select 'end time: ' || systimestamp from dual;
