package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.Data2D;

import java.io.OutputStream;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

public class Data2DSerializer extends AbstractSerializer {

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/datagrid/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/datagrid/datagrid_v0_1.xsd";
	public static String T_PREFIX = "data";
	public static String ROOT_ELEM_NAME = "datagrid";
	
	private String tn;
	private String tnl;
	private String prefix;
	private String rootName;
	
	public Data2DSerializer() {
		tn = TARGET_NAMESPACE;
		tnl = TARGET_NAMESPACE_LOCATION;
		prefix = T_PREFIX;
		rootName = ROOT_ELEM_NAME;
	}
	
	public Data2DSerializer(String targetNamespace, String nsLocation, String nsPrefix, String rootElemName) {
		tn = targetNamespace;
		tnl = nsLocation;
		prefix = nsPrefix;
		rootName = rootElemName;
	}
	
	public String getTargetNamespace() {
		return tn;
	}
	
	public String getTargetNamespaceLocation() {
		return tnl;
	}
	
	public String getTargetNamespacePrefix() {
		return prefix;
	}
	
	public String getRootElementName() {
		return rootName;
	}
	
	
	public void writeResponse(OutputStream stream, Data2D result) throws XMLStreamException {
		XMLEventWriter xw = xoFact.createXMLEventWriter(stream);
		writeHeader(xw, result);
	}
	
	
	/**
	 * Writes all the passed models to the XMLEventWriter.
	 * 
	 * @param xw
	 * @param models
	 * @throws XMLStreamException
	 */
	public void writeHeader(XMLEventWriter xw, Data2D result) throws XMLStreamException {
	
		xw.setDefaultNamespace(tn);
		xw.add( evtFact.createStartDocument(ENCODING, XML_VERSION) );
		
		
		xw.add( evtFact.createStartElement(EMPTY, tn, rootName) );
		xw.add( evtFact.createNamespace(tn) );
		xw.add( evtFact.createNamespace(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE) );
		xw.add( evtFact.createAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", tnl) );

		writeResponseSection(xw, result);
		
		xw.add( evtFact.createEndElement(prefix, tn, rootName) );
		xw.add( evtFact.createEndDocument() );
	}
	
	public void writeResponseSection(javax.xml.stream.XMLEventWriter xw, Data2D result) throws XMLStreamException {

		xw.add( evtFact.createStartElement(EMPTY, tn, "response") );
		writeMetadata(xw, result);
		writeData(xw, result);
		
		xw.add( evtFact.createEndElement(EMPTY, tn, "response") );
	}
	
	public void writeMetadata(XMLEventWriter xw, Data2D result) throws XMLStreamException {
		xw.add( evtFact.createStartElement(EMPTY, tn, "metadata") );
		xw.add( evtFact.createAttribute(EMPTY, tn, "rowCount", Integer.toString(result.getRowCount())) );
		xw.add( evtFact.createAttribute(EMPTY, tn, "columnCount", Integer.toString(result.getColCount())) );
		
		xw.add( evtFact.createStartElement(EMPTY, tn, "columns") );
		String[] attribNames = new String[] {"name", "type"};
		for(String head : result.getHeadings()) {
			writeElemEvent(xw, "col", null, attribNames, new String[] {head, "Number"});
		}
		xw.add( evtFact.createEndElement(EMPTY, tn, "columns") );

		xw.add( evtFact.createEndElement(EMPTY, tn, "metadata") );

	}
	
	public void writeData(XMLEventWriter xw, Data2D result) throws XMLStreamException {
		xw.add( evtFact.createStartElement(EMPTY, tn, "data") );

			for (int r = 0; r < result.getRowCount(); r++)  {
				xw.add( evtFact.createStartElement(EMPTY, tn, "r") );
				xw.add( evtFact.createAttribute(EMPTY, tn, "id", Integer.toString(result.getIdForRow(r))) );
				for (int c = 0; c < result.getColCount(); c++)  {
					writeElemEvent(xw, "c", Double.toString(result.getDouble(r, c)));
				}
				xw.add( evtFact.createEndElement(EMPTY, tn, "r") );
			}
			
			
		xw.add( evtFact.createEndElement(EMPTY, tn, "data") );
	}
}
