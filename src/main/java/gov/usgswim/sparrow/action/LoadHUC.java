package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.HUC;
import gov.usgswim.sparrow.request.HUCRequest;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 * Loads the outline geometry for a HUC from the DB. 
 * @author eeverman
 *
 */
public class LoadHUC extends Action<HUC> {

	protected HUCRequest req;
	
	public LoadHUC(HUCRequest request) {
		req = request;
	}
	

	
	public void setReq(HUCRequest req) {
		this.req = req;
	}



	@Override
	public HUC doAction() throws Exception {
		
		//The return value
		HUC huc = null;
		
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("HUC_LEVEL", req.getHucType().getLevel());
		params.put("HUC_CODE", req.getHuc());
		

		
		ResultSet rs = getROPSFromPropertiesFile("selectFull", getClass(), params).executeQuery();
		
		if (rs.next()) {

			STRUCT fullGeomStruct = (STRUCT) rs.getObject("GEOM");
			STRUCT approxGeomStruct = (STRUCT) rs.getObject("ARRPOX_GEOM");
			
			Geometry geom = null;
			String code = rs.getString("HUC_CODE");
			String name = rs.getString("NAME");
			float[] fullCoords = null;
			float[] approxCoords = null;
			
			if (fullGeomStruct != null) {
				JGeometry jGeom = JGeometry.load(fullGeomStruct);
				fullCoords = loadCoordinates(jGeom);
			}
			
			if (approxGeomStruct != null) {
				JGeometry jGeom = JGeometry.load(approxGeomStruct);
				approxCoords = loadCoordinates(jGeom);
			}
			
			geom = new Geometry(fullCoords, approxCoords, false);
			
			huc = new HUC(code, name, req.getHucType(), geom);

		} else {
			this.setPostMessage("No HUC found for '" + req.getHuc() + "', level " + req.getHucType().getLevel());
		}
		
		rs.close();	//Will autoclose anyway.
		
		return huc;
	}
	
	protected float[] loadCoordinates(JGeometry jGeom) {

		double[] dblOrds = jGeom.getOrdinatesArray();
		float[] floatOrds = new float[dblOrds.length];
		
		for (int i = 0; i<dblOrds.length; i++) {
			floatOrds[i] = (float) dblOrds[i];
		}
		
		return floatOrds;
	}


}
