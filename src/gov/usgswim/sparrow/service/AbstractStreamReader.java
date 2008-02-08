package gov.usgswim.sparrow.service;

import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;

import java.util.LinkedList;
import java.util.Queue;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

public abstract class AbstractStreamReader extends BasicXMLStreamReader implements XMLStreamReader {

}
