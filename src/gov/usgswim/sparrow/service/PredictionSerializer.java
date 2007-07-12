package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.service.AbstractSerializer;
import gov.usgswim.sparrow.service.PredictServiceRequest;

import java.io.OutputStream;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

public class PredictionSerializer extends AbstractSerializer {

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/prediction-response/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/prediction-response/v0_1.xsd";
	public static String T_PREFIX = "mod";
	
	public PredictionSerializer() {
		super();
	}
	
	public String getTargetNamespace() {
		return TARGET_NAMESPACE;
	}
	
	
	public void writeResponse(OutputStream stream, PredictServiceRequest request, Data2D result) throws XMLStreamException {
		XMLEventWriter xw = xoFact.createXMLEventWriter(stream);
		writeResponse(xw, request, result);
	}
	
	
	/**
	 * Writes all the passed models to the XMLEventWriter.
	 * 
	 * @param xw
	 * @param models
	 * @throws XMLStreamException
	 */
	public void writeResponse(XMLEventWriter xw, PredictServiceRequest request, Data2D result) throws XMLStreamException {
	
		xw.setDefaultNamespace(TARGET_NAMESPACE);
		xw.add( evtFact.createStartDocument(ENCODING, XML_VERSION) );
		
		
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "sparrow-prediction-response") );
		xw.add( evtFact.createNamespace(TARGET_NAMESPACE) );
		xw.add( evtFact.createNamespace(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE) );
		xw.add( evtFact.createAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE_LOCATION) );

		writeRequest(xw, request);
		writeResponse(xw, result);
		
		xw.add( evtFact.createEndElement(T_PREFIX, TARGET_NAMESPACE, "sparrow-prediction-response") );
		xw.add( evtFact.createEndDocument() );
	}
	
	public void writeRequest(XMLEventWriter xw, PredictServiceRequest request) {
		//for now, just skip this optional element
	}
	
	public void writeResponse(javax.xml.stream.XMLEventWriter xw, Data2D result) throws XMLStreamException {

		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "response") );
		writeMetadata(xw, result);
		writeData(xw, result);

		
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "response") );

	}
	
	public void writeMetadata(XMLEventWriter xw, Data2D result) throws XMLStreamException {
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "metadata") );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "rowCount", Integer.toString(result.getRowCount())) );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "columnCount", Integer.toString(result.getColCount())) );
		
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "columns") );
		String[] attribNames = new String[] {"name", "type"};
		for(String head : result.getHeadings()) {
			writeElemEvent(xw, "col", null, attribNames, new String[] {head, "Number"});
		}
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "columns") );

		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "metadata") );

	}
	
	public void writeData(XMLEventWriter xw, Data2D result) throws XMLStreamException {
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "data") );

			for (int r = 0; r < result.getRowCount(); r++)  {
				xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "r") );
				for (int c = 0; c < result.getColCount(); c++)  {
					writeElemEvent(xw, "c", Double.toString(result.getDouble(r, c)));
				}
				xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "r") );
			}
			
			
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "data") );
	}
}
