package gov.usgswim.sparrow.action;

import java.sql.ResultSet;
import java.sql.Statement;

import gov.usgswim.sparrow.domain.CalibrationSite;
import gov.usgswim.sparrow.domain.CalibrationSiteBuilder;

public class LoadCalibrationSite extends Action<CalibrationSite> {

	@Override
	public CalibrationSite doAction() throws Exception {
		CalibrationSiteBuilder result = null;
		
		String[] params = new String[] {"ReachId", ""}; //TODO
		
		String query = getTextWithParamSubstitution("SelectStationByReachId", params);
		
		Statement stmt = null;
		ResultSet rset = null;
		
		stmt = getROConnection().createStatement();
		
		rset = stmt.executeQuery(query);
		
		if (rset.next()) {
			result = new CalibrationSiteBuilder();
			result.setActualValue(rset.getDouble("ACTUAL"));
			result.setLatitude(rset.getDouble("LATITUDE"));
			result.setLongitude(rset.getDouble("LONGITUDE"));
			result.setPredictedValue(rset.getDouble("PREDICT"));
			result.setStationId(rset.getString("STATION_ID"));
			result.setStationName(rset.getString("STATION_NAME"));
			result.setModelReachId(rset.getLong("MODEL_REACH_ID"));
		}
		
		return result.toImmutable();
	}
	
}
