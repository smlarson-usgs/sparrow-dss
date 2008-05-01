package gov.usgswim.sparrow.service.predictcontext;

import gov.usgs.webservices.framework.formatter.DataFlatteningFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter;
import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.AbstractPipeline;

public class PredictContextPipeline extends AbstractPipeline{
	public PredictContextPipeline(){
//		super(new PredictContextService(), new PredictionContext());<PredictionContext> 
		super(null, null);
	}
	
	@Override
	protected IFormatter getCustomFlatteningFormatter(OutputType outputType) {
		DataFlatteningFormatter df = new DataFlatteningFormatter(outputType);
		df.setRowElementName("source");
		df.setKeepElderInfo(true);
		return df;
	}


}
