package gov.usgswim.sparrow.service.predictcontext;

import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;

public class PredictContextService implements HttpRequestHandler<PredictContextRequest> {

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
			
			
			String response = getText("ResponseOK", 
				new String[] {
					"ModelId", context.getModelID().toString(),
					"ContextId", Integer.toString( context.hashCode() ),
					"RowIdType", "reach",
					"AdjustmentContextId", Integer.toString( context.getAdjustmentGroups().hashCode() ),
					"AnalysisContextId", Integer.toString( context.getAnalysis().hashCode() ),
					"TerminalContextId", Integer.toString( context.getTerminalReaches().hashCode() ),
					"AreaOfInterstContextId", "NEED-AREA-OF-INTEREST"
			});
			
			return inFact.createXMLStreamReader(new StringReader(response));
		}
		// failure
		return inFact.createXMLStreamReader(new StringReader("<prediction-context-response><status>Failed</status></prediction-context-response>"));
	}

	public void shutDown() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Loads the named text chunk from the properties file and inserts the named values passed in params.
	 * 
	 * params are passed in serial pairs as {"name1", "value1", "name2", "value2"}.
	 * toString is called on each item, so it is OK to pass in autobox numerics.
	 * See the DataLoader.properties file for the names of the parameters available
	 * for the requested query.
	 * 
	 * @param name	Name of the query in the properties file
	 * @param params	An array of name and value objects to replace in the query.
	 * @return
	 * @throws IOException
	 */
	public static String getText(String name, Object[] params) throws IOException {
		String query = getText(name);

		for (int i=0; i<params.length; i+=2) {
			String n = "$" + params[i].toString() + "$";
			String v = params[i+1].toString();

			query = StringUtils.replace(query, n, v);
		}

		return query;
	}
	
	public static String getText(String name) throws IOException {
		Properties props = new Properties();

		props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("gov/usgswim/sparrow/service/predictcontext/PredictContextServiceTemplate.properties"));

		return props.getProperty(name);
	}

}
