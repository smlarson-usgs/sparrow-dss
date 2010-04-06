This folder should contain predict.txt files for each model loaded in the database.
The file should be renamed X.txt where X is the model number in the db.

The columns in each file should be renamed as follows:
i1 - ix : Incremental predicted load for sources 1 - x
t1- tx : Total predicted load for sources 1 - x
ia : Incremental predicted load for all sources
ta : Total predicted load for all sources

For purposes of comparing data at an individual reach, here is a handle sql statement:

select src.identifier, val.value, rcoef.inc_delivery, srcoef.value as land_to_water,
	val.value *  rcoef.inc_delivery * srcoef.value as decayed_inc_pred_value,
	val.value * srcoef.value as non_decayed_inc_pred_value
from
    model_reach reach inner join Source src on reach.sparrow_model_id = src.sparrow_model_id
    inner join Source_value val on (src.source_id = val.source_id and reach.model_reach_id = val.model_reach_id)
    inner join reach_coef rcoef on (reach.model_reach_id = rcoef.model_reach_id)
    inner join source_reach_coef srcoef on (reach.model_reach_id = srcoef.model_reach_id and src.source_id = srcoef.source_id)
where reach.sparrow_model_id = 36 and reach.identifier = 21300 and rcoef.iteration = 0 and srcoef.iteration = 0
order by src.identifier