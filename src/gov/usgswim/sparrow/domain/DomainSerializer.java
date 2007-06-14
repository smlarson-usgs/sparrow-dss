package gov.usgswim.sparrow.domain;


import com.ctc.wstx.stax.WstxEventFactory;

import com.ctc.wstx.stax.WstxOutputFactory;

import java.io.OutputStream;

import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import javax.xml.transform.Result;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.StringUtils;

public class DomainSerializer {
	public static String EMPTY = StringUtils.EMPTY;
	public static String ENCODING = "ISO-8859-1";
	public static String XML_VERSION = "1.0";
	
	
	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/model/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/model/v0_1 model.xsd";
	public static String T_PREFIX = "mod";
	
	public static String XMLSCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	public static String XMLSCHEMA_PREFIX = "xsi";
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	protected static XMLEventFactory evtFact;
	protected static XMLOutputFactory xoFact;
	
	public DomainSerializer() {
	
		synchronized (factoryLock) {
			if (evtFact == null) {
				evtFact = WstxEventFactory.newInstance();
				xoFact = WstxOutputFactory.newInstance();
			}
		}
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
	 * Writes all the passed models to the XMLStreamWriter.
	 * @param xw
	 * @param models
	 * @throws XMLStreamException
	 */
	public void writeModels(XMLStreamWriter xw, List<? extends Model> models) throws XMLStreamException {
		xoFact.createXMLEventWriter((Result) xw);
		writeModels(xoFact.createXMLEventWriter((Result) xw), models);
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
	
	
	public void writeModel(javax.xml.stream.XMLEventWriter xw, Model model) throws XMLStreamException {

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
	
	public void writeSource(XMLEventWriter xw, Source src) throws XMLStreamException {
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, "source") );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "id", src.getId().toString()) );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "identifier", Integer.toString(src.getIdentifier())) );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, "sortOrder", Integer.toString(src.getSortOrder())) );
		

		writeElemEvent(xw, "name", src.getName());
		writeElemEvent(xw, "displayName", src.getDisplayName());
		writeElemEventIfNotNull(xw, "description", src.getDescription());
		

		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, "source") );

	}
	
	/**
	 * Creates events for a single Element with text content and adds them to
	 * the event stream.  The passed value is trimmed to null and if null after
	 * trim, no text content is written.
	 * 
	 * The events are added using an empty prefix and using the default namespace.
	 * 
	 * @param xw
	 * @param name
	 * @param value
	 * @throws XMLStreamException
	 */
	protected void writeElemEvent(XMLEventWriter xw, String name, String value) throws XMLStreamException {
		value = StringUtils.trimToNull(value);
		
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, name) );
		if (value != null) xw.add( evtFact.createCharacters(value) );
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, name) );
	}
	
	/**
	 * Creates events for a single Element and adds them to the event stream.
	 * 
	 * Attributes are also written by passing an array of attribute names and
	 * values.  These arrays must not be null and must have matching lengths.
	 * 
	 * The passed value is trimmed to null and if null after trim, no text content
	 * is written.
	 * 
	 * The events are added using an empty prefix and using the default namespace.
	 * 
	 * @param xw
	 * @param name
	 * @param value
	 * @param attribNames
	 * @param attribValues
	 * @throws XMLStreamException
	 */
	protected void writeElemEvent(javax.xml.stream.XMLEventWriter xw, String name, 
				String value, String[] attribNames, String[] attribValues) throws XMLStreamException {
				
				
		value = StringUtils.trimToNull(value);
		
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, name) );
		
		for (int i = 0; i < attribNames.length; i++)  {
			xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, attribNames[i], attribValues[i]) );
		}
		
		if (value != null) {
			xw.add( evtFact.createCharacters(value) );
		}
		
		
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, name) );
	}
	
	/**
	 * Creates events for a single Element w/ a single attribute and adds them to
	 * the event stream.
	 * 
	 * The passed value is trimmed to null and if null after trim, no text content
	 * is written.
	 * 
	 * The events are added using an empty prefix and using the default namespace.
	 * 
	 * @param xw
	 * @param name
	 * @param value
	 * @param attribName
	 * @param attribValue
	 * @throws XMLStreamException
	 */
	protected void writeElemEvent(javax.xml.stream.XMLEventWriter xw, String name, 
				String value, String attribName, String attribValue) throws XMLStreamException {
				
		value = StringUtils.trimToNull(value);
		
		xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, name) );
		xw.add( evtFact.createAttribute(EMPTY, TARGET_NAMESPACE, attribName, attribValue) );

		
		if (value != null) {
			xw.add( evtFact.createCharacters(value) );
		}
		
		
		xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, name) );
	}
	
	/**
	 * Creates events for a single Element with text content and adds them to
	 * the event stream.  The passed value is trimmed to null and if null after
	 * trim, no element is added.
	 * 
	 * The events are added using an empty prefix and using the default namespace.
	 * 
	 * @param xw
	 * @param name
	 * @param value
	 * @throws XMLStreamException
	 */
	protected void writeElemEventIfNotNull(javax.xml.stream.XMLEventWriter xw, String name, String value) throws XMLStreamException {
		
		value = StringUtils.trimToNull(value);
		
		if (value != null) {
			xw.add( evtFact.createStartElement(EMPTY, TARGET_NAMESPACE, name) );
			xw.add( evtFact.createCharacters(value) );
			xw.add( evtFact.createEndElement(EMPTY, TARGET_NAMESPACE, name) );
		}
	}

}
