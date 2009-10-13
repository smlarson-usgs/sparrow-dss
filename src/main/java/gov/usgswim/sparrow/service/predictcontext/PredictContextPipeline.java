package gov.usgswim.sparrow.service.predictcontext;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class PredictContextPipeline extends AbstractPipeline<PredictContextRequest>{

	public static JSONFormatter configure(JSONFormatter jFormatter) {
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "reachGroup");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "adjustment");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "logical-set");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "reach");
		return jFormatter;
	}

	public PredictContextPipeline(){
		super(new PredictContextService(), new PredictContextParser());
//		super(null, null);
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		// no flattening for PredictContext pipeline
		return new XMLPassThroughFormatter();
	}

	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return configure(new JSONFormatter());
	}



}
