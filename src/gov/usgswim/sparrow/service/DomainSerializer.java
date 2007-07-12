package gov.usgswim.sparrow.service;


import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.Source;

import java.io.OutputStream;

import java.util.List;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.time.DateFormatUtils;


public class DomainSerializer extends AbstractSerializer {
	
	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/model/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/model/v0_1 model.xsd";
	public static String T_PREFIX = "mod";

	
	public DomainSerializer() {
		super();
	}
	
	public String getTargetNamespace() {
		return TARGET_NAMESPACE;
	}
	
	
	/**
	 * Writes all the passed models to the output stream as XML.
	 * 
	 * @param stream
	 * @param models
	 * @throws XMLStreamException
	 */
	public void writeModels(OutputStream stream, List<? extends Model> models) throws XMLStreamException {
		XMLEventWriter xw = xoFact.createXMLEventWriter(stream);
		writeModels(xw, models);
	}
	
	
	/**
	 * Writes all the passed models to the XMLEventWriter.
	 * 
	 * @param xw
	 * @param models
	 * @throws XMLStreamException
	 */
	public void writeModels(XMLEventWriter xw, List<? extends Model> models) throws XMLStreamException {
	
		xw.setDefaultNamespace(TARGET_NAMESPACE);
		xw.add( evtFact.createStartDocument(ENCODING, XML_VERSION) );
		
		
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "models") );
		xw.add( evtFact.createNamespace(TARGET_NAMESPACE) );
		xw.add( evtFact.createNamespace(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE) );
		xw.add( evtFact.createAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE_LOCATION) );

		for (Model m : models) {
			writeModel(xw, m);
		}

		xw.add( evtFact.createEndElement(T_PREFIX, TARGET_NAMESPACE, "models") );
		xw.add( evtFact.createEndDocument() );
	}
	
	
	public void writeModel(javax.xml.stream.XMLEventWriter xw,
												 Model model) throws XMLStreamException {

		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "model") );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "id", model.getId().toString()) );
		
		writeElemEvent(xw, "name", model.getName());
		writeElemEventIfNotNull(xw, "description", model.getDescription());
		writeElemEventIfNotNull(xw, "url", model.getUrl());
		writeElemEvent(xw, "dateAdded", DateFormatUtils.ISO_DATE_FORMAT.format(model.getDateAdded()));
		writeElemEvent(xw, "contactId", model.getContactId().toString());
		writeElemEvent(xw, "enhNetworkId", model.getEnhNetworkId().toString());
		writeElemEvent(xw, "bounds", null,
			new String[] {"north", "west", "south", "east"},
			new String[] {model.getNorthBound().toString(), model.getWestBound().toString(), model.getSouthBound().toString(), model.getEastBound().toString()});



		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "sources") );
		for (Source s : model.getSources()) {
			writeSource(xw, s);
		}
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "sources") );
		
		
		
		
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "model") );

	}
	
	public void writeSource(XMLEventWriter xw,
													Source src) throws XMLStreamException {
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "source") );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "id", src.getId().toString()) );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "identifier", Integer.toString(src.getIdentifier())) );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "sortOrder", Integer.toString(src.getSortOrder())) );
		

		writeElemEvent(xw, "name", src.getName());
		writeElemEvent(xw, "displayName", src.getDisplayName());
		writeElemEventIfNotNull(xw, "description", src.getDescription());
		

		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "source") );

	}

}
