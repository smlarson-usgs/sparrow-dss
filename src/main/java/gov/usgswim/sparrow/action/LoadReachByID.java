package gov.usgswim.sparrow.action;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import gov.usgswim.sparrow.cachefactory.ReachID;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

public class LoadReachByID extends Action<ReachInfo> {



	protected ReachID reachId;
	
	public LoadReachByID() {
		//default
	}
	
	public LoadReachByID(ReachID reachId) {
		this.reachId = reachId;
	}
	
	public ReachID getReachId() {
		return reachId;
	}

	public void setReachId(ReachID reachId) {
		this.reachId = reachId;
	}
	
	@Override
	public ReachInfo doAction() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", this.reachId.getModelID());
		params.put("Identifier", this.reachId.getReachID());
		
		ReachInfo result = null;
		
		ResultSet rs = getPSFromPropertiesFile("LoadScalerAttribs", getClass(), params).executeQuery();
		
		//Scaler attribs we need to temp store
		String reachName = null;
		String huc2 = null;
		String huc2name = null;
		String huc4 = null;
		String huc4name = null;
		String huc6 = null;
		String huc6name = null;
		String huc8 = null;
		String huc8name = null;
		
		if (rs.next()) {
			
			//Add the db ID to the params to use for the next query
			params.put("ModelReachId", rs.getString("model_reach_id"));
			
			reachName = rs.getString("reach_name");
			huc2 = rs.getString("HUC2");
			huc2name = rs.getString("HUC2_NAME");
			huc4 = rs.getString("HUC4");
			huc4name = rs.getString("HUC4_NAME");
			huc6 = rs.getString("HUC6");
			huc6name = rs.getString("HUC6_NAME");
			huc8 = rs.getString("HUC8");
			huc8name = rs.getString("HUC8_NAME");
		}
		
		rs.close();
		
		rs = getPSFromPropertiesFile("LoadSpatialAttribs", getClass(), params).executeQuery();
		
		if (rs.next()) {

			// reaches retrieved by ID should never have distanceInMeters information
			result = new ReachInfo(
					reachId.getModelID(), reachId.getReachID(), reachName, null,
					rs.getDouble("MIN_LONG"), rs.getDouble("MIN_LAT"),
					rs.getDouble("MAX_LONG"), rs.getDouble("MAX_LAT"),
					rs.getDouble("MARKER_LONG"), rs.getDouble("MARKER_LAT"),
					huc2, huc2name, huc4, huc4name,
					huc6, huc6name, huc8, huc8name
			);

			{	// set the catchment geometry
				STRUCT catch_geom_struct = (STRUCT) rs.getObject("catch_geom");
				if (catch_geom_struct != null) {
					JGeometry jGeom = JGeometry.load(catch_geom_struct);
					double[] ordinates = jGeom.getOrdinatesArray();
					result.setCatchGeometryOrdinates(ordinates);
				} else { // use the reach geometry instead
					STRUCT reach_geom_struct = (STRUCT) rs.getObject("reach_geom");
					if (reach_geom_struct != null) {
						JGeometry jGeom = JGeometry.load(reach_geom_struct);
						double[] ordinates = jGeom.getOrdinatesArray();
						result.setReachGeometryOrdinates(ordinates);
					}
				}
			}

		}
		
		rs.close();
		
		return result;
	}
//	
//	@Override
//	protected ReachInfo doAction() throws Exception {
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("ModelId", this.reachId.getModelID());
//		params.put("Identifier", this.reachId.getReachID());
//		
//		ReachInfo result = null;
//		
//		ResultSet rs = getPSFromPropertiesFile("FindReach", getClass(), params).executeQuery();
//		
//		if (rs.next()) {
//
//			// reaches retrieved by ID should never have distanceInMeters information
//			result = new ReachInfo(this.reachId.getModelID(), rs.getInt("identifier"), rs.getString("reach_name"), null,
//					rs.getDouble("MIN_LONG"), rs.getDouble("MIN_LAT"), rs.getDouble("MAX_LONG"), rs.getDouble("MAX_LAT"),
//					rs.getDouble("MARKER_LONG"), rs.getDouble("MARKER_LAT"),
//					rs.getString("HUC2"), rs.getString("HUC2NAME"), rs.getString("HUC4"), rs.getString("HUC4NAME"),
//					rs.getString("HUC6"), rs.getString("HUC6NAME"), rs.getString("HUC8"), rs.getString("HUC8NAME")
//			);
//
//			{	// set the catchment geometry
//				STRUCT catch_geom_struct = (STRUCT) rs.getObject("catch_geom");
//				if (catch_geom_struct != null) {
//					JGeometry jGeom = JGeometry.load(catch_geom_struct);
//					double[] ordinates = jGeom.getOrdinatesArray();
//					result.setCatchGeometryOrdinates(ordinates);
//				} else { // use the reach geometry instead
//					STRUCT reach_geom_struct = (STRUCT) rs.getObject("r_geom");
//					if (reach_geom_struct != null) {
//						JGeometry jGeom = JGeometry.load(reach_geom_struct);
//						double[] ordinates = jGeom.getOrdinatesArray();
//						result.setReachGeometryOrdinates(ordinates);
//					}
//				}
//			}
//
//		}
//		
//		return result;
//	}

}
