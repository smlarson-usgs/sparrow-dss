package gov.usgswim.sparrow.service.idbypoint;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class IDByPointPipeline extends AbstractPipeline<IDByPointRequest> {


	public IDByPointPipeline(){
		super(new IDByPointService(), new IDByPointParser());
	}

	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		return new SparrowFlatteningFormatter(outputType);
	}


}
