package gov.usgswim.sparrow.service;


import com.ctc.wstx.stax.WstxEventFactory;

import com.ctc.wstx.stax.WstxOutputFactory;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang.StringUtils;

public abstract class AbstractSerializer {
	public static String EMPTY = StringUtils.EMPTY;
	public static String ENCODING = "ISO-8859-1";
	public static String XML_VERSION = "1.0";
	

	public static String XMLSCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema-instance";
	public static String XMLSCHEMA_PREFIX = "xsi";
	
	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	protected static XMLEventFactory evtFact;
	protected static XMLOutputFactory xoFact;
	
	public AbstractSerializer() {
	
		synchronized (factoryLock) {
			if (evtFact == null) {
				evtFact = WstxEventFactory.newInstance();
				xoFact = WstxOutputFactory.newInstance();
			}
		}
	}
	
	public abstract String getTargetNamespace();
	
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
		
		xw.add( evtFact.createStartElement(EMPTY, getTargetNamespace(), name) );
		if (value != null) xw.add( evtFact.createCharacters(value) );
		xw.add( evtFact.createEndElement(EMPTY, getTargetNamespace(), name) );
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
		
		xw.add( evtFact.createStartElement(EMPTY, getTargetNamespace(), name) );
		
		for (int i = 0; i < attribNames.length; i++)  {
			xw.add( evtFact.createAttribute(EMPTY, getTargetNamespace(), attribNames[i], attribValues[i]) );
		}
		
		if (value != null) {
			xw.add( evtFact.createCharacters(value) );
		}
		
		
		xw.add( evtFact.createEndElement(EMPTY, getTargetNamespace(), name) );
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
		
		xw.add( evtFact.createStartElement(EMPTY, getTargetNamespace(), name) );
		xw.add( evtFact.createAttribute(EMPTY, getTargetNamespace(), attribName, attribValue) );

		
		if (value != null) {
			xw.add( evtFact.createCharacters(value) );
		}
		
		
		xw.add( evtFact.createEndElement(EMPTY, getTargetNamespace(), name) );
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
			xw.add( evtFact.createStartElement(EMPTY, getTargetNamespace(), name) );
			xw.add( evtFact.createCharacters(value) );
			xw.add( evtFact.createEndElement(EMPTY, getTargetNamespace(), name) );
		}
	}

}
