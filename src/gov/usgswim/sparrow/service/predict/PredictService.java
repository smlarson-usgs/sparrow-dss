package gov.usgswim.sparrow.service.predict;

import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.ThreadSafe;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.ComparePercentageView;
import gov.usgswim.datatable.adjustment.FilteredDataTable;
import gov.usgswim.datatable.impl.DataTableUtils;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.service.HttpService;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.AdjustmentSet;
import gov.usgswim.sparrow.AdjustmentSetImm;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.PredictRequest;
import gov.usgswim.sparrow.PredictResult;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.task.ComputableCache;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;

/**
 * A service that accepts requests for SPARROW predictions and returns an
 * xml document containing the predictions.
 *
 * In order to support local requests on the map server, this service also
 * has a runPrediction() method that returns raw data in a DataTable format,
 * so that no inter-server communication is needed if the service is running
 * within the MapViewer server.  (similar to the EJB local interface)
 */
@ThreadSafe
public class PredictService implements HttpService<PredictServiceRequest> {


	protected static Logger log =
		Logger.getLogger(PredictService.class); //logging for this class

	protected static String RESPONSE_MIME_TYPE = "application/xml";


	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	//protected static XMLInputFactory xinFact;
	protected static XMLOutputFactory xoFact;

	public PredictService() {
	}

	public PredictResult runPrediction(Integer predictionContextId) {
		PredictionContext pc = SharedApplication.getInstance().getPredictionContext(predictionContextId);
		return runPrediction(pc);
	}
	
	public PredictResult runPrediction(PredictionContext context) {
		return SharedApplication.getInstance().getPredictResult(context);
	}
	
	
	public DataTable runPrediction(PredictServiceRequest req) {
		DataTable result = null;		//The prediction result

		Long modelId = req.getPredictRequest().getModelId();
		long startTime = System.currentTimeMillis();	//Time started

		try {
			ComputableCache<PredictRequest, PredictResult> pdCache = SharedApplication.getInstance().getPredictResultCache();
			PredictResult adjResult = pdCache.compute(req.getPredictRequest());

			TemporaryHelper.sampleDataTable(adjResult);

			if (req.getPredictType().isComparison()) {
				//need to run the base prediction and the adjusted prediction
				AdjustmentSet noAdj = new AdjustmentSetImm();
				PredictRequest noAdjRequest = new PredictRequest(modelId, noAdj);
				PredictResult noAdjResult = SharedApplication.getInstance().getPredictResultCache().compute(noAdjRequest);

				TemporaryHelper.sampleDataTable(noAdjResult);

				result = new ComparePercentageView(
						noAdjResult, adjResult,
						req.getPredictType().equals(gov.usgswim.sparrow.service.predict.PredictServiceRequest.PredictType.DEC_CHG_FROM_NOMINAL));

			} else {
				//need to run only the adjusted prediction
				result = adjResult;
			}

			log.debug("Predict service done for model #" + modelId + " (" + result.getRowCount() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");

			if (req.getDataSeries() == PredictServiceRequest.DataSeries.ALL) {
				//return all results
				return result;
			} else if (req.getDataSeries().getAggColumnIndex() > -1){
				//Return only the requested series
				int firstAggCol = result.getColumnCount()- 2;
				int dataCol = firstAggCol + req.getDataSeries().getAggColumnIndex();
				return new FilteredDataTable(result, dataCol, 1);
			} else {
				//should handle these special - for now return all data
				return result;
			}

		} catch (Exception e) {
			log.error("No way to indicate this error to mapViewer, so throwing a runtime exception.", e);
			throw new RuntimeException(e);
		}


	}


	/**
	 * Loads the model sources, coefs, and values needed to run the prediction.
	 * @param arg
	 * @return
	 */
	public PredictData loadData(PredictRequest req) throws Exception {
		ComputableCache<Long, PredictData> pdCache = SharedApplication.getInstance().getPredictDatasetCache();
		return pdCache.compute( req.getModelId() );
	}

	/**
	 * Adjusts the passed data based on the adjustments in the requests.
	 *
	 * It is assumed that the passed data is immutable, in which case a mutable
	 * builder is created, adjusted, copied as immutable, and returned.
	 * 
	 * If the passed data is mutable (an instance of PredictionDataBuilder,
	 * possibly for testing purposes), it will be adjusted by setting a new source,
	 * copied as immutable, and returned.
	 * 
	 * If there are no adjustments, the passed dataset is returned.
	 *
	 * @param req
	 * @param data
	 * @return
	 */
	public PredictData adjustData(PredictRequest req,
			PredictData data) throws Exception {

		if (req.getAdjustmentSet().hasAdjustments()) {
			PredictDataBuilder mutable = data.getBuilder();


			//This method does not modify the underlying data
			mutable.setSrc(
					req.getAdjustmentSet().adjust(mutable.getSrc(), mutable.getSrcMetadata(), mutable.getSys())
			);

			return mutable.toImmutable();
		}

		return data;
	}


	public void shutDown() {
		xoFact = null;
	}

	public XMLStreamReader getXMLStreamReader(PipelineRequest o, boolean needsCompleteFirstRow) throws Exception {
		return getXMLStreamReader((PredictServiceRequest) o, needsCompleteFirstRow);
	}

	public XMLStreamReader getXMLStreamReader(PredictServiceRequest req, boolean needsCompleteFirstRow) throws Exception {
		DataTable result = runPrediction(req);
		result = filterResults(result, req);
		PredictData adjData = null;
		if (req.getDataSeries().equals( PredictServiceRequest.DataSeries.ALL )) {
			//TODO This is a hack:  we throw away the adjusted data in the PredictComputable,
			//then recompute it here b/c there is no way back to it.
			PredictData data = loadData(req.getPredictRequest());	//is cached
			adjData = adjustData(req.getPredictRequest(), data);	//redo adjustement
		}
		return new PredictSerializer(req, result, adjData);
	}

	//TODO Not tested
	public DataTable filterResults(DataTable source, PredictServiceRequest req) throws Exception {

		if (req.getIdByPointRequest() != null) {

			//The IDByPointRequest returns data w/ reach Ids as IDs in the DataTable
			ComputableCache<IDByPointRequest, DataTable> idByPcache = SharedApplication.getInstance().getIdByPointCache();
			Long[] rowIds = DataTableUtils.getRowIds(idByPcache.compute(req.getIdByPointRequest()));

			double[][] newData = new double[rowIds.length][];

			for (int index=0; index<rowIds.length; index++) {
				long rowId = rowIds[index];
				int rowIndex = source.getRowForId(rowId);
				double[] rowData = TemporaryHelper.getDoubleRow(source, rowIndex);
				newData[index] = rowData;
			}
			String[] headings = TemporaryHelper.getHeadings(source);
			SimpleDataTableWritable result = new SimpleDataTableWritable(newData, headings);
			TemporaryHelper.setIds(result, rowIds);
			return result;
			// TODO determine if necessary to add indexes

		} else {
			return source;
		}
	}

}

