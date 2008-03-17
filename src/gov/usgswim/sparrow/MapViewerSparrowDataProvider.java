package gov.usgswim.sparrow;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.sparrow.service.PredictParser;
import gov.usgswim.sparrow.service.PredictService;
import gov.usgswim.sparrow.service.PredictServiceRequest;
import gov.usgswim.sparrow.service.PredictServiceRequest.DataSeries;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.io.StringReader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import javax.sql.DataSource;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import oracle.mapviewer.share.ext.NSDataProvider;
import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;
import oracle.mapviewer.share.Field;

import org.apache.commons.lang.StringUtils;



import org.apache.log4j.Logger;

import org.codehaus.stax2.XMLInputFactory2;

public class MapViewerSparrowDataProvider implements NSDataProvider {
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
	public static final String DATA_SERIES = "data-series";
	
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
	
	public MapViewerSparrowDataProvider() {
	}
	


	/**
	 * Called onces at creation time.
	 * @param properties
	 * @return
	 */
	public boolean init(Properties properties) {
		predictService = new PredictService();
		predictParser = new PredictParser();
		
		return true;
	}

	public NSDataSet buildDataSet(java.util.Properties params) {
		Hashtable hash = new Hashtable(13);

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
	public NSDataSet buildDataSet(Hashtable properties) {
		long startTime = System.currentTimeMillis();	//Time started
		
		//All request info is stored in this class
		PredictServiceRequest svsRequest;
		PredictRequest predictRequest;
		
		Data2D sysInfo = null;		//row id numbers for matching the data to the geometry
		Data2D result = null;			//The prediction result (raw data)
		NSDataSet nsData = null;	//The Mapviewer data format for the data
		
		if (properties.containsKey(XML_REQUEST_KEY) && properties.get(XML_REQUEST_KEY) != null) {
			log.debug("Request treated as xml request.");
			
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
			
		} else {
			log.debug("Request treated as parameter request.");
			
			long modelId = Long.parseLong( properties.get(MODEL_ID_KEY).toString() );
			
			//Build the prediction request
			AdjustmentSetBuilder adjBuilder = new AdjustmentSetBuilder();
			List<Adjustment> adjs = adjBuilder.parseGrossAdj((String) properties.get(GROSS_SOURCE_ADJUST_KEY));
			for (Adjustment a: adjs) {
				adjBuilder.addAdjustment(a);
			}
			
			predictRequest = new PredictRequest(modelId, adjBuilder.getImmutable());
			
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
			svsRequest.setDataSeries(PredictServiceRequest.DataSeries.find((String) properties.get(DATA_SERIES)) );

			
			//Make this default to TOTAL instead of all
			if (svsRequest.getDataSeries() == gov.usgswim.sparrow.service.PredictServiceRequest.DataSeries.ALL) svsRequest.setDataSeries(gov.usgswim.sparrow.service.PredictServiceRequest.DataSeries.TOTAL);
				
			log.debug("Using Dataseries: " + svsRequest.getDataSeries() + " & result mode: " + svsRequest.getPredictType());
			
		}



		//RUN THE SERVICE REQUEST
		result = predictService.runPrediction(svsRequest);
		
		
		try {
			sysInfo = SharedApplication.getInstance().getPredictDatasetCache().compute( predictRequest.getModelId() ).getSys();
		} catch (Exception e) {
			log.error("No way to indicate this error to mapViewer, so returning null", e);
			return null;
		}
		
		nsData = copyToNSDataSet(result, sysInfo, svsRequest.getDataSeries());
		
		log.debug("MVSparrowDataProvider done for model #" + predictRequest + " (" + nsData.size() + " rows) Time: " + (System.currentTimeMillis() - startTime) + "ms");
		
		return nsData;

	}
	
	protected NSDataSet copyToNSDataSet(Data2D result, Data2D sysInfo, PredictServiceRequest.DataSeries column) {

		int rowCount = result.getRowCount();
		NSRow[] nsRows = new NSRow[rowCount];
		
		final int dataColIndex = 0;	//we always place the data in the first column
		
		for (int r=0; r < rowCount; r++) {
			Field[] row = new Field[2];	//ID
			row[0] = new Field(sysInfo.getInt(r, 0));
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
	public void destroy() {
		predictService.shutDown();
		predictService = null;
		predictParser = null;
	}
	

	
}
