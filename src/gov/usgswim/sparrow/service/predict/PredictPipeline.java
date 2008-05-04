package gov.usgswim.sparrow.service.predict;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class PredictPipeline extends AbstractPipeline<PredictServiceRequest> {
	public static JSONFormatter configure(JSONFormatter jFormatter) {
		jFormatter.identifyRepeatedTagElement("columns", "group");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "col");
		jFormatter.identifyRepeatedTagElement("data", "r");
		jFormatter.identifyRepeatedTagElement("r", "c");
		return jFormatter;
	}

	public PredictPipeline(){
		super(new PredictService(), new PredictParser());
	}
	
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		return new SparrowFlatteningFormatter(outputType);
	}

	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return configure(new JSONFormatter());
	}
}
