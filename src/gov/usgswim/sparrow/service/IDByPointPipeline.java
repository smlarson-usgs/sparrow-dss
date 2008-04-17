package gov.usgswim.sparrow.service;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;

public class IDByPointPipeline extends AbstractPipeline<IDByPointRequest> {


	public IDByPointPipeline(){
		super(new IDByPointService(), new IDByPointParser());
	}

	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		return new SparrowFlatteningFormatter(outputType);
	}


}
