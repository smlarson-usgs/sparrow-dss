package gov.usgswim.sparrow.service.predictcontext;

import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class PredictContextService implements HttpRequestHandler<PredictContextRequest> {

	public XMLStreamReader getXMLStreamReader(PredictContextRequest o,
			boolean isNeedsCompleteFirstRow) throws Exception {

		//Store to cache
		SharedApplication.getInstance().putPredictionContext(o.getPredictionContext());
		
		boolean success = true;
		XMLInputFactory inFact = XMLInputFactory.newInstance();
		if (success) {
			return inFact.createXMLStreamReader(new StringReader("<prediction-context-response><status>OK</status></prediction-context-response>"));
//			<prediction-context-response
//			  xmlns="http://www.usgs.gov/sparrow/prediction-schema/v0_2"
//				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
//				model-id="22" context-id="389392">
//				
//				<status>OK</status>
//				<message></message>
//				<cache-lifetime-seconds>86400</cache-lifetime-seconds>
//				
//				<!--
//				The response document indicates that the submission was received and will
//				be stored on the server for 86400 seconds (24 hours).  During that time, this
//				prediction context can be refered to by the context-id (389392 from above),
//				or the four pieces of the prediction context can be used seperately via their
//				individual context-id's (below).
//				
//				Any use of the prediction context (or any piece of it) will reset the cache
//				timer.
//				
//				The treatment groups, terminus-reaches, and area-of-interest are all very
//				model specific, so it seems like this document should continue to contain
//				the model-id (at top) to reflect that.
//				-->
//				<row-ids>reach | HUC8 | HUC6 | HUC4 | HUC2 | state | county</row-ids>
//				
//				<!-- Here a predefined treatment group was used, so both IDs are returned -->
//				<adjustment-groups predefined="957648" context-id="982398"/>
//				<analysis context-id="1093474"/>
//				<terminal-reaches context-id="127830"/>
//				<area-of-interest context-id="2947593"/>
//
//			</prediction-context-response>
		}
		return inFact.createXMLStreamReader(new StringReader("<prediction-context-response><status>Failed</status></prediction-context-response>"));
	}

	public void shutDown() {
		// TODO Auto-generated method stub

	}

}
