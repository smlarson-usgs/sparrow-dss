package gov.usgswim.sparrow.parser.gml;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * ReachOverlayIdentifierParser
 *<br /><br />
 *	This class implements a SAX Parsing strategy for parsing all Reach Overlay IDENTIFIERS
 *	result sets.
 */
public class ReachOverlayIdentifierParser {
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

