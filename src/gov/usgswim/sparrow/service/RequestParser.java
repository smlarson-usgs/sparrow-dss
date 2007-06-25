package gov.usgswim.sparrow.service;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface RequestParser<T> {
	public T parse(XMLStreamReader in) throws XMLStreamException;
}
