package gov.usgswim.sparrow.service.predictcontext;

import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.PropertyLoaderHelper;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class PredictContextService implements HttpService<PredictContextRequest> {
	
	private PropertyLoaderHelper props = new PropertyLoaderHelper("gov/usgswim/sparrow/service/predictcontext/PredictContextServiceTemplate.properties");

	public XMLStreamReader getXMLStreamReader(PredictContextRequest o,
			boolean isNeedsCompleteFirstRow) throws Exception {

		//Store to cache
		PredictionContext context = o.getPredictionContext();
		boolean isSuccess = false;
		try {
			SharedApplication.getInstance().putPredictionContext(context);
			isSuccess = true;
		} catch (Exception e) {
			// TODO need to log failure
			e.printStackTrace();
		}

		XMLInputFactory inFact = XMLInputFactory.newInstance();
		if (isSuccess) {
			
			String response = props.getText("ResponseOK", 
				new String[] {
					"ModelId", context.getModelID().toString(),
					"ContextId", Integer.toString( context.hashCode() ),
					"RowIdType", "reach",
					"AdjustmentContextId", Integer.toString( context.getAdjustmentGroups().hashCode() ),
					"AnalysisContextId", Integer.toString( context.getAnalysis().hashCode() ),
					"TerminalContextId", Integer.toString( context.getTerminalReaches().hashCode() ),
					"AreaOfInterstContextId", Integer.toString( context.getAreaOfInterest().hashCode() )
			});
			
			return inFact.createXMLStreamReader(new StringReader(response));
		}
		// failure
		return inFact.createXMLStreamReader(new StringReader("<prediction-context-response><status>Failed</status></prediction-context-response>"));
	}

	public void shutDown() {
		// TODO Auto-generated method stub

	}
	


}
