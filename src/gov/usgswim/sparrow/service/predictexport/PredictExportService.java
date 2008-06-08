package gov.usgswim.sparrow.service.predictexport;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.PropertyLoaderHelper;

import javax.xml.stream.XMLStreamReader;

public class PredictExportService implements HttpService<PredictExportRequest> {
	
	private PropertyLoaderHelper props = new PropertyLoaderHelper("gov/usgswim/sparrow/service/predictcontext/PredictContextServiceTemplate.properties");

	public XMLStreamReader getXMLStreamReader(PredictExportRequest o,
			boolean isNeedsCompleteFirstRow) throws Exception {


		PredictionContext context = SharedApplication.getInstance().getPredictionContext(o.getContextID());
		PredictResult result = SharedApplication.getInstance().getAnalysisResult(context);
		PredictData data = SharedApplication.getInstance().getPredictData(context.getModelID());
		
		if (context.getAdjustmentGroups() != null) {
		
			DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(context.getAdjustmentGroups());
			data = new PredictDataImm(data.getTopo(), data.getCoef(), adjSrc, data.getSrcMetadata(), data.getDecay(),
					data.getSys(), data.getAncil(), data.getModel());
			
		}
		
		PredictExportSerializer ser = new PredictExportSerializer(o, result, data);
		
		return ser;
	}

	public void shutDown() {
		// TODO Auto-generated method stub

	}
	


}
