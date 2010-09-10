package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.PredictData;

/**
 * Interface for an Action that loads ModelPredictData.
 * The interface allows the factory to use either the db based loader or the
 * file based loader.
 * 
 * @author eeverman
 *
 */
public interface ILoadModelPredictData extends IAction<PredictData>{

	public Long getModelId();

	public void setModelId(Long modelId);

}