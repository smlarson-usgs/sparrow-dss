package gov.usgs.webservices.framework.utils;

import gov.usgs.webservices.framework.formatter.XMLPassThroughFormatter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public abstract class UsgsStAXUtils {
	public static Map<Integer, String> eventNames;
	public static Location defaultLocation;
	public static NamespaceContext defaultNSContext;

	static {
		initializeEventNames();
		initializeDefaults();
	}
	
	private static void initializeDefaults() {
		// See XMLStreamReader.getLocation() javadocs for why this location acts
		// the way it does
		defaultLocation = new Location() {

			public int getCharacterOffset() {
				return -1;
			}

			public int getColumnNumber() {
				return -1;
			}

			public int getLineNumber() {
				return -1;
			}

			public String getPublicId() {
				return null;
			}

			public String getSystemId() {
				return null;
			}

		};
		
		defaultNSContext = new NamespaceContext() {

			public String getNamespaceURI(String prefix) {
				if (prefix == null) {
					throw new IllegalArgumentException("null namespace prefix not allowed");
				}
				return null;
			}

			public String getPrefix(String namespaceURI) {
				// TODO Auto-generated method stub
				return null;
			}

			public Iterator<?> getPrefixes(String namespaceURI) {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
	}

	private static void initializeEventNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		names.put(0, "NOT_YET_BEGUN_PARSING");
		names.put(XMLStreamConstants.START_ELEMENT, "START_ELEMENT");
		names.put(XMLStreamConstants.END_ELEMENT, "END_ELEMENT");
		names.put(XMLStreamConstants.PROCESSING_INSTRUCTION,
				"PROCESSING_INSTRUCTION");
		names.put(XMLStreamConstants.CHARACTERS, "CHARACTERS");
		names.put(XMLStreamConstants.COMMENT, "COMMENT");
		names.put(XMLStreamConstants.SPACE, "SPACE");
		names.put(XMLStreamConstants.START_DOCUMENT, "START_DOCUMENT");
		names.put(XMLStreamConstants.END_DOCUMENT, "END_DOCUMENT");
		names.put(XMLStreamConstants.ENTITY_REFERENCE, "ENTITY_REFERENCE");
		names.put(XMLStreamConstants.ATTRIBUTE, "ATTRIBUTE");
		names.put(XMLStreamConstants.DTD, "DTD");
		names.put(XMLStreamConstants.CDATA, "CDATA");
		names.put(XMLStreamConstants.NAMESPACE, "NAMESPACE");
		names.put(XMLStreamConstants.NOTATION_DECLARATION,
				"NOTATION_DECLARATION");
		names.put(XMLStreamConstants.ENTITY_DECLARATION,
				"ENTITY_DECLARATION");
		eventNames = names;
	}

    public static void printEventInfo(XMLStreamReader reader) throws XMLStreamException {
        int eventCode = reader.next();
        switch (eventCode) {
            case 1 :
                System.out.println("event = START_ELEMENT");
                System.out.println("Localname = "+reader.getLocalName());
                break;
            case 2 :
                System.out.println("event = END_ELEMENT");
                System.out.println("Localname = "+reader.getLocalName());
                break;
            case 3 :
                System.out.println("event = PROCESSING_INSTRUCTION");
                System.out.println("PIData = " + reader.getPIData());
                break;
            case 4 :
                System.out.println("event = CHARACTERS");
                System.out.println("Characters = " + reader.getText());
                break;
            case 5 :
                System.out.println("event = COMMENT");
                System.out.println("Comment = " + reader.getText());
                break;
            case 6 :
                System.out.println("event = SPACE");
                System.out.println("Space = " + reader.getText());
                break;
            case 7 :
                System.out.println("event = START_DOCUMENT");
                System.out.println("Document Started.");
                break;
            case 8 :
                System.out.println("event = END_DOCUMENT");
                System.out.println("Document Ended");
                break;
            case 9 :
                System.out.println("event = ENTITY_REFERENCE");
                System.out.println("Text = " + reader.getText());
                break;
            case 11 :
                System.out.println("event = DTD");
                System.out.println("DTD = " + reader.getText());

                break;
            case 12 :
                System.out.println("event = CDATA");
                System.out.println("CDATA = " + reader.getText());
                break;
        }
    }

	/**
		 * Copies the reader to the writer. The start and end document methods must
		 * be handled on the writer manually. This is copied from STAXUtils but with
		 * a modification to not completely ignore SPACE events as was done in the
		 * original
		 * 
		 * TODO: if the namespace on the reader has been declared previously to
		 * where we are in the stream, this probably won't work.
		 * 
		 * @param reader
		 * @param writer
		 * @throws XMLStreamException
		 */
		public static void copy( XMLStreamReader reader, XMLStreamWriter writer ) 
		throws XMLStreamException
		{
			int read = 0; // number of elements read in
			int event = reader.getEventType();
			int eventCount = 0;
	
			while ( reader.hasNext() )
			{
				switch( event )
				{
					case XMLStreamConstants.START_ELEMENT:
						read++;
						UsgsStAXUtils.writeStartElement( reader, writer );
						break;
					case XMLStreamConstants.END_ELEMENT:
						writer.writeEndElement();
						read--;
						if ( read <= 0 )
							return;
						break;
					case XMLStreamConstants.CHARACTERS:
						writer.writeCharacters( reader.getText() );  
						break;
					case XMLStreamConstants.START_DOCUMENT:
					case XMLStreamConstants.END_DOCUMENT:
					case XMLStreamConstants.ATTRIBUTE:
					case XMLStreamConstants.NAMESPACE:
						break;
					case XMLStreamConstants.CDATA:
						
						writer.writeCData(reader.getText());
						break;
					case XMLStreamConstants.SPACE:
						// SPACE events are generally ignored. However, a carriage
						// return needs to be printed every once in a while to
						// prevent excessive line lengths
						if (eventCount > XMLPassThroughFormatter.SKIP_EVENTS) {
							writer.writeCharacters(reader.getText());
							eventCount = 0;
						}
						break;
					case XMLStreamConstants.COMMENT:
						writer.writeComment(reader.getText());
						break;
					default:
						break;
				}
				eventCount++;
				event = reader.next();
			}
		}

	public static void writeStartElement(XMLStreamReader reader, XMLStreamWriter writer) 
	throws XMLStreamException
	{
		String local = reader.getLocalName();
		String uri = reader.getNamespaceURI();
		String prefix = reader.getPrefix();
		if (prefix == null)
		{
			prefix = "";
		}
	
		String boundPrefix = writer.getPrefix(uri);
		boolean writeElementNS = false;
		if ( boundPrefix == null || !prefix.equals(boundPrefix) )
		{   
			writeElementNS = true;
		}
	
		// Write out the element name
		if (uri != null && uri.length() > 0)
		{
			if (prefix.length() == 0) 
			{ 
	
				writer.writeStartElement(local);
				writer.setDefaultNamespace(uri); 
	
			} 
			else 
			{ 
				writer.writeStartElement(prefix, local, uri); 
				writer.setPrefix(prefix, uri); 
			} 
		}
		else
		{
			writer.writeStartElement( reader.getLocalName() );
		}
	
		// Write out the namespaces
		for ( int i = 0; i < reader.getNamespaceCount(); i++ )
		{
			String nsURI = reader.getNamespaceURI(i);
			String nsPrefix = reader.getNamespacePrefix(i);
	
			// Why oh why does the RI suck so much?
			if (nsURI == null) nsURI = "";
			if (nsPrefix == null) nsPrefix = "";
	
			if ( nsPrefix.length() ==  0 )
			{
				writer.writeDefaultNamespace(nsURI);
			}
			else
			{
				writer.writeNamespace(nsPrefix, nsURI);
			}
	
			if (uri != null && nsURI.equals(uri) && nsPrefix.equals(prefix))
			{
				writeElementNS = false;
			}
		}
	
		// Check if the namespace still needs to be written.
		// We need this check because namespace writing works 
		// different on Woodstox and the RI.
		if (writeElementNS && uri != null)
		{
			if ( prefix.length() ==  0 )
			{
				writer.writeDefaultNamespace(uri);
			}
			else
			{
				writer.writeNamespace(prefix, uri);
			}
		}
	
		// Write out attributes
		for ( int i = 0; i < reader.getAttributeCount(); i++ )
		{
			String ns = reader.getAttributeNamespace(i);
			String nsPrefix = reader.getAttributePrefix(i);
			if ( ns == null || ns.length() == 0 ){
				writer.writeAttribute(
						reader.getAttributeLocalName(i),
						reader.getAttributeValue(i));
			}
			else if (nsPrefix == null || nsPrefix.length() == 0)
			{
				writer.writeAttribute(
						reader.getAttributeNamespace(i),
						reader.getAttributeLocalName(i),
						reader.getAttributeValue(i));
			}
			else
			{
				writer.writeAttribute(reader.getAttributePrefix(i),
						reader.getAttributeNamespace(i),
						reader.getAttributeLocalName(i),
						reader.getAttributeValue(i));
			}
		}
	}
	
	public static XMLStreamReader wrapXMLStreamReaderIgnoreNamespaces(final XMLStreamReader xReader) {
		return new XMLStreamReader() {
			private XMLStreamReader reader = xReader;

			public void close() throws XMLStreamException {reader.close();}

			public int getAttributeCount() {
				return reader.getAttributeCount();
			}

			public String getAttributeLocalName(int index) {
				return reader.getAttributeLocalName(index);
			}

			public QName getAttributeName(int index) {
				return reader.getAttributeName(index);
			}

			public String getAttributeNamespace(int index) {
				// IGNORING NAMESPACES
				return XMLConstants.DEFAULT_NS_PREFIX;
			}

			public String getAttributePrefix(int index) {
				// IGNORING NAMESPACES
				return XMLConstants.DEFAULT_NS_PREFIX;
			}

			public String getAttributeType(int index) {
				return reader.getAttributeType(index);
			}

			public String getAttributeValue(int index) {
				return reader.getAttributeValue(index);
			}

			public String getAttributeValue(String namespaceURI, String localName) {
				return reader.getAttributeValue(namespaceURI, localName);
			}

			public String getCharacterEncodingScheme() {
				return reader.getCharacterEncodingScheme();
			}

			public String getElementText() throws XMLStreamException {
				return reader.getElementText();
			}

			public String getEncoding() {
				return reader.getEncoding();
			}

			public int getEventType() {
				return reader.getEventType();
			}

			public String getLocalName() {
				return reader.getLocalName();
			}

			public Location getLocation() {
				return reader.getLocation();
			}

			public QName getName() {
				return reader.getName();
			}

			public NamespaceContext getNamespaceContext() {
				return reader.getNamespaceContext();
			}

			public int getNamespaceCount() {
				return reader.getNamespaceCount();
			}

			public String getNamespacePrefix(int index) {
				return reader.getNamespacePrefix(index);
			}

			public String getNamespaceURI() {
				// IGNORING NAMESPACES
				return XMLConstants.DEFAULT_NS_PREFIX;
			}

			public String getNamespaceURI(String prefix) {
				// IGNORING NAMESPACES
				return XMLConstants.DEFAULT_NS_PREFIX;
			}

			public String getNamespaceURI(int index) {
				// IGNORING NAMESPACES
				return XMLConstants.DEFAULT_NS_PREFIX;
			}

			public String getPIData() {
				return reader.getPIData();
			}

			public String getPITarget() {
				return reader.getPITarget();
			}

			public String getPrefix() {
				return XMLConstants.DEFAULT_NS_PREFIX;
			}

			public Object getProperty(String name) throws IllegalArgumentException {
				return reader.getProperty(name);
			}

			public String getText() {
				return reader.getText();
			}

			public char[] getTextCharacters() {
				return reader.getTextCharacters();
			}

			public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
				return reader.getTextCharacters(sourceStart, target, targetStart, length);
			}

			public int getTextLength() {
				return reader.getTextLength();
			}

			public int getTextStart() {
				return reader.getTextStart();
			}

			public String getVersion() {
				return reader.getVersion();
			}

			public boolean hasName() {
				return reader.hasName();
			}

			public boolean hasNext() throws XMLStreamException {
				return reader.hasNext();
			}

			public boolean hasText() {
				return reader.hasText();
			}

			public boolean isAttributeSpecified(int index) {
				return reader.isAttributeSpecified(index);
			}

			public boolean isCharacters() {
				return reader.isCharacters();
			}

			public boolean isEndElement() {
				return reader.isEndElement();
			}

			public boolean isStandalone() {
				return reader.isStandalone();
			}

			public boolean isStartElement() {
				return reader.isStartElement();
			}

			public boolean isWhiteSpace() {
				return reader.isWhiteSpace();
			}

			public int next() throws XMLStreamException {
				return reader.next();
			}

			public int nextTag() throws XMLStreamException {
				return reader.nextTag();
			}

			public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
				reader.require(type, namespaceURI, localName);
			}

			public boolean standaloneSet() {
				return reader.standaloneSet();
			}
			
		};
	}
		
		
}
