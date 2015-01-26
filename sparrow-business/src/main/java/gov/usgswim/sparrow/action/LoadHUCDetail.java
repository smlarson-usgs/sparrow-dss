package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.Geometry;
import gov.usgswim.sparrow.domain.HUC;
import gov.usgswim.sparrow.domain.Segment;
import gov.usgswim.sparrow.request.HUCRequest;
import gov.usgswim.sparrow.util.GeometryUtil;

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
		addResultSetForAutoClose(rs);
		
		if (rs.next()) {
			
			String code = rs.getString("HUC_CODE");
			String name = rs.getString("NAME");
			Geometry simplified = GeometryUtil.loadPolygon(rs, "GEOM", true);

			huc = new HUC(code, name, req.getHucType(), null, simplified, null);

		} else {
			this.setPostMessage("No HUC found for '" + req.getHuc() + "', level " + req.getHucType().getLevel());
		}
		
		rs.close();	//Will autoclose anyway.
		
		return huc;
	}
	
}
