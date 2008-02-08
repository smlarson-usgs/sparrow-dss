package gov.usgswim.service.pipeline;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

/**
 * This class masquerades as an XMLEventWriter to store up the events. It then
 * can respond as an XMLStreamReader or XMLEventReader
 * 
 * @author ilinkuo
 * 
 */
public class XMLEventStorer implements XMLEventWriter {

	public void add(XMLEvent arg0) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void add(XMLEventReader arg0) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void close() throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void flush() throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public NamespaceContext getNamespaceContext() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPrefix(String arg0) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDefaultNamespace(String arg0) throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void setNamespaceContext(NamespaceContext arg0)
			throws XMLStreamException {
		// TODO Auto-generated method stub

	}

	public void setPrefix(String arg0, String arg1) throws XMLStreamException {
		// TODO Auto-generated method stub

	}
	
	public XMLStreamReader makeStreamReader() {
		return null;
	}
	
	public XMLEventReader makeEventReader() {
		throw new UnsupportedOperationException("not yet implemented");
	}
	

}
