--liquibase formatted sql

--This is for the sparrow_dss schema
 
--changeset drsteini:triggers1a endDelimiter:/ splitStatements:false
create or replace trigger model_reach_auto_id_trig
  before insert on model_reach
  referencing new as newrow
  for each row
  when (newrow.model_reach_id is null)
begin
  select model_reach_seq.nextval into :newrow.model_reach_id from dual;
end;
--rollback drop trigger model_reach_auto_id_trig;

--changeset drsteini:triggers1b endDelimiter:/ splitStatements:false
create or replace trigger predefined_watershed_trg
  before insert on predefined_watershed
  referencing new as new old as old
  for each row
begin
-- for toad:  highlight column watershed_id
  :new.watershed_id := predefined_watershed_seq.nextval;
end predefined_watershed_trg;
--rollback drop trigger predefined_watershed_trg;

--changeset drsteini:triggers1c endDelimiter:/ splitStatements:false
create or replace trigger reach_auto_id_trig
  before insert on reach_coef
  for each row
begin
  if inserting then
    if :new.reach_coef_id is null then
      select reach_coef_seq.nextval
        into :new.reach_coef_id
        from dual;
    end if;
  end if;
end;
--rollback drop trigger reach_auto_id_trig;

--changeset drsteini:triggers1d endDelimiter:/ splitStatements:false
create or replace trigger source_auto_id_trig
  before insert on source
  for each row
begin
  if inserting then
    if :new.source_id is null then
      select source_seq.nextval
        into :new.source_id
        from dual;
    end if;
  end if;
end;
--rollback drop trigger source_auto_id_trig;

--changeset drsteini:triggers1e endDelimiter:/ splitStatements:false
create or replace trigger source_reach_coef_auto_id_trig
  before insert on source_reach_coef
  for each row
begin
  if inserting then
    if :new.source_reach_coef_id is null then
      select source_reach_coef_seq.nextval
        into :new.source_reach_coef_id
        from dual;
    end if;
  end if;
end;
--rollback drop trigger source_reach_coef_auto_id_trig;

--changeset drsteini:triggers1f endDelimiter:/ splitStatements:false
create or replace trigger source_value_auto_id_trig
  before insert on source_value
  for each row
begin
  if inserting then
    if :new.source_value_id is null then
      select source_value_seq.nextval
        into :new.source_value_id
        from dual;
    end if;
  end if;
end;
--rollback drop trigger source_value_auto_id_trig;

--changeset drsteini:triggers1g endDelimiter:/ splitStatements:false
create or replace trigger sparrow_model_auto_id_trig
  before insert on sparrow_model
  for each row
begin
  if inserting then
    if :new.sparrow_model_id is null then
      select sparrow_model_seq.nextval
        into :new.sparrow_model_id from dual;
    end if;
  end if;
end;
--rollback drop trigger sparrow_model_auto_id_trig;
