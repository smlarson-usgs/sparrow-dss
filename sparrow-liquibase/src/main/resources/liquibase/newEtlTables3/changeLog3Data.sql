--liquibase formatted sql

--This is for the sparrow_dss schema
 
--logicalFilePath: changeLog3Data.sql

--changeset drsteini:data3a
--(mrb, constituent, number_of_sources, iteration, sparrow_model_id, enh_network_id)
insert into etl_parameters values ('National','TN', 10, 0, 22, 22);
insert into etl_parameters values ('National','TP', 8, 0, 23, 22);
insert into etl_parameters values ('National','TOC', 7, 0, 24, 22);
insert into etl_parameters values ('National','Sediment', 6, 0, 30, 22);
insert into etl_parameters values ('MRB05','TN', 7, 0, 35, 23);
insert into etl_parameters values ('MRB05','TP', 7, 0, 36, 23);
insert into etl_parameters values ('MRB03','TN', 5, 0, 41, 23);
insert into etl_parameters values ('MRB03','TP', 6, 0, 42, 23);
insert into etl_parameters values ('MRB07','TN', 10, 0, 43, 23);
insert into etl_parameters values ('MRB07','TP', 6, 0, 44, 23);
insert into etl_parameters values ('MRB2', 'TP', 6, 0, 49, 23);
insert into etl_parameters values ('MRB2', 'TN', 5, 0, 50, 23);
insert into etl_parameters values ('MRB01', 'TN', 6, 0, 51, 43);
insert into etl_parameters values ('MRB01', 'TP', 6, 0, 52, 43);
insert into etl_parameters values ('MRB06','TDS', 15, 0, 53, 23);
insert into etl_parameters values ('Chesapeake','TN', 5, 0, 54, 43);
insert into etl_parameters values ('Chesapeake','TP', 6, 0, 55, 43);
insert into etl_parameters values ('MRB04','TN', 5, 0, 57, 23);
insert into etl_parameters values ('MRB04','TP', 5, 0, 58, 23);
commit;
--rollback truncate table etl_parameters;
