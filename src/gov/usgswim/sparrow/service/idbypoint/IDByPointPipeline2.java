package gov.usgswim.sparrow.service.idbypoint;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;
import gov.usgswim.sparrow.service.predict.PredictPipeline;

public class IDByPointPipeline2 extends AbstractPipeline<IDByPointRequest2> {


	public IDByPointPipeline2(){
		super(new IDByPointService2(), new IDByPointParser2());
	}

	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		return new SparrowFlatteningFormatter(outputType);
	}

	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return PredictPipeline.configure(new JSONFormatter());
	}

}
