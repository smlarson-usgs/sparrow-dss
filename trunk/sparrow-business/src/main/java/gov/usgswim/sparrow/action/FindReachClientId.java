package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.request.ReachClientId;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import gov.usgswim.sparrow.request.ReachID;

public class FindReachClientId extends Action<ReachClientId>{

	private ReachID reachId;
	
	public FindReachClientId(ReachID reachId) {
		this.reachId = reachId;
	}
	
	@Override
	public ReachClientId doAction() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("modelId", reachId.getModelID());
		params.put("reachId", reachId.getReachID());
		
		log.trace("Loading client reach id for model " + reachId.getModelID() +
				", reach id " + reachId.getReachID());
		
		ResultSet rs = getROPSFromPropertiesFile("findClientId", getClass(), params).executeQuery();
		addResultSetForAutoClose(rs);
		
		ReachClientId clientId = null;
		
		if (rs.next()) {
			String clientReachIdStr = rs.getString("full_identifier");
			if (clientReachIdStr != null) {
				clientId = new ReachClientId(reachId.getModelID(), clientReachIdStr);
			}
		}
		
		log.trace("Found client reach id '" + clientId.getReachClientId() +
				"' for model " + reachId.getModelID() + ", reach id " + reachId.getReachID());

		return clientId;
	}

}
