package gov.usgswim.sparrow.service.predict;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class PredictPipeline extends AbstractPipeline<PredictServiceRequest> {

	public PredictPipeline(){
		super(new PredictService(), new PredictParser());
	}
	
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		return new SparrowFlatteningFormatter(outputType);
	}

}
