package gov.usgswim.sparrow.service.predictcontext;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class PredictContextPipeline extends AbstractPipeline<PredictContextRequest>{
	public PredictContextPipeline(){
		super(new PredictContextService(), new PredictContextParser());
//		super(null, null);
	}
	
	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		// no flattening for PredictContext pipeline
		return new XMLPassThroughFormatter();
	}


}
