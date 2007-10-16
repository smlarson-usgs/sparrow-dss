package gov.usgswim.sparrow.service;

import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.ThreadSafe;
import gov.usgswim.service.HttpRequestHandler;
import gov.usgswim.sparrow.AdjustmentSetImm;
import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DPercentCompare;
import gov.usgswim.sparrow.Data2DView;
import gov.usgswim.sparrow.PredictRequest;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLOutputFactory;

import org.apache.log4j.Logger;


/**
 * A service that accepts requests for SPARROW predictions and returns an
 * xml document containing the predictions.
 *
 * In order to support local requests on the map server, this service also
 * has a runPrediction() method that returns raw data in a Data2D format,
 * so that no inter-server communication is needed if the service is running
 * within the MapViewer server.  (similar to the EJB local interface)
 */
@ThreadSafe
public class PredictService implements HttpRequestHandler<PredictServiceRequest> {
			
			
	protected static Logger log =
		Logger.getLogger(PredictService.class); //logging for this class
		
	protected static String RESPONSE_MIME_TYPE = "application/xml";
		
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;
	
	
	public PredictService() {
	}
	
	
	public void dispatch(PredictServiceRequest req, HttpServletResponse response) throws Exception {
		response.setContentType(RESPONSE_MIME_TYPE);
		dispatch(req, response.getOutputStream());
	}
	
	/**
	 * This is really a test method and not part of any of the standard interfaces.
	 * 
	 * It provides direct access to the raw prediction data for an imcoming xml
	 * request, w/o requiring the data to be serialized back to XML.
	 * 
	 * @param in
	 * @return
	 * @throws Exception
	 */
	public Data2D dispatchDirect(PredictServiceRequest req) throws Exception {
		return runPrediction(req);
	}
	
	public void dispatch(PredictServiceRequest req, OutputStream outStream) throws Exception {
																																 
		synchronized (factoryLock) {
			if (xoFact == null) {
				xoFact = WstxOutputFactory.newInstance();
			}
		}
		
		Data2D result = runPrediction(req);

		PredictSerializer ps = new PredictSerializer();
		ps.writeResponse(outStream, req, result);

	}
	
	public Data2D runPrediction(PredictServiceRequest req) {
		Data2D result = null;		//The prediction result
		
		Long modelId = req.getPredictRequest().getModelId();
		long startTime = System.currentTimeMillis();	//Time started

		try {

			Data2D adjResult = SharedApplication.getInstance().getPredictResultCache().compute(req.getPredictRequest());
			
			if (req.getPredictType().isComparison()) {
				//need to run the base prediction and the adjusted prediction
				AdjustmentSetImm noAdj = new AdjustmentSetImm();
				PredictRequest noAdjRequest = new PredictRequest(modelId, noAdj);
				Data2D noAdjResult = SharedApplication.getInstance().getPredictResultCache().compute(noAdjRequest);
	
				result = new Data2DPercentCompare(
						noAdjResult, adjResult,
						req.getPredictType().equals(gov.usgswim.sparrow.service.PredictServiceRequest.PredictType.DEC_CHG_FROM_NOMINAL));
						
			} else {
				//need to run only the adjusted prediction
				result = adjResult;
			}
			
			log.debug("Predict service done for model #" + modelId + " (" + result.getRowCount() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");
			
			if (req.getDataSeries() == gov.usgswim.sparrow.service.PredictServiceRequest.DataSeries.ALL) {
				//return all results
				return result;
			} else if (req.getDataSeries().getAggColumnIndex() > -1){
				//Return only the requested series
				int firstAggCol = result.getColCount() - 2;
				int dataCol = firstAggCol + req.getDataSeries().getAggColumnIndex();
				return new Data2DView(result, dataCol, 1);
			} else {
				//should handle these special - for now return all data
				return result;
			}
			
		} catch (Exception e) {
			log.error("No way to indicate this error to mapViewer, so throwing a runtime exception.", e);
			throw new RuntimeException(e);
		}
		

	}


	public void shutDown() {
		xoFact = null;
	}
}
