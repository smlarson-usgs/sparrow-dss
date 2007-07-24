package gov.usgswim.sparrow.service;

import gov.usgswim.NotThreadSafe;
import gov.usgswim.sparrow.PredictionRequest;

import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Represents the entire state of a prediction request.
 * 
 * This is more then just a PredictionRequest, which only contains the info
 * needed to define a prediction to run.  This class includes additional
 * data such as which data to return, wheather to return raw data or comparisons,
 * etc..
 * 
 * Most of the set methods are 'friendly' to null values.  If there is a default
 * value, an attempt to set to null is simply ignored.
 */
@NotThreadSafe
public class PredictServiceRequest {

	private ResponseFilter responseType = PredictServiceRequest.ResponseFilter.ALL;
	private PredictType predictType = gov.usgswim.sparrow.service.PredictServiceRequest.PredictType.VALUES;
	private PredictionRequest predictionRequest;
	private DataSeries dataSeries = PredictServiceRequest.DataSeries.TOTAL;
	private Point.Double filterPoint;
	private Integer numberOfResults = null;


	public enum DataSeries {
		All("all", "Return all values"),
		TOTAL("total", "Return total predicted values"),
		INCREMENTAL_ADD("incremental", "Return the incremental added in the catch (not decayed)"),
		DECAYED("decay", "Return the amount decayed in each reach");
		
		private String _name;
		private String _desc;
		DataSeries(String name, String description) {
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
		
		public static DataSeries find(String name) {
			for(DataSeries type: PredictServiceRequest.DataSeries.values()) {
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
			for(PredictType type: gov.usgswim.sparrow.service.PredictServiceRequest.PredictType.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return null;
		}
	}
	
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
			for(ResponseFilter type: PredictServiceRequest.ResponseFilter.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return null;
		}
			
	};
	
	public PredictServiceRequest() {
	}
	
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

	public void setPredictionRequest(PredictionRequest predictionRequest) {
		this.predictionRequest = predictionRequest;
	}

	public PredictionRequest getPredictionRequest() {
		return predictionRequest;
	}

	public void setFilterPoint(Point2D.Double idPoint) {
		this.filterPoint = idPoint;
	}

	public Point2D.Double getFilterPoint() {
		return filterPoint;
	}
	
	public void setDataSeries(PredictServiceRequest.DataSeries dataSeries) {
		if (dataSeries != null) {
			this.dataSeries = dataSeries;
		}
	}

	public PredictServiceRequest.DataSeries getDataSeries() {
		return dataSeries;
	}
	
	public void setNumberOfResults(Integer numberOfResults) {
		this.numberOfResults = numberOfResults;
	}

	public Integer getNumberOfResults() {
		return numberOfResults;
	}
}
