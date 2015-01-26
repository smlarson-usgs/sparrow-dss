package gov.usgswim.sparrow.action;

import gov.usgs.cida.sparrow.service.util.GeoServerConnection;
import gov.usgs.cida.sparrow.service.util.GeoServerConnection.GeoServerResponse;
import gov.usgswim.sparrow.domain.ModelBBox;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.parser.gml.ReachOverlayIdentifierParser;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


/**
 * Loads all the reach ids for a bounding box for a given model.
 * The returned array are sorted by the identifier.
 * 
 * @author eeverman
 *
 */
public class LoadReachesInBBox extends Action<Long[]> {		
	/** Name of the GeoServer query in the classname matched properties file */
	public static final String GEOSERVER_QUERY = "getfeature";
		
	protected ModelBBox modelBBox;
	
	public LoadReachesInBBox() {
		super();
	}


	@Override
	public Long[] doAction() throws Exception {
		/**
		 * Call GeoServer to retrieve a list of Feature IDs within a bounding box
		 * for the layer associated with the model.
		 */
		ModelRequestCacheKey key = new ModelRequestCacheKey(modelBBox.getModelId(), false, false, false);
		List<SparrowModel> modelMetaDataList = SharedApplication.getInstance().getModelMetadata(key);
		
		SparrowModel sparrowModel = null;
		if(modelMetaDataList.size() == 1) {
			sparrowModel = modelMetaDataList.get(0);
		} else {
			if(modelMetaDataList.size() == 0) {
				log.error("Unable to retrieve SparrowModel from SharedApplication for model id [" + modelBBox.getModelId() + "].  Returning an emtpy list of ids...");
				return new Long[]{};
			} else {
				log.error("When retrieving the SparrowModel from SharedApplication for model id [" + modelBBox.getModelId() + "], more than " +
						  "model was returned (total of [" + modelMetaDataList.size() + "] returned).  Using first model returned for GeoServer GetFeature query.");
				sparrowModel = modelMetaDataList.get(0);
			}
		}
		
		String layerName = "reach-overlay:" + sparrowModel.getThemeName();		
		double leftLong = modelBBox.getLeftLongBound();
		double rightLong = modelBBox.getRightLongBound();
		double upperLat = modelBBox.getUpperLatBound();
		double lowerLat = modelBBox.getLowerLatBound();
		
		//In form of:
		//		wfs?service=wfs&version=1.0.0&request=GetFeature&typeNames=@LayerName@&srsName=EPSG:4326&bbox=$leftLong$, $lowerLat$, $rightLong$, $upperLat$&propertyname=reach-overlay:IDENTIFIER
		String request = "wfs?service=wfs&version=1.0.0&request=GetFeature&typeNames=" + layerName + "&srsName=EPSG:4326&bbox=" +
						 leftLong + "," + lowerLat + "," + rightLong + "," + upperLat + "&propertyname=reach-overlay:IDENTIFIER";
		
		log.info("PERFORMING GEOSERVER REQUEST WITH: [" + request + "]");
		
		GeoServerResponse response = null;
		Long[] results = null;
		
		try {
			response = GeoServerConnection.getInstance().doGetRequest(request);
		
			if(response == null) {
				throw new Exception("Unable to retrieve feature ids from GeoServer for model [" + modelBBox.getModelId() + "].");
			}
	
			/**
			 * Since the GeoServerConnection class returns an object that represents
			 * the raw response from GeoServer we will need to parse the contents
			 * ourselves based on the information inside the GeoServerReponse object.
			 * 
			 * We are expecting XML/GML so that is what we will parse for.
			 */
			Map<String, String> contentTypes = response.getContentTypes();
			boolean gmlContent = false;
			if(contentTypes.keySet().contains("subtype")) {
				String subType = contentTypes.get("subtype");
				if(subType.contains("gml")) {
					gmlContent = true;
				}
			} else {
				// Loop through all values and see if we find a gml content type
				for(String value : contentTypes.values()) {
					if(value.contains("gml")) {
						gmlContent = true;
						break;
					}
				}
			}
			
			if(!gmlContent) {
				throw new Exception("Response from GeoServer for model [" + modelBBox.getModelId() + "] does not contain valid GML.  Unable to parse.");
			}
			
			results = convertStringListToLongArray(parseGMLResultsForFeatureIDs(response.getFilename()));
			
			/**
			 * Remove the temporary file now that we no longer need it.
			 */
		} finally {
			Path tempPath = Paths.get(response.getFilename());
			Files.deleteIfExists(tempPath);
		}
		
		log.info("Number of FeatureIDs returned from GeoServer are :[" + results.length + "]");
		
		return results;
	}


