package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.ReachGeometry;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.util.GeometryUtil;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

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
		ReachGeometry catchment = null;
		
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("MODEL_ID", reach.getModelID());
		params.put("REACH_ID", reach.getReachID());
		
		ResultSet rs = getROPSFromPropertiesFile("selectFull", getClass(), params).executeQuery();
		
		if (rs.next()) {

			//OK to load full detail for a single catchment
			Geometry geom = GeometryUtil.loadPolygon(rs, "GEOM", false);
			
			catchment = new ReachGeometry(reach.getReachID(), reach.getModelID(), geom);


		} else {
			this.setPostMessage("No Reach found for '" + reach.getReachID() + "' ID in model " + reach.getModelID());
		}
		
		rs.close();	//Will autoclose anyway.
		
		return catchment;
	}

	public ReachID getReach() {
		return reach;
	}

	public void setReach(ReachID reach) {
		this.reach = reach;
	}


}
