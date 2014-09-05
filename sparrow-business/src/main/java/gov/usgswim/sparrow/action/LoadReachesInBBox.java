package gov.usgswim.sparrow.action;

import gov.usgs.cida.sparrow.service.util.GeoServerConnection;
import gov.usgs.cida.sparrow.service.util.GeoServerConnection.GeoServerResponse;
import gov.usgswim.sparrow.domain.ModelBBox;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Loads all the reach ids for a bounding box for a given model.
 * The returned array are sorted by the identifier.
 * 
 * @author eeverman
 *
 */
public class LoadReachesInBBox extends Action<Long[]> {
	protected static Logger log =
			Logger.getLogger(LoadReachesInBBox.class); //logging for this class
		
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
		/**
		 * For JIRA SPDSS-1278 I was originally going to use the LoadReachesInBBox.properties pattern
		 * that was used prior when building the SQL needed classes.  Unfortunately the calls that
		 * offer this "properties file -> string replacement" functionality is a coded with SQL
		 * classes in mind (PreparedStatement, SQLString, etc).  Due to this and the fact that this
		 * GeoServer request will not change I decided to make it a hardcoded value here.
		 * 
		 * There also isn't a lot of time to get this specific JIRA done so if this is an important
		 * pattern that we now want to offer for GeoServer requests we should add this functionality
		 * in a later JIRA.
		 * 
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("LayerName", layerName);
				params.put("leftLong", modelBBox.getLeftLongBound());
				params.put("rightLong", modelBBox.getRightLongBound());
				params.put("upperLat", modelBBox.getUpperLatBound());
				params.put("lowerLat", modelBBox.getLowerLatBound());
				String request = getROPSFromPropertiesFile(GEOSERVER_QUERY, this.getClass(), params);
		 *
		 *
		 */
		String request = "wfs?service=wfs&version=1.0.0&request=GetFeature&typeNames=" + layerName + "&srsName=EPSG:4326&bbox=" +
						 leftLong + "," + lowerLat + "," + rightLong + "," + upperLat + "&propertyname=reach-overlay:IDENTIFIER";
		
		log.info("PERFORMING GEOSERVER REQUEST WITH: [" + request + "]");
		
		GeoServerResponse response = GeoServerConnection.getInstance().doRequest(request);
		
		if(response == null) {
			log.error("Unable to retrieve feature ids from GeoServer for model [" + modelBBox.getModelId() + "].  Returning an emtpy list of ids...");
			return new Long[]{};
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
			log.error("Response from GeoServer for model [" + modelBBox.getModelId() + "] does not contain valid GML.  Unable to parse.  Returning an emtpy list of ids...");
			return new Long[]{};
		}
		
		Long[] results = convertStringListToLongArray(parseGMLResultsForFeatureIDs(response.getFilename()));
		
		/**
		 * Remove the temporary file now that we no longer need it.
		 */
		Path tempPath = Paths.get(response.getFilename());
		Files.deleteIfExists(tempPath);
		
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
	
	private Long[] convertStringListToLongArray(List<String> list) {
		List<Long> results = new ArrayList<Long>();
		
		for(String item : list) {
			Long value = 0L;
			
			try {
				results.add(Long.parseLong(item));				
			} catch (Exception e) {
				log.error("Unable to convert feature id [" + item + "] to long value.  Skipping id...");
			}
		}
		
		return results.toArray(new Long[results.size()]);
	}
	
	
	private List<String> parseGMLResultsForFeatureIDs(String file) {
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
		ReachOverlayIdentifierParser roiParser;
		try {
			roiParser = new ReachOverlayIdentifierParser();
		} catch (Exception e) {
			log.error("Unable to create GML SAX parser for model [" + modelBBox.getModelId() + "].  Exception: [" + e.getMessage() + "]");
			return results;
		} 
		
		try {
			results.addAll(roiParser.parseReachOverlayIdentifierSource(file));
		} catch (SAXException e) {
			log.error("Unable to parse GeoServer response for [" + modelBBox.getModelId() + "].  SAXException: [" + e.getMessage() + "]");
			e.printStackTrace();
		} catch (IOException e) {
			log.error("Unable to parse GeoServer response for [" + modelBBox.getModelId() + "].  IOException: [" + e.getMessage() + "]");
			e.printStackTrace();
		}		
		
		return results;
	}

	/**
	 * ReachOverlayIdentifierParser
	 *<br /><br />
	 *	This class implements a SAX Parsing strategy for parsing all Reach Overlay IDENTIFIERS
	 *	result sets.
	 */
	private class ReachOverlayIdentifierParser {
		private SAXParser saxParser;
		private ReachOverlayIdentifierHandler idHander;
		List<String> identifiers;
		
		public ReachOverlayIdentifierParser() throws ParserConfigurationException, SAXException {
			this.saxParser = new SAXParser();
			this.identifiers = new ArrayList<String>();
			this.idHander = new ReachOverlayIdentifierHandler(this.identifiers);
		}
		
		public List<String> parseReachOverlayIdentifierSource(String file) throws SAXException, IOException {
			this.saxParser.setContentHandler(this.idHander);
			this.saxParser.parse(file);
			
			return this.identifiers;
		}
	}

	/**
	 * ReachOverlayIdentifierHandler parses the GeoServer WFS GML format for <reach-overlay:IDENTIFIER> elements.
	 * <br /><br />
	 * 
	 * GML Response from GeoServer looks like:
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
	private class ReachOverlayIdentifierHandler extends DefaultHandler {
		private List<String> identifiers;
		
		private static final String ID_ELEMENT = "reach-overlay:IDENTIFIER";
		private CharArrayWriter contents = new CharArrayWriter();
		
		public ReachOverlayIdentifierHandler(List<String> idList) {
			this.identifiers = idList;
		}
		
		public void startDocument() throws SAXException {
			//String msg = "========== ReachOverlayIdentifierHandler.startDocument() ==========";
			//System.out.println(msg);
			//log.debug(msg);
		}
		
		public void endDocument() throws SAXException {
			//String msg = "========== ReachOverlayIdentifierHandler.endDocument() ==========";
			//System.out.println(msg);
			//log.debug(msg);
		}
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			//String msg = "========== ReachOverlayIdentifierHandler.startElement() [" + qName + "] ==========";
			//System.out.println(msg);
			//log.debug(msg);
			contents.reset();
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException {
			//String msg = "========== ReachOverlayIdentifierHandler.endElement() [" + qName + "] ==========";
			//System.out.println(msg);
			//log.debug(msg);
			
			/**
			 * IDENTIFIER name element
			 */
			if(ReachOverlayIdentifierHandler.ID_ELEMENT.equals(qName)) {
				String stringValue = contents.toString();
				
				if((stringValue != null) && (!stringValue.isEmpty())) {
					this.identifiers.add(stringValue);
				}
			}
		}
		
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write(ch, start, length);
		}
	}

}
