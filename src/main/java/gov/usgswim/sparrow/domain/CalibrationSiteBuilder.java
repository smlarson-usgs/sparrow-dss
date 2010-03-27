package gov.usgswim.sparrow.domain;

import gov.usgswim.ImmutableBuilder;

public class CalibrationSiteBuilder implements CalibrationSite, ImmutableBuilder<CalibrationSite> {

	private Double actualValue;
	private Double latitude;
	private Double longitude;
	private Long modelReachId;
	private Double predictedValue;
	private String stationId;
	private String stationName;
	
	public CalibrationSiteBuilder() {
		//
	}
	
	public CalibrationSiteBuilder(Long modelReachId, Double latitude, 
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

	public void setActualValue(Double actualValue) {
		this.actualValue = actualValue;
	}

	
	@Override
	public Double getLatitude() {
		return this.latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	
	@Override
	public Double getLongitude() {
		return this.longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	
	@Override
	public Long getModelReachId() {
		return this.modelReachId;
	}

	public void setModelReachId(Long modelReachId) {
		this.modelReachId = modelReachId;
	}

	
	@Override
	public Double getPredictedValue() {
		return this.predictedValue;
	}

	public void setPredictedValue(Double predictedValue) {
		this.predictedValue = predictedValue;
	}

	
	@Override
	public String getStationId() {
		return this.stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	
	@Override
	public String getStationName() {
		return this.stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}

	@Override
	public CalibrationSite toImmutable() throws IllegalStateException {
		CalibrationSite result = new CalibrationSiteImm(this.modelReachId, this.latitude, 
										this.longitude, this.actualValue, this.predictedValue, 
										this.stationId, this.stationName);
		return result;
	}

}
