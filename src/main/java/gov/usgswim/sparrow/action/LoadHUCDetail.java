package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.HUC;
import gov.usgswim.sparrow.domain.Segment;
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
public class LoadHUCDetail extends Action<HUC> {

	protected HUCRequest req;
	
	public LoadHUCDetail(HUCRequest request) {
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
			Geometry simpleGeom = null;
			Geometry convexGeom = null;
			String code = rs.getString("HUC_CODE");
			String name = rs.getString("NAME");
			Segment[] segments = null;
			Segment[] simpleSegments = null;
			Segment convexSegment = null;
			
			if (fullGeomStruct != null) {
				JGeometry jGeomParent = JGeometry.load(fullGeomStruct);
				
				JGeometry[] jGeomSegments = jGeomParent.getElements();
				
				segments = new Segment[jGeomSegments.length];
				simpleSegments = new Segment[jGeomSegments.length];
				
				for (int i = 0; i < jGeomSegments.length; i++) {
					segments[i] = new Segment(loadCoordinates(jGeomSegments[i]), false);
					JGeometry jSimple = jGeomSegments[i].simplify(.005d);
					simpleSegments[i] = new Segment(loadCoordinates(jSimple), false);
				}
				
			}
			
			if (approxGeomStruct != null) {
				JGeometry jGeom = JGeometry.load(approxGeomStruct);
				convexSegment = new Segment(loadCoordinates(jGeom), false);
			}
			
			geom = new Geometry(segments);
			simpleGeom = new Geometry(simpleSegments);
			convexGeom = new Geometry(convexSegment);
			
			huc = new HUC(code, name, req.getHucType(), geom, simpleGeom, convexGeom);

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
