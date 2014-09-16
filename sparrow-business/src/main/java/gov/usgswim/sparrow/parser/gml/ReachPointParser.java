package gov.usgswim.sparrow.parser.gml;

import java.io.CharArrayWriter;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ReachPointParser {
	private SAXParser saxParser;
	private ReachPointHandler idHander;
	ReachPointInfo reachPoint;
	
	public ReachPointParser(double originalLatitude, double originalLongitude) throws ParserConfigurationException, SAXException {
		this.saxParser = new SAXParser();
		this.reachPoint = new ReachPointInfo(originalLatitude, originalLongitude);
		
		this.idHander = new ReachPointHandler(this.reachPoint);
	}
	
	public ReachPointInfo parseReachPointSource(String file) throws SAXException, IOException {
		this.saxParser.setContentHandler(this.idHander);
		this.saxParser.parse(file);
		
		return this.reachPoint;
	}
	
	/**
	 * ReachPointHandler parses the GeoServer WFS XML format for <reach-overlay:IDENTIFIER> elements.
	 * <br /><br />
	 * 
	 * XML Response from GeoServer looks like:
	 * 
	 * 		<?xml version="1.0" encoding="UTF-8"?>
	 * 		<wfs:FeatureCollection xmlns:wfs="http://www.opengis.net/wfs"
	 * 			xmlns:feature="http://water.usgs.gov/nawqa/sparrow/dss/spatial/reach-overlay"
	 * 			xmlns:ogc="http://www.opengis.net/ogc" xmlns:gml="http://www.opengis.net/gml">
	 * 			<gml:boundedBy>
	 * 				<gml:Box srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
	 * 					<gml:coord>
	 * 						<gml:X>-82.080513</gml:X>
	 * 						<gml:Y>26.684446</gml:Y>
	 * 					</gml:coord>
	 * 					<gml:coord>
	 * 						<gml:X>-82.063721</gml:X>
	 * 						<gml:Y>26.754545</gml:Y>
	 * 					</gml:coord>
	 * 				</gml:Box>
	 * 			</gml:boundedBy>
	 * 			<gml:featureMember>
	 * 				<feature:mrb02_mrbe2rf1 fid="mrb02_mrbe2rf1.1552">
	 * 					<gml:boundedBy>
	 * 						<gml:Box srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
	 * 							<gml:coord>
	 * 								<gml:X>-82.080513</gml:X>
	 * 								<gml:Y>26.684446</gml:Y>
	 * 							</gml:coord>
	 * 							<gml:coord>
	 * 								<gml:X>-82.063721</gml:X>
	 * 								<gml:Y>26.754545</gml:Y>
	 * 							</gml:coord>
	 * 						</gml:Box>
	 * 					</gml:boundedBy>
	 * 					<feature:the_geom>
	 * 						<gml:MultiLineString srsName="http://www.opengis.net/gml/srs/epsg.xml#4326">
	 * 							<gml:lineStringMember>
	 * 								<gml:LineString>
	 * 									<gml:coordinates>-82.063721,26.754545 -82.063911,26.753155
	 * 										-82.06411,26.751751 -82.066002,26.750546 -82.066307,26.74905
	 * 										-82.066505,26.747646 -82.065208,26.745953 -82.065407,26.744442
	 * 										-82.067314,26.743252 -82.067513,26.741848 -82.067711,26.740353
	 * 										-82.068016,26.738949 -82.066612,26.73724 -82.066902,26.735851
	 * 										-82.06881,26.734646 -82.069008,26.73315 -82.070808,26.731945
	 * 										-82.071114,26.730541 -82.071312,26.729046 -82.071602,26.727642
	 * 										-82.071815,26.726147 -82.072105,26.724743 -82.072304,26.723354
	 * 										-82.072609,26.721844 -82.074409,26.720654 -82.073112,26.718945
	 * 										-82.073311,26.717541 -82.075203,26.716351 -82.077003,26.715145
	 * 										-82.078606,26.715343 -82.080513,26.714153 -82.079109,26.712446
	 * 										-82.079414,26.711042 -82.078011,26.709349 -82.078316,26.707945
	 * 										-82.076912,26.706251 -82.077202,26.704847 -82.077415,26.703352
	 * 										-82.077705,26.701948 -82.077904,26.700453 -82.078102,26.699049
	 * 										-82.076805,26.697355 -82.07711,26.695951 -82.075706,26.694242
	 * 										-82.077621,26.693052 -82.07782,26.691648 -82.076416,26.689955
	 * 										-82.076721,26.688551 -82.078522,26.687346 -82.078812,26.68585
	 * 										-82.079025,26.684446</gml:coordinates>
	 * 								</gml:LineString>
	 * 							</gml:lineStringMember>
	 * 						</gml:MultiLineString>
	 * 					</feature:the_geom>
	 * 					<feature:IDENTIFIER>81131</feature:IDENTIFIER>
	 * 					<feature:SOURCE>enhanced</feature:SOURCE>
	 * 					<feature:nearest_distance>8187.040895378429</feature:nearest_distance>
	 * 					<feature:nearest_bearing>180.9722383124635</feature:nearest_bearing>
	 * 				</feature:mrb02_mrbe2rf1>
	 * 			</gml:featureMember>
	 * 		</wfs:FeatureCollection>
	 * 
	 * 	All we care about in this XML are the values:
	 * 			<feature:IDENTIFIER>######</feature:IDENTIFIER>
	 * 			<feature:nearest_distance>8187.040895378429</feature:nearest_distance>
	 * 			
	 */
	private class ReachPointHandler extends DefaultHandler {
		private ReachPointInfo reachPoint;
		
		private static final String ID_ELEMENT = "feature:IDENTIFIER";
		private static final String DISTANCE_ELEMENT = "feature:nearest_distance";
		private CharArrayWriter contents = new CharArrayWriter();
		
		public ReachPointHandler(ReachPointInfo reachPoint) {
			this.reachPoint = reachPoint;
		}
				
		public void startDocument() throws SAXException {
			//String msg = "========== ReachPointHandler.startDocument() ==========";
			//System.out.println(msg);
			//log.debug(msg);
		}
		
		public void endDocument() throws SAXException {
			//String msg = "========== ReachPointHandler.endDocument() ==========";
			//System.out.println(msg);
			//log.debug(msg);
		}
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			//String msg = "========== ReachPointHandler.startElement() [" + qName + "] ==========";
			//System.out.println(msg);
			//log.debug(msg);
			contents.reset();
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException {
			//String msg = "========== ReachPointHandler.endElement() [" + qName + "] ==========";
			//System.out.println(msg);
			//log.debug(msg);
			
			/**
			 * IDENTIFIER name element
			 */
			if(ReachPointHandler.ID_ELEMENT.equals(qName)) {
				String stringValue = contents.toString();
				
				if((stringValue != null) && (!stringValue.isEmpty())) {
					this.reachPoint.setReachIdentifier(stringValue);
				}
			}
			
			/**
			 * DISTANCE name element
			 */
			if(ReachPointHandler.DISTANCE_ELEMENT.equals(qName)) {
				String stringValue = contents.toString();
				
				if((stringValue != null) && (!stringValue.isEmpty())) {
					this.reachPoint.setDistance(stringValue);
				}
			}
		}
		
		public void characters(char[] ch, int start, int length) throws SAXException {
			contents.write(ch, start, length);
		}	
	}
	
	/**
	 * Internal class to encapsulate the information gathered for a read identifier
	 * based on a lat/lng
	 *
	 */
	public class ReachPointInfo {
		private double originLongitude;
		private double originLatitude;
		private long reachIdentifier;
		private double distance;
		
		public ReachPointInfo(double lat, double lng) {
			this.originLatitude = lat;
			this.originLongitude = lng;
		}
		
		public ReachPointInfo(double lat, double lng, long id, int distance) {
			this.originLatitude = lat;
			this.originLongitude = lng;
			this.reachIdentifier = id;
			this.distance = distance;
		}

		public long getReachIdentifier() {
			return reachIdentifier;
		}

		public void setReachIdentifier(long reachIdentifier) {
			this.reachIdentifier = reachIdentifier;
		}
		
		public void setReachIdentifier(String reachIdentifier) {
			this.reachIdentifier = Long.parseLong(reachIdentifier);
		}

		public double getDistance() {
			return distance;
		}

		public void setDistance(double distance) {
			this.distance = distance;
		}
		
		public void setDistance(String distance) {
			this.distance = Double.parseDouble(distance);
		}

		public double getOriginLongitude() {
			return originLongitude;
		}

		public double getOriginLatitude() {
			return originLatitude;
		}
		
		@Override
		public String toString() {
			StringBuffer result = new StringBuffer();
			
			result.append("ReachPointInfo: [\n");
			result.append("\tOriginal Latitude: \t" + this.originLatitude + "\n");
			result.append("\tOriginal Longitude:\t" + this.originLongitude + "\n");
			result.append("\tClosest ReachID:   \t" + this.reachIdentifier + "\n");
			result.append("\tDistance to Origin:\t" + this.distance + "\n");
			result.append("\n]\n");
			
			return result.toString();
		}
	}
}
