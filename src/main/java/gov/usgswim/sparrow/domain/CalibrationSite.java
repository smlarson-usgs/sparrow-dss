package gov.usgswim.sparrow.domain;

public interface CalibrationSite {

	public Long getModelReachId();
	
	/**
	 * Gets the Station name of the calibration site
	 * @return
	 */
	public String getStationName();
	
	public Double getActualValue();
	
	public Double getLatitude();
	
	public Double getLongitude();
	
	public Double getPredictedValue();
	
	public String getStationId();

	public String getReachName();
	
	public String getReachId();
}
