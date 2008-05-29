package gov.usgswim.sparrow.deprecated;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;
import gov.usgswim.sparrow.service.predict.PredictPipeline;

public class IDByPointPipeline extends AbstractPipeline<IDByPointRequest_old> {


	public IDByPointPipeline(){
		super(new IDByPointService(), new IDByPointParser());
	}

	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		return new SparrowFlatteningFormatter(outputType);
	}

	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return PredictPipeline.configure(new JSONFormatter());
	}


}
