show user;
set timing on;
set serveroutput on;
whenever sqlerror exit failure rollback;
whenever oserror exit failure rollback;
select 'start time: ' || systimestamp from dual;


variable wmodel_id number;
variable network_id number;
begin
	select sparrow_model_id, enh_network_id
	  into :wmodel_id, :network_id
	  from etl_parameters
	 where mrb = '&1' and
	       constituent = '&2';
	dbms_output.put_line('sparrow_model_id:' || :wmodel_id || ':enh_network_id:' || :network_id || ':');
end;
/


truncate table model_reach_watershed_swap;

alter table model_reach_watershed_swap drop constraint model_reach_id_swp_fk;

alter table model_reach_watershed_swap add constraint model_reach_id_swp_fk foreign key (model_reach_id) references model_reach_swap (model_reach_id) disable;

begin
  declare model_id number := :wmodel_id; 

  cursor watershed_cur(p_model_id in number) is
    select watershed_id, parameter_type, watershed_parameters, sparrow_model_id
      from predefined_watershed
     where sparrow_model_id = p_model_id;
     
  type synonym_table is table of watershed_cur%rowtype;    
  my_synonyms synonym_table;

begin
  open watershed_cur(:wmodel_id);
  fetch watershed_cur bulk collect into my_synonyms;
  close watershed_cur;
    

  for i in my_synonyms.first..my_synonyms.last loop
    CASE my_synonyms(i).parameter_type
                        WHEN 'IDENTIFIER' then execute immediate 'insert into model_reach_watershed_swap (model_reach_id, watershed_id, sparrow_model_id_partition)
                                    select mav.model_reach_id, ' ||my_synonyms(i).watershed_id||',' ||my_synonyms(i).sparrow_model_id ||
                                    ' from model_attrib_vw mav
                                          where mav.sparrow_model_id='||:wmodel_id||' and
                                                mav.full_identifier in (' ||my_synonyms(i).watershed_parameters ||')';
                        WHEN 'EDANAME' then execute immediate 'insert into model_reach_watershed_swap (model_reach_id, watershed_id, sparrow_model_id_partition)
                                    select mav.model_reach_id, ' ||my_synonyms(i).watershed_id||',' ||my_synonyms(i).sparrow_model_id ||
                                    ' from model_attrib_vw mav
                                          where mav.sparrow_model_id='||:wmodel_id||' and
						(mav.shore_reach=1 or mav.term_estuary=1) and
                                                mav.edaname in (' ||my_synonyms(i).watershed_parameters ||')'; 
                        WHEN 'EDACODE' then execute immediate 'insert into model_reach_watershed_swap (model_reach_id, watershed_id, sparrow_model_id_partition)
                                    select mav.model_reach_id, ' ||my_synonyms(i).watershed_id||',' ||my_synonyms(i).sparrow_model_id ||
                                    ' from model_attrib_vw mav
                                            where mav.sparrow_model_id='||:wmodel_id||' and
                                                (mav.shore_reach=1 or mav.term_estuary=1) and
                                                mav.edacode in (' || my_synonyms(i).watershed_parameters || ')';
                        WHEN 'LIKEEDACODE' then execute immediate 'insert into model_reach_watershed_swap (model_reach_id, watershed_id, sparrow_model_id_partition)
                                    select mav.model_reach_id, '||my_synonyms(i).watershed_id||',' ||my_synonyms(i).sparrow_model_id || 
                                    ' from model_attrib_vw mav
                                            where mav.sparrow_model_id='||:wmodel_id||' and
                                                (mav.shore_reach=1 or mav.term_estuary=1) and
                                                mav.edacode like (' ||my_synonyms(i).watershed_parameters ||')';
                        ELSE insert into model_reach_watershed_swap (model_reach_id, watershed_id, sparrow_model_id_partition)
                                    select a.model_reach_id, a.watershed_id, a.sparrow_model_id 
                                    from (select mra.model_reach_id, pw.watershed_id, pw.sparrow_model_id 
                                          from model_reach_attrib mra,
                                          predefined_watershed pw
                                          where mra.sparrow_model_id_partition=:wmodel_id and
                                                pw.sparrow_model_id=:wmodel_id and
                                                (mra.shore_reach=1 or mra.term_estuary=1) and
                                                1=2) a;
    END CASE;
  end loop;
  commit;
  
end;
/
end;
/
