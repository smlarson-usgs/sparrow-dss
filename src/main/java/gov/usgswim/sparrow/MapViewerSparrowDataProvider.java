package gov.usgswim.sparrow;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.sparrow.deprecated.PredictParser;
import gov.usgswim.sparrow.deprecated.PredictService;
import gov.usgswim.sparrow.deprecated.PredictServiceRequest;
import gov.usgswim.sparrow.parser.AdvancedAnalysis;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.revised.RunModeController;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.task.ComputableCache;

import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import oracle.mapviewer.share.Field;
import oracle.mapviewer.share.ext.NSDataProvider;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

import org.apache.log4j.Logger;

public class MapViewerSparrowDataProvider
implements NSDataProvider
{
	protected static Logger log =
		Logger.getLogger(MapViewerSparrowDataProvider.class); //logging for this class


	PredictService predictService;
	AbstractHttpRequestParser<PredictServiceRequest> predictParser;


	//Request parameter key constants

	/**
	 * If a non-empty value is passed for this key value, all other parameters
	 * are ignored and the contents are assumed to be an xml request of type
	 * http://www.usgs.gov/sparrow/prediction-response/v0_1
	 */
	public static final String XML_REQUEST_KEY = "xml";

	/**
	 * The db unique id of the Sparrow model.  Required.
	 */
	public static final String MODEL_ID_KEY = "model_id";

	/**
	 * A string containing a delimited list, in pairs, of the source srcId and the
	 * decimal percentage to adjust it.  It is an error to provide a string that
	 * contains anything other then numbers and delimiters, or that contains
	 * an odd number of values.
	 *
	 * Example:  "1,.25,4,2,8,0"
	 *
	 * In this example:
	 * <ul>
	 * <li>Source #1 has its value multiplied by .25
	 * <li>Source #4 has its value multiplied by 2
	 * <li>Source #8 has its value multiplied by 0 (effectively turning it off)
	 * <li>ALL other sources not listed as assumed to be unchanged
	 * (that is, they are multiplied by 1).
	 * <ul>
	 */
	public static final String GROSS_SOURCE_ADJUST_KEY = "gross_src_adj";

	/**
	 * Determines what data is returned.  Valid values are the
	 * RESULT_MODE_VALUE_XXX constants.
	 */
	public static final String RESULT_MODE_KEY = "result_mode";

	/**
	 * Determines which column to map.
	 */
	public static final String DATA_SERIES = "dataSeries";

	/**
	 * A possible value for the RESULT_MODE_KEY parameter.
	 * This mode returns the new calculated value.
	 */
	public static final String RESULT_MODE_VALUE = "value";

	/**
	 * A possible value for the RESULT_MODE_KEY parameter.
	 * This mode returns the percentage change from the original model predicted value.
	 * The value is returned a whole percentage (ie, if the value doubled, 100 would be returned)
	 */
	public static final String RESULT_MODE_PERC_CHG = "perc_chg";

	/**
	 * A possible value for the RESULT_MODE_KEY parameter.
	 * This mode returns the percentage change from the original model predicted value.
	 * The value is returned a decimal percentage (ie, if the value doubled, 1 would be returned)
	 */
	public static final String RESULT_MODE_DEC_PERC_CHG = "dec_perc_chg";

	public static final String CONTEXT_ID = "context-id";

	/**
	 * The model id.  This is an alternate to CONTEXT_ID when a context-id is not
	 * available.
	 */
	public static final String MODEL_ID = "model-id";

	public MapViewerSparrowDataProvider() {
	}



	/**
	 * Called once at creation time.
	 * @param properties
	 * @return
	 */
	@Override
	public boolean init(Properties properties) {
		predictService = new PredictService();
		predictParser = new PredictParser();

		return true;
	}

	/**
	 * TODO [ik] figure out if this method is ever called. If not, we can remove
	 * deprecated methods called by it.
	 */
	@Override
	public NSDataSet buildDataSet(java.util.Properties params) {
		Hashtable<Object,Object> hash = new Hashtable<Object,Object>(13);

		for (Object key : params.keySet()) {
			hash.put(key, params.get(key));
		}

		return buildDataSet(hash);
	}


	/**
	 * Called for each request.
	 * @param properties
	 * @return
	 */
	public NSDataSet buildDataSet(Hashtable<?,?> properties) {
		long startTime = System.currentTimeMillis();	//Time started

		//All request info is stored in this class
		PredictServiceRequest svsRequest;
		PredictRequest predictRequest;

		DataTable topoInfo = null;	// row id numbers for matching the data to the geometry
		DataTable result = null;	// The prediction result (raw data)
		NSDataSet nsData = null;	// The Mapviewer data format for the data

		if (properties.containsKey(XML_REQUEST_KEY) && properties.get(XML_REQUEST_KEY) != null) {
			log.debug("Request treated as xml request.");
			RunModeController.checkDeprecation();
			//find the xml request as a property of the request
			String xmlReq = properties.get(XML_REQUEST_KEY).toString();

			try {
				svsRequest = predictParser.parse(xmlReq);

				//Make this default to TOTAL instead of all
				if (svsRequest.getDataSeries() == PredictServiceRequest.DataSeries.ALL) {
					svsRequest.setDataSeries(PredictServiceRequest.DataSeries.TOTAL);
				}

				predictRequest = svsRequest.getPredictRequest();
			} catch (XMLStreamException e) {
				throw new RuntimeException("Error reading the passed XML data", e);
			} catch (Exception e) {
				throw new RuntimeException("Error while handling request", e);
			}

			log.debug("Using Dataseries: " + svsRequest.getDataSeries() + " & result mode: " + svsRequest.getPredictType());

		} else if (properties.containsKey(CONTEXT_ID) && properties.get(CONTEXT_ID) != null) {
			log.debug("Main case: Request treated as a context-id request.");

			Integer contextId = Integer.parseInt(properties.get(CONTEXT_ID).toString());
			PredictionContext context = SharedApplication.getInstance().getPredictionContext(contextId);

			if (context != null) {

				try {
					nsData = copyToNSDataSet(context);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}

				log.debug("MVSparrowDataProvider done for model #" + context.getModelID() + " (" + nsData.size() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");

				return nsData;

			}
			throw new RuntimeException("No PredictionContext found for ID " + contextId);

		} else if (properties.containsKey(MODEL_ID) && properties.get(MODEL_ID) != null) {
			RunModeController.checkDeprecation();
			Long modelId = Long.parseLong(properties.get(MODEL_ID).toString());
			PredictionContext context = new PredictionContext(modelId, null, AdvancedAnalysis.getDefaultTotalAnalysis(), null, null);

			log.debug("MVDP model-id request (map calibrated state).  PC hash = " + context.hashCode());

			try {
				nsData = copyToNSDataSet(context);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

			log.debug("MVSparrowDataProvider done for model #" + context.getModelID() + " (" + nsData.size() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");

			return nsData;

		} else {
			log.debug("Request treated as parameter request.");
			RunModeController.checkDeprecation();
			long modelId = Long.parseLong(properties.get(MODEL_ID_KEY).toString());

			//Build the prediction request
			AdjustmentSetBuilder adjBuilder = new AdjustmentSetBuilder();
			List<Adjustment> adjs = adjBuilder.parseGrossAdj((String) properties.get(GROSS_SOURCE_ADJUST_KEY));
			for (Adjustment a : adjs) {
				adjBuilder.addAdjustment(a);
			}

			predictRequest = new PredictRequest(modelId, adjBuilder.toImmutable());

			//Build the service request
			svsRequest = new PredictServiceRequest();
			svsRequest.setPredictRequest(predictRequest);

			//this should work, but we are trying to be compatable w/ existing app
			//svsRequest.setPredictType(PredictServiceRequest.PredictType.find((String) properties.get(RESULT_MODE_KEY)) );
			//
			//Set resultType using old style parameters
			String predType = (String) properties.get(RESULT_MODE_KEY);
			if (RESULT_MODE_VALUE.equals(predType)) {
				svsRequest.setPredictType(PredictServiceRequest.PredictType.VALUES);
			} else if (RESULT_MODE_PERC_CHG.equals(predType)) {
				svsRequest.setPredictType(PredictServiceRequest.PredictType.PERC_CHG_FROM_NOMINAL);
			} else {
				//default
				svsRequest.setPredictType(PredictServiceRequest.PredictType.PERC_CHG_FROM_NOMINAL);
			}


			//new version still compatible w/ old here.
			svsRequest.setDataSeries(PredictServiceRequest.DataSeries.find((String) properties.get(DATA_SERIES)));


			//Make this default to TOTAL instead of all
			if (svsRequest.getDataSeries() == gov.usgswim.sparrow.deprecated.PredictServiceRequest.DataSeries.ALL) {
				svsRequest.setDataSeries(gov.usgswim.sparrow.deprecated.PredictServiceRequest.DataSeries.TOTAL);
			}
			log.debug("Using Dataseries: " + svsRequest.getDataSeries() + " & result mode: " + svsRequest.getPredictType());

		}



		//RUN THE SERVICE REQUEST
		result = predictService.runPrediction(svsRequest);


		try {
			ComputableCache<Long, PredictData> pdCache = SharedApplication.getInstance().getPredictDatasetCache();
			topoInfo = pdCache.compute(predictRequest.getModelId()).getTopo();
		} catch (Exception e) {
			log.error("No way to indicate this error to mapViewer, so returning null", e);
			return null;
		}

		nsData = copyToNSDataSet(result, topoInfo, svsRequest.getDataSeries());

		log.debug("MVSparrowDataProvider done for model #" + predictRequest + " (" + nsData.size() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");

		return nsData;

	}


	public NSDataSet copyToNSDataSet(PredictionContext context) throws Exception {
		PredictionContext.DataColumn dc = context.getDataColumn();

		int rowCount = dc.getTable().getRowCount();
		NSRow[] nsRows = new NSRow[rowCount];

		//Build the row of data
		for (int r = 0; r < rowCount; r++) {
			Field[] row = new Field[2];

			long id = -1L;
			try {
				id = dc.getTable().getIdForRow(r);
			} catch (NullPointerException npe) {
				PredictData nomPredictData = SharedApplication.getInstance().getPredictData(context.getModelID());
				id = nomPredictData.getTopo().getIdForRow(r);
			}
			// TODO [IK] try alternate path with id as string to try to solve
			// huc aggregation artificial key issue. This would require that the
			// DataTable argument passed in NOT use HUC ID as a DataTable ID,
			// which requires it to be a number, but rather as a column of data
			// in the datatable, and that the copyToNSDataSet be smart enough
			// to figure out what to do.
			row[0] = new Field(id);
			row[0].setKey(true);

			// Value
			Double val = dc.getTable().getDouble(r, dc.getColumn());
			if (val != null) {
				row[1] = new Field(val);
			} else {
				row[1] = new Field();
				row[1].setNull();
			}

			NSRow nsRow = new NSRow(row);
			nsRows[r] = nsRow;
		}

		if (log.isDebugEnabled()) debugNSData(nsRows);

		return new NSDataSet(nsRows);
	}

	@Deprecated
	protected NSDataSet copyToNSDataSet(DataTable result, DataTable topoInfo, PredictServiceRequest.DataSeries column) {
		int rowCount = result.getRowCount();
		NSRow[] nsRows = new NSRow[rowCount];

		final int dataColIndex = 0;	//we always place the data in the first column

		for (int r=0; r < rowCount; r++) {
			Field[] row = new Field[2];	//ID
			row[0] = new Field(topoInfo.getIdForRow(r));
			//row[0] = new Field(sysInfo.getInt(r, 0));
			row[0].setKey(true);

			row[1] = new Field(result.getDouble(r, dataColIndex));	//Value
			//row[1].setLabelText(true);

			NSRow nsRow = new NSRow(row);
			nsRows[r] = nsRow;
		}

		if (log.isDebugEnabled()) debugNSData(nsRows);

		return new NSDataSet(nsRows);
	}

	protected void debugNSData(NSRow[] nsRows) {
		int maxRow = 10;
		if (maxRow > nsRows.length) maxRow = nsRows.length;

		log.debug("MVSparrowDataProvider These are the first ten rows of data: ");
		for (int r = 0; r < maxRow; r++)  {
			StringBuffer sb = new StringBuffer();
			for (int c = 0; c < nsRows[0].size(); c++)  {
				sb.append(nsRows[r].get(c).toString());
				if (nsRows[r].get(c).isKey()) sb.append("[Key] ");
				if (nsRows[r].get(c).isLabelText()) sb.append("[Lab] ");
				if ((c + 1) < nsRows[0].size()) sb.append("| ");
			}
			log.debug(sb.toString());
		}
	}

	/**
	 * Called once when this instance is destroyed
	 */
	@Override
	public void destroy() {
		predictService.shutDown();
		predictService = null;
		predictParser = null;
	}



}

