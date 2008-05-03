package gov.usgswim.sparrow.service.json;

import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class JSONifyPipeline extends AbstractPipeline implements Pipeline {

	protected JSONifyPipeline() {
		super(null, null);
	}

	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		// TODO Auto-generated method stub
		return null;
	}

}
