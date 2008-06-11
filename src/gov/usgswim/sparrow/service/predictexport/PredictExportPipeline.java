package gov.usgswim.sparrow.service.predictexport;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class PredictExportPipeline extends AbstractPipeline<PredictExportRequest>{
	
	public static JSONFormatter configure(JSONFormatter jFormatter) {
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "reach-group");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "adjustment");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "logical-set");
		jFormatter.identifyRepeatedTagElement(JSONFormatter.ANY_PARENT, "reach");
		return jFormatter;
	}
	
	public PredictExportPipeline(){
		super(new PredictExportService(), new PredictExportParser());
//		super(null, null);
	}
	
	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		return new SparrowFlatteningFormatter(outputType);
	}

	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return configure(new JSONFormatter());
	}



}