	/**
	 * @return the modelBBox
	 */
	public ModelBBox getModelBBox() {
		return modelBBox;
	}


	/**
	 * @param modelBBox the modelBBox to set
	 */
	public void setModelBBox(ModelBBox modelBBox) {
		this.modelBBox = modelBBox;
	}
	
	private Long[] convertStringListToLongArray(List<String> list) throws Exception {
		List<Long> results = new ArrayList<Long>();
		
		for(String item : list) {
			results.add(Long.parseLong(item));	
		}
		
		return results.toArray(new Long[results.size()]);
	}
	
	
	private List<String> parseGMLResultsForFeatureIDs(String file) throws SAXException, IOException, ParserConfigurationException {
		List<String> results = new ArrayList<String>();
		
		/**
		 * GML Reponse from GeoServer looks like:
		 * 
		 * 		<?xml version="1.0" encoding="UTF-8"?>
		 * 		<wfs:FeatureCollection xmlns="http://www.opengis.net/wfs" xmlns:wfs="http://www.opengis.net/wfs" xmlns:reach-overlay="http://water.usgs.gov/nawqa/sparrow/dss/spatial/reach-overlay" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://water.usgs.gov/nawqa/sparrow/dss/spatial/reach-overlay http://cida-eros-sparrowdev.er.usgs.gov:8081/geoserver/wfs?service=WFS&version=1.0.0&request=DescribeFeatureType&typeName=reach-overlay%3Amrb06_mrbe2rf1 http://www.opengis.net/wfs http://cida-eros-sparrowdev.er.usgs.gov:8081/geoserver/schemas/wfs/1.0.0/WFS-basic.xsd">
		 * 			<gml:boundedBy>
		 * 				<gml:null>unknown</gml:null>
		 * 			</gml:boundedBy>
		 * 			<gml:featureMember>
		 * 				<reach-overlay:mrb06_mrbe2rf1 fid="mrb06_mrbe2rf1.3951">
		 * 					<reach-overlay:IDENTIFIER>45857</reach-overlay:IDENTIFIER>
		 * 				</reach-overlay:mrb06_mrbe2rf1>
		 * 			</gml:featureMember>
		 * 			<gml:featureMember>
		 * 				<reach-overlay:mrb06_mrbe2rf1 fid="mrb06_mrbe2rf1.3952">
		 * 					<reach-overlay:IDENTIFIER>45860</reach-overlay:IDENTIFIER>
		 * 				</reach-overlay:mrb06_mrbe2rf1>
		 * 			</gml:featureMember>
		 * 		</wfs:FeatureCollection>
		 * 
		 * 	All we care about in this XML is the value in <reach-overlay:IDENTIFIER>######</reach-overlay:IDENTIFIER>
		 */
		ReachOverlayIdentifierParser roiParser = new ReachOverlayIdentifierParser();
		results.addAll(roiParser.parseReachOverlayIdentifierSource(file));
		
		return results;
	}
	
	@Override
	public Long getModelId() {
		if (modelBBox != null) {
			return modelBBox.getModelId();
		} else {
			return null;
		}
	}
}
