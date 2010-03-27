package gov.usgswim.sparrow.domain;

import gov.usgswim.Immutable;

import java.io.Serializable;

@Immutable
public class CalibrationSiteImm implements CalibrationSite, Serializable {

	private static final long serialVersionUID = -9200698117625690760L;

	private final Double actualValue;
	private final Double latitude;
	private final Double longitude;
	private final Long modelReachId;
	private final Double predictedValue;
	private final String stationId;
	private final String stationName;
	
	public CalibrationSiteImm(Long modelReachId, Double latitude, 
			Double longitude, Double actualValue, Double predictedValue, 
			String stationId, String stationName) {
		this.modelReachId = modelReachId;
		this.latitude = latitude;
		this.longitude = longitude;
		this.actualValue = actualValue;
		this.predictedValue = predictedValue;
		this.stationId = stationId;
		this.stationName = stationName;
	}
	
	@Override
	public Double getActualValue() {
		return this.actualValue;
	}

	@Override
	public Double getLatitude() {
		return this.latitude;
	}

	@Override
	public Double getLongitude() {
		return this.longitude;
	}

	@Override
	public Long getModelReachId() {
		return this.modelReachId;
	}

	@Override
	public Double getPredictedValue() {
		return this.predictedValue;
	}

	@Override
	public String getStationId() {
		return this.stationId;
	}

	@Override
	public String getStationName() {
		return this.stationName;
	}

}
