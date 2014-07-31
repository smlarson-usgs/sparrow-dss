--liquibase formatted sql

--This is for the sparrow_dss schema
 
--changeset drsteini:grants1a
grant select on model_calib_sites to sparrow_dss_ro;
--rollback revoke select on model_calib_sites from sparrow_dss_ro;

--changeset drsteini:grants1b
grant select on model_reach to sparrow_dss_ro;
--rollback revoke select on model_reach from sparrow_dss_ro;

--changeset drsteini:grants1c
grant select on model_reach_geom to sparrow_dss_ro;
--rollback revoke select on model_reach_geom from sparrow_dss_ro;

--changeset drsteini:grants1d
grant select on reach_coef to sparrow_dss_ro;
--rollback revoke select on reach_coef from sparrow_dss_ro;

--changeset drsteini:grants1e
grant select on source to sparrow_dss_ro;
--rollback revoke select on source from sparrow_dss_ro;

--changeset drsteini:grants1f
grant select on source_reach_coef to sparrow_dss_ro;
--rollback revoke select on source_reach_coef from sparrow_dss_ro;

--changeset drsteini:grants1g
grant select on source_value to sparrow_dss_ro;
--rollback revoke select on source_value from sparrow_dss_ro;

--changeset drsteini:grants1h
grant select on sparrow_model to sparrow_dss_ro;
--rollback revoke select on sparrow_model from sparrow_dss_ro;
