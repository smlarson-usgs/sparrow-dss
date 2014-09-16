package gov.usgswim.sparrow.action;

import gov.usgs.cida.sparrow.service.util.GeoServerConnection;
import gov.usgs.cida.sparrow.service.util.GeoServerConnection.GeoServerResponse;
import gov.usgswim.sparrow.parser.gml.ReachPointParser;
import gov.usgswim.sparrow.parser.gml.ReachPointParser.ReachPointInfo;
import gov.usgswim.sparrow.request.ReachClientId;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;
import gov.usgswim.sparrow.service.idbypoint.ReachInfo;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class LoadReachByPoint extends Action<ReachInfo>{
	private static final String LAYER_TOKEN = "_LAYERNAME_";
	private static final String LONGITUDE_TOKEN = "_LONGITUDE_";
	private static final String LATITUDE_TOKEN = "_LATITUDE_";
	
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
		
		/**
		 * JIRA SPDSS-1301
		 * 
		 * 		Need to replace the following ResultSet logic with a call to 
		 * 		GeoServer for a featureID that is close to this point.
		 * 
		 * 		We first need to get the layer name for this model
		 */
		String layerName = SparrowResourceUtils.lookupLayerNameForModelID(this.modelId);
		if(layerName == null) {
			throw new Exception("Layer for model id [" + this.modelId + "] is null");
		}
		layerName = "reach-overlay:" + layerName;
		
		ReachPointInfo reachResult = getReachPointFromGeoServer(layerName);
				
		if (reachResult != null) {
			
			//Get the Client ID for this reach
			ReachID rid = new ReachID(this.modelId, reachResult.getReachIdentifier());
			FindReachClientId findReachClientId = new FindReachClientId(rid);
			ReachClientId clientRId = findReachClientId.run();
			
			if (clientRId != null) {
				ReachInfo reachInfo = SharedApplication.getInstance().getReachByIDResult(clientRId);
				// add the distance information to the retrieved Reach
				ReachInfo result = reachInfo.cloneWithDistance((int)reachResult.getDistance());
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
	
	private ReachPointInfo getReachPointFromGeoServer(String layername) throws Exception {
		ReachPointInfo reachResult = null;
		
		/** 
		 * 		Now we need to do a GeoServer call in order to get the information
		 * 		we need about a "point".  We will do a WPS Execute POST utilizing
		 * 		GeoServer's internal "nearest" calculation.  In order to do so we
		 * 		need to post a WFS GML document that describes the layer and the
		 * 		geolocation for our current model.
		 * 
		 * 		Our POST data is in a GML/XML file of the same name as this class
		 * 		and we must replace the tokens in the file w/ our layername and
		 * 		our lat,long values.
		 */
		String postData = null;
		try {
			postData = createPostData(layername);
		} catch (Exception e) {
			throw new Exception("Unable to build GML POST data for model id [" + this.modelId + "].  Exception: " + e.getMessage());
		}
		
		/**
		 * WPS call looks like:
		 * 
		 * 		http://cida-eros-sparrowdev.er.usgs.gov:8081/geoserver/wps
		 */
		String wpsCall = "wps";
		
		GeoServerResponse response = null;
		try {
			response = GeoServerConnection.getInstance().doPostRequest(wpsCall, postData.getBytes(), "text/xml");
		
			if(response == null) {
				throw new Exception("Unable to retrieve feature id from GeoServer for model [" + this.modelId + "].");
			}
	
			/**
			 * Since the GeoServerConnection class returns an object that represents
			 * the raw response from GeoServer we will need to parse the contents
			 * ourselves based on the information inside the GeoServerReponse object.
			 * 
			 * We are expecting XML so that is what we will parse for.
			 */
			Map<String, String> contentTypes = response.getContentTypes();
			boolean xmlContent = false;
			
			// Loop through all values and see if we find an xml content type
			for(String value : contentTypes.values()) {
				if(value.contains("xml")) {
					xmlContent = true;
					break;
				}
			}
			
			if(!xmlContent) {
				throw new Exception("Response from GeoServer for model [" + this.modelId + "] does not contain valid XML.  Unable to parse.");
			}
			
			reachResult = getReachInfoFromGeoServerResponse(response);
			
			/**
			 * Remove the temporary file now that we no longer need it.
			 */
		} finally {
			Path tempPath = Paths.get(response.getFilename());
			Files.deleteIfExists(tempPath);
		}
		
		return reachResult;
	}
	
	private String createPostData(String layername) throws IOException {
		String postData = new String(SparrowResourceUtils.getFileDataByClass(getClass(), ".xml"));
		
		postData = postData.replaceAll(LoadReachByPoint.LAYER_TOKEN, layername);
		postData = postData.replaceAll(LoadReachByPoint.LATITUDE_TOKEN, this.lat + "");
		postData = postData.replaceAll(LoadReachByPoint.LONGITUDE_TOKEN, this.lng + "");
		
		return postData;
	}
	
	private ReachPointInfo getReachInfoFromGeoServerResponse(GeoServerResponse geoResponse) throws Exception  {
		ReachPointParser reachPointParser;
		try {
			reachPointParser = new ReachPointParser(this.lat, this.lng);
		} catch (Exception e) {
			throw new Exception("Unable to create ReachPointParser for model [" + this.modelId + "]" + e.getMessage());
		}
		
		ReachPointInfo info;
		try {
			info = reachPointParser.parseReachPointSource(geoResponse.getFilename());
		} catch (Exception e) {
			throw new Exception("Unable to parse GeoServer response for model [" + this.modelId + "]" + e.getMessage());
		}
		
		return info;
	}
}
