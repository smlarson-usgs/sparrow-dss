package gov.usgswim.sparrow.service.model;

import gov.usgs.webservices.framework.formatter.DataFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.JSONFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.service.AbstractPipeline;


public class ModelPipeline extends AbstractPipeline<ModelRequest> {
	public static JSONFormatter configure(JSONFormatter jFormatter) {
		jFormatter.identifyRepeatedTagElement("models", "model");
		jFormatter.identifyRepeatedTagElement("sources", "source");
		return jFormatter;
	}

	public ModelPipeline(){
		super(new ModelService(), new ModelParser());
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		DataFlatteningFormatter df = new DataFlatteningFormatter(outputType);
		df.setRowElementName("source");
		df.setKeepElderInfo(true);
		return df;
	}

	@Override
	public IFormatter getConfiguredJSONFormatter() {
		return configure(new JSONFormatter());
	}

}
