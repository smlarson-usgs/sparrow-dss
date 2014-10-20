--liquibase formatted sql

--changeset kmschoep:update_themeName
UPDATE sparrow_model
SET theme_name = 'chesa_nhd'
where sparrow_model_id = 54 OR sparrow_model_id = 55;
--rollback UPDATE sparrow_model SET theme_name = 'mrb01_nhd' where sparrow_model_id = 54 OR sparrow_model_id = 55;