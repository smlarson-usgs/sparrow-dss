package gov.usgswim.sparrow;

import gov.usgswim.sparrow.datatable.PredictResultImm;

public interface Runner {
	public PredictResultImm doPredict() throws Exception;

}