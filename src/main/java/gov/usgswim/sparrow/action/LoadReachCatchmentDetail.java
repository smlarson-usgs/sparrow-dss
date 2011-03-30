package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.ReachGeometry;
import gov.usgswim.sparrow.domain.Segment;
import gov.usgswim.sparrow.request.ReachID;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

/**
 * Loads the outline geometry for the catchment of a single reach from the DB.
 * 
 * This action is not wired up to the cache and isn't being used at the moment.
 * It could be a replacement for how we load geometry for the ReachID operation,
 * and at the time it was created, is a good way to look at the individual geometry
 * we have (see the related test).
 * 
 * @author eeverman
 *
 */
public class LoadReachCatchmentDetail extends Action<ReachGeometry> {

	protected ReachID reach;
	
	public LoadReachCatchmentDetail() {
	}
	
	public LoadReachCatchmentDetail(ReachID reach) {
		this.reach = reach;
	}



	@Override
	public ReachGeometry doAction() throws Exception {
		
		//The return value
		ReachGeometry upstream = null;
		
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("MODEL_ID", reach.getModelID());
		params.put("REACH_ID", reach.getReachID());
		
		ResultSet rs = getROPSFromPropertiesFile("selectFull", getClass(), params).executeQuery();
		
		if (rs.next()) {

			STRUCT fullGeomStruct = (STRUCT) rs.getObject("GEOM");
			STRUCT approxGeomStruct = (STRUCT) rs.getObject("ARRPOX_GEOM");
			
			Geometry geom = null;
			Geometry simpleGeom = null;
			Geometry convexGeom = null;
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
			
			upstream = new ReachGeometry(reach.getReachID(), reach.getModelID(), geom, simpleGeom, convexGeom);

		} else {
			this.setPostMessage("No Reach found for '" + reach.getReachID() + "' ID in model " + reach.getModelID());
		}
		
		rs.close();	//Will autoclose anyway.
		
		return upstream;
	}
	
	protected float[] loadCoordinates(JGeometry jGeom) {

		double[] dblOrds = jGeom.getOrdinatesArray();
		float[] floatOrds = new float[dblOrds.length];
		
		for (int i = 0; i<dblOrds.length; i++) {
			floatOrds[i] = (float) dblOrds[i];
		}
		
		return floatOrds;
	}

	public ReachID getReach() {
		return reach;
	}

	public void setReach(ReachID reach) {
		this.reach = reach;
	}


}
