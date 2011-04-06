package gov.usgswim.sparrow.action;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import gov.usgswim.sparrow.domain.CalibrationSite;
import gov.usgswim.sparrow.domain.CalibrationSiteBuilder;

public class LoadCalibrationSite extends Action<CalibrationSite> {
	
	String query =  "SelectStationByReachId";
	Double lat;
	Double lon;
	
	public LoadCalibrationSite(Double lat, Double lon) {
		query = "SelectStationByLatLon";
		this.lat = lat;
		this.lon = lon;
	}
	
	public LoadCalibrationSite(Long reachId) {
		//TODO reach id
	}
	
	@Override
	public CalibrationSite doAction() throws Exception {
		CalibrationSiteBuilder result = null;
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("lat", lat);
		params.put("lng", lon);
		
		PreparedStatement st = getROPSFromPropertiesFile(query, getClass(), params);
		ResultSet rset = st.executeQuery();
		
		if (rset.next()) {
			result = new CalibrationSiteBuilder();
			result.setActualValue(rset.getDouble("ACTUAL"));
			result.setLatitude(rset.getDouble("LATITUDE"));
			result.setLongitude(rset.getDouble("LONGITUDE"));
			result.setPredictedValue(rset.getDouble("PREDICT"));
			result.setStationId(rset.getString("STATION_ID"));
			result.setStationName(rset.getString("STATION_NAME"));
			result.setModelReachId(rset.getLong("MODEL_REACH_ID"));
			result.setReachId(rset.getString("REACH_ID"));
			result.setReachName(rset.getString("REACH_NAME"));
		}
		
		return result.toImmutable();
	}
	
}
