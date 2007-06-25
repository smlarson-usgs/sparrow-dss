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
 */
@NotThreadSafe
public class PredictServiceRequest {

	private ResponseType responseType = gov.usgswim.sparrow.service.PredictServiceRequest.ResponseType.ALL_RESULTS;
	private PredictType predictType = gov.usgswim.sparrow.service.PredictServiceRequest.PredictType.VALUES;
	private PredictionRequest predictionRequest;
	private DataColumn dataColumn = gov.usgswim.sparrow.service.PredictServiceRequest.DataColumn.TOTAL;
	private Point.Double idPoint;
	private Integer numberOfResults = null;


	public enum DataColumn {
		TOTAL("total", "Return total predicted values"),
		INCREMENTAL_ADD("inc_add", "Return the incremental added in the catch (not decayed)"),
		DECAYED("decayed", "Return the amount decayed in each reach");
		
		private String _name;
		private String _desc;
		DataColumn(String name, String description) {
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
		
		public static DataColumn find(String name) {
			for(DataColumn type: gov.usgswim.sparrow.service.PredictServiceRequest.DataColumn.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return null;
		}
	}
	
	public enum PredictType {
		VALUES("val", "Return predicted values", false),
		PERC_CHG_FROM_NOMINAL("percent_change", "Percent change from nominal predictions", true),
		DEC_CHG_FROM_NOMINAL("decimal_change", "Percent change (as a decimal) from nominal predictions", true);
		
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
	
	public enum ResponseType {
		ALL_RESULTS("all", "Return all results"),
		IDENTIFY_BY_POINT("id-by-pt", "Return results nearest a point");
		
		private String _name;
		private String _desc;
		ResponseType(String name, String description) {
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
		
		public static ResponseType find(String name) {
			for(ResponseType type: gov.usgswim.sparrow.service.PredictServiceRequest.ResponseType.values()) {
				if (type._name.equalsIgnoreCase(name)) return type;
			}
			return null;
		}
			
	};
	
	public PredictServiceRequest() {
	}
	
	public void setResponseType(PredictServiceRequest.ResponseType responseType) {
		this.responseType = responseType;
	}

	public PredictServiceRequest.ResponseType getResponseType() {
		return responseType;
	}

	public void setPredictType(PredictServiceRequest.PredictType predictType) {
		this.predictType = predictType;
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

	public void setIdPoint(Point2D.Double idPoint) {
		this.idPoint = idPoint;
	}

	public Point2D.Double getIdPoint() {
		return idPoint;
	}
	
	public void setDataColumn(PredictServiceRequest.DataColumn dataColumn) {
		this.dataColumn = dataColumn;
	}

	public PredictServiceRequest.DataColumn getDataColumn() {
		return dataColumn;
	}
	
	public void setNumberOfResults(Integer numberOfResults) {
		this.numberOfResults = numberOfResults;
	}

	public Integer getNumberOfResults() {
		return numberOfResults;
	}
}
