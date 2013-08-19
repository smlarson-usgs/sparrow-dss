package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.request.ReachClientId;
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
		
		Long reachIdentifier = null; Integer distance = null;
		if (rs.next()) {
			reachIdentifier = rs.getLong("identifier");
			distance = rs.getInt("dist_in_meters");
		}
		if (reachIdentifier != null) {
			
			//Get the Client ID for this reach
			ReachID rid = new ReachID(this.modelId, reachIdentifier);
			FindReachClientId findReachClientId = new FindReachClientId(rid);
			ReachClientId clientRId = findReachClientId.run();
			
			if (clientRId != null) {
				ReachInfo reachInfo = SharedApplication.getInstance().getReachByIDResult(clientRId);
				// add the distance information to the retrieved Reach
				ReachInfo result = reachInfo.cloneWithDistance(distance);
				result.setClickedPoint(lng, lat);
				return result;
				
			} else {
				setPostMessage("Could not find the Client ID for the reach");
			}
		} else {
			setPostMessage("No reaches found near the specified location");
		}
		
		return null;
	}

}
