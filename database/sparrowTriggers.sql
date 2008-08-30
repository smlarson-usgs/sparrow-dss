--------------------------------------------------------
--  File created - Thursday-August-28-2008   
--------------------------------------------------------

--------------------------------------------------------
--  DDL for Trigger MODEL_REACH_AUTO_ID_TRIG
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "SPARROW_DSS"."MODEL_REACH_AUTO_ID_TRIG" BEFORE
INSERT ON "SPARROW_DSS"."MODEL_REACH" REFERENCING NEW AS newRow
FOR EACH ROW   WHEN (newRow.MODEL_REACH_ID IS NULL) BEGIN
  SELECT MODEL_REACH_SEQ.nextval INTO :newRow.MODEL_REACH_ID FROM dual;
END;


/
ALTER TRIGGER "SPARROW_DSS"."MODEL_REACH_AUTO_ID_TRIG" ENABLE;
 
--------------------------------------------------------
--  DDL for Trigger REACH_AUTO_ID_TRIG
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "SPARROW_DSS"."REACH_AUTO_ID_TRIG" before insert on "REACH_COEF"    for each row begin     if inserting then       if :NEW."REACH_COEF_ID" is null then          select REACH_COEF_SEQ.nextval into :NEW."REACH_COEF_ID" from dual;       end if;    end if; end;

/
ALTER TRIGGER "SPARROW_DSS"."REACH_AUTO_ID_TRIG" ENABLE;
 
--------------------------------------------------------
--  DDL for Trigger SOURCE_AUTO_ID_TRIG
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "SPARROW_DSS"."SOURCE_AUTO_ID_TRIG" before insert on "SOURCE"    for each row begin     if inserting then       if :NEW."SOURCE_ID" is null then          select SOURCE_SEQ.nextval into :NEW."SOURCE_ID" from dual;       end if;    end if; end;

/
ALTER TRIGGER "SPARROW_DSS"."SOURCE_AUTO_ID_TRIG" ENABLE;
 
--------------------------------------------------------
--  DDL for Trigger SOURCE_REACH_COEF_AUTO_ID_TRIG
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "SPARROW_DSS"."SOURCE_REACH_COEF_AUTO_ID_TRIG" before insert on "SOURCE_REACH_COEF"    for each row begin     if inserting then       if :NEW."SOURCE_REACH_COEF_ID" is null then          select SOURCE_REACH_COEF_SEQ.nextval into :NEW."SOURCE_REACH_COEF_ID" from dual;       end if;    end if; end;

/
ALTER TRIGGER "SPARROW_DSS"."SOURCE_REACH_COEF_AUTO_ID_TRIG" ENABLE;
 
--------------------------------------------------------
--  DDL for Trigger SOURCE_VALUE_AUTO_ID_TRIG
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "SPARROW_DSS"."SOURCE_VALUE_AUTO_ID_TRIG" before insert on "SOURCE_VALUE"    for each row begin     if inserting then       if :NEW."SOURCE_VALUE_ID" is null then          select SOURCE_VALUE_SEQ.nextval into :NEW."SOURCE_VALUE_ID" from dual;       end if;    end if; end;

/
ALTER TRIGGER "SPARROW_DSS"."SOURCE_VALUE_AUTO_ID_TRIG" ENABLE;
 
--------------------------------------------------------
--  DDL for Trigger SPARROW_MODEL_AUTO_ID_TRIG
--------------------------------------------------------

  CREATE OR REPLACE TRIGGER "SPARROW_DSS"."SPARROW_MODEL_AUTO_ID_TRIG" before insert on "SPARROW_MODEL"    for each row begin     if inserting then       if :NEW."SPARROW_MODEL_ID" is null then          select SPARROW_MODEL_SEQ.nextval into :NEW."SPARROW_MODEL_ID" from dual;       end if;    end if; end;

/
ALTER TRIGGER "SPARROW_DSS"."SPARROW_MODEL_AUTO_ID_TRIG" ENABLE;
 