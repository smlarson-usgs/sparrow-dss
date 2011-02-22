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
		

		
		ResultSet rs = getROPSFromPropertiesFile("select", getClass(), params).executeQuery();
		
		if (rs.next()) {

			STRUCT geom_struct = (STRUCT) rs.getObject("GEOM");
			
			Geometry geom = null;
			String code = rs.getString("HUC_CODE");
			String name = rs.getString("NAME");

			if (geom_struct != null) {
				JGeometry jGeom = JGeometry.load(geom_struct);
				double[] dblOrds = jGeom.getOrdinatesArray();
				float[] floatOrds = new float[dblOrds.length];
				
				for (int i = 0; i<dblOrds.length; i++) {
					floatOrds[i] = (float) dblOrds[i];
				}
				
				geom = new Geometry(floatOrds, false);
				
				dblOrds = null;		//eagerly destroy
			}
			
			
			huc = new HUC(code, name, req.getHucType(), geom);

		} else {
			this.setPostMessage("No HUC found for '" + req.getHuc() + "', level " + req.getHucType().getLevel());
		}
		
		rs.close();	//Will autoclose anyway.
		
		return huc;
	}


}
