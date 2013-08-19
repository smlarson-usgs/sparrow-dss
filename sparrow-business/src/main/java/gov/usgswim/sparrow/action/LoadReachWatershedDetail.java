package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.ReachGeometry;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.util.GeometryUtil;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Loads the outline geometry for a HUC from the DB. 
 * @author eeverman
 *
 */
public class LoadReachWatershedDetail extends Action<ReachGeometry> {

	protected ReachID reach;
	
	public LoadReachWatershedDetail() {
	}
	
	public LoadReachWatershedDetail(ReachID reach) {
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
		addResultSetForAutoClose(rs);
		
		if (rs.next()) {

			Geometry geom = GeometryUtil.loadPolygon(rs, "GEOM", true);
			
			upstream = new ReachGeometry(reach.getReachID(), reach.getModelID(), geom);

		} else {
			this.setPostMessage("No Reach found for '" + reach.getReachID() + "' ID in model " + reach.getModelID());
		}
		
		rs.close();	//Will autoclose anyway.
		
		return upstream;
	}

	public ReachID getReach() {
		return reach;
	}

	public void setReach(ReachID reach) {
		this.reach = reach;
	}


}
