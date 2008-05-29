package gov.usgswim.sparrow.service.predict;

import gov.usgswim.NotThreadSafe;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.PredictRequest;
import gov.usgswim.sparrow.deprecated.IDByPointRequest_old;
import gov.usgswim.sparrow.parser.ResponseFormat;

/**
 * Holds the entire state of a prediction service request.
 * 
 * This is more then just a PredictRequest, which only contains the info
 * needed to define a prediction to run.  This class includes additional
 * data such as which data to return, wheather to return raw data or comparisons,
 * etc..
 * 
 * Most of the set methods are 'friendly' to null values.  If there is a default
 * value, an attempt to set to null is simply ignored.
 */
@NotThreadSafe
public class PredictServiceRequest implements PipelineRequest{

	private ResponseFilter responseType = gov.usgswim.sparrow.service.predict.PredictServiceRequest.ResponseFilter.ALL;
	private PredictType predictType = gov.usgswim.sparrow.service.predict.PredictServiceRequest.PredictType.VALUES;
	private PredictRequest predictRequest;
	private DataSeries dataSeries = gov.usgswim.sparrow.service.predict.PredictServiceRequest.DataSeries.ALL;
	private IDByPointRequest_old idByPointRequest;
	private String xmlRequest;
	private int rowLimit;
	private ResponseFormat responseFormat;


	public enum DataSeries {
		ALL("all", "Return all values", -1),
		TOTAL("total", "Return total predicted values", 1),
		INCREMENTAL_ADD("incremental", "Return the incremental added in the catch (not decayed)", 0),
		DECAYED("decay", "Return the amount decayed in each reach", -1);	//Dont have a column for this yet
		
		private String _name;
		private String _desc;
		private int _aggCol;
		DataSeries(String name, String description, int aggCol) {
			_name = name;
			_desc = description;
			_aggCol = aggCol;
		}
		
		public String toString() {
			return _name;
		}
		
		public String getName() {
			return _name;
		}
		
		public String getDescription() {
			return _desc;
		}
		
		/**
		 * Returns the column index of the data column, after all of the source
		 * columns.  A value of 0 is the first aggregate column (the first column
		 * beyond the source specific columns) and so on.  For dataseries that this
		 * does not apply to, -1 should be returned.
		 * @return
		 */
		public int getAggColumnIndex() {
			return _aggCol;
		}
		
		public static DataSeries find(String name) {
			for(DataSeries type: gov.usgswim.sparrow.service.predict.PredictServiceRequest.DataSeries.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return null;
		}
	}
	
	public enum PredictType {
		VALUES("value", "Return predicted values", false),
		PERC_CHG_FROM_NOMINAL("perc_change", "Percent change from nominal predictions", true),
		DEC_CHG_FROM_NOMINAL("dec_perc_change", "Percent change (as a decimal) from nominal predictions", true);
		
		private String _name;
		private String _desc;
		private boolean _compType;
		PredictType(String name, String description, boolean compType) {
			_name = name;
			_desc = description;
			_compType = compType;
		}
		
		public String toString() {
			return _name;
		}
		
		public String getName() {
			return _name;
		}
		
		public String getDescription() {
			return _desc;
		}
		
		public boolean isComparison() {
			return _compType;
		}
		
		public static PredictType find(String name) {
			for(PredictType type: gov.usgswim.sparrow.service.predict.PredictServiceRequest.PredictType.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return null;
		}
	}
	
	//TODO This is perhaps obsolete - the idbypoint is being handled by its own request...
	public enum ResponseFilter {
		ALL("all-results", "Return all results"),
		NEAR_POINT("near-point", "Return results nearest a point");
		
		private String _name;
		private String _desc;
		ResponseFilter(String name, String description) {
			_name = name;
			_desc = description;
		}
		
		public String toString() {
			return _name;
		}
		
		public String getName() {
			return _name;
		}
		
		public String getDescription() {
			return _desc;
		}
		
		public static ResponseFilter find(String name) {
			for(ResponseFilter type: gov.usgswim.sparrow.service.predict.PredictServiceRequest.ResponseFilter.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return null;
		}
			
	};
	
	public PredictServiceRequest() {
	}
	
	// -------------------
	// GETTERS AND SETTERS
	// -------------------
	public void setResponseType(PredictServiceRequest.ResponseFilter responseType) {
		if (responseType != null) {
			this.responseType = responseType;
		}
	}

	public PredictServiceRequest.ResponseFilter getResponseType() {
		return responseType;
	}

	public void setPredictType(PredictServiceRequest.PredictType predictType) {
		if (predictType != null) {
			this.predictType = predictType;
		}
	}

	public PredictServiceRequest.PredictType getPredictType() {
		return predictType;
	}

	/**
	 * @param predictionRequest
	 */
	public void setPredictRequest(PredictRequest predictionRequest) {
		this.predictRequest = predictionRequest;
	}
	

	/**
	 * @return
	 */
	public PredictRequest getPredictRequest() {
		return predictRequest;
	}
	
	public void setIdByPointRequest(IDByPointRequest_old idByPointRequest) {
		this.idByPointRequest = idByPointRequest;
	}

	public IDByPointRequest_old getIdByPointRequest() {
		return idByPointRequest;
	}
	
	public void setDataSeries(PredictServiceRequest.DataSeries dataSeries) {
		if (dataSeries != null) {
			this.dataSeries = dataSeries;
		}
	}

	public PredictServiceRequest.DataSeries getDataSeries() {
		return dataSeries;
	}
	
	public String getXMLRequest() {
		return xmlRequest;
	}

	public void setXMLRequest(String request) {
		xmlRequest = request;		
	}

	public void setRowLimit(int rowLimit) {
		this.rowLimit = rowLimit;
	}
	
	public int getRowLimit() {
		return rowLimit;
	}

	public void setResponseFormat(ResponseFormat respFormat) {
		this.responseFormat = respFormat;
		responseFormat.fileName = "predict";
	}
	
	public ResponseFormat getResponseFormat() {
		if (responseFormat == null) {
			setResponseFormat(new ResponseFormat());
		}
		return responseFormat;
	}
}
