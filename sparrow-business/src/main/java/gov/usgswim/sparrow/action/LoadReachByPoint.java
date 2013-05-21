package gov.usgswim.sparrow.action;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;

public class LoadReachByPoint extends Action<ReachInfo>{

	protected ModelPoint request;
	private Long modelId;
	private Double lng;
	private Double lat;
	
	public LoadReachByPoint(ModelPoint request) {
		this.request = request;
		this.modelId = request.getModelID();
		this.lng = request.getPoint().x;
		this.lat = request.getPoint().y;
	}
	
	@Override
	public ReachInfo doAction() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", this.modelId);
		params.put("lat", this.lat);
		params.put("lng", this.lng);
		
		ResultSet rs = getROPSFromPropertiesFile("FindReachUsingBarryFastQuery", getClass(), params).executeQuery();
		addResultSetForAutoClose(rs);
		
		Integer reachIdentifier = null; Integer distance = null;
		if (rs.next()) {
			reachIdentifier = rs.getInt("identifier");
			distance = rs.getInt("dist_in_meters");
		}
		if (reachIdentifier != null) {
			ReachInfo reach = SharedApplication.getInstance().getReachByIDResult(new ReachID(this.modelId, reachIdentifier));
			// add the distance information to the retrieved Reach
			ReachInfo result = reach.cloneWithDistance(distance);
			result.setClickedPoint(lng, lat);
			return result;
		}
		
		return null;
	}

}
