package gov.usgswim.sparrow.service;

import com.ctc.wstx.stax.WstxEventFactory;
import com.ctc.wstx.stax.WstxOutputFactory;

import java.io.FileInputStream;

import java.io.FileNotFoundException;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;

import javax.xml.stream.XMLOutputFactory;

import javax.xml.stream.XMLStreamException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

public class MetaRequestHandler {

	//They promise these factories are threadsafe
	private static Object factoryLock = new Object();
	protected static XMLInputFactory inFact;

	public MetaRequestHandler() {

		synchronized (factoryLock) {
			if (inFact == null) {
				inFact = XMLInputFactory2.newInstance();

				inFact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
													 Boolean.FALSE);
				inFact.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
													 Boolean.FALSE);
				inFact.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
			}
		}
	}

	//public handleRequest


	public ServiceRequest handleRequest(XMLStreamReader reader) throws XMLStreamException {
		ModelRequest req = null;
		
		while (reader.hasNext()) {
			int eventCode = reader.next();
			
			switch (eventCode) {
			case XMLStreamReader.START_ELEMENT:
				String lName = reader.getLocalName();
				
				if ("model".equals(lName)) {
					req = new ModelRequest();
					
					if (reader.getAttributeCount() > 0) {
						for (int i = 0; i < reader.getAttributeCount(); i++)  {
							String name = reader.getAttributeLocalName(i);
							String val = reader.getAttributeValue(i);
							if ("public".equals(name)) {
								req.setPublic(val);
							} else if ("approved".equals(name)) {
								req.setApproved(val);
							} else if ("archived".equals(name)) {
								req.setArchived(val);
							}
						}
						
						
					}
					
					
				} else if ("source".equals(lName)) {
					req.setSources(true);
				}
				
				
				break;
			}
		}
		
		return req;
	}
	
	public void printEventInfo(XMLStreamReader reader) throws XMLStreamException {
		int eventCode = reader.next();
		switch (eventCode) {
		case XMLStreamReader.START_ELEMENT:
			System.out.println("event = START_ELEMENT");
			System.out.println("Localname = " + reader.getLocalName());
			if (reader.getAttributeCount() > 0) {
				for (int i = 0; i < reader.getAttributeCount(); i++)  {
					String name = reader.getAttributeLocalName(i);
					String val = reader.getAttributeValue(i);
					System.out.println("ATTRIB " + name + " = " + val);
				}
				
				
			}
			break;
		case 2:
			System.out.println("event = END_ELEMENT");
			System.out.println("Localname = " + reader.getLocalName());
			break;
		case 3:
			System.out.println("event = PROCESSING_INSTRUCTION");
			System.out.println("PIData = " + reader.getPIData());
			break;
		case 4:
			System.out.println("event = CHARACTERS");
			System.out.println("Characters = " + reader.getText());
			break;
		case 5:
			System.out.println("event = COMMENT");
			System.out.println("Comment = " + reader.getText());
			break;
		case 6:
			System.out.println("event = SPACE");
			System.out.println("Space = " + reader.getText());
			break;
		case 7:
			System.out.println("event = START_DOCUMENT");
			System.out.println("Document Started.");
			break;
		case 8:
			System.out.println("event = END_DOCUMENT");
			System.out.println("Document Ended");
			break;
		case 9:
			System.out.println("event = ENTITY_REFERENCE");
			System.out.println("Text = " + reader.getText());
			break;
		case 11:
			System.out.println("event = DTD");
			System.out.println("DTD = " + reader.getText());

			break;
		case 12:
			System.out.println("event = CDATA");
			System.out.println("CDATA = " + reader.getText());
			break;
		case XMLEvent.ATTRIBUTE:
			System.out.println("event = ATTRIBUTE");
			System.out.println("name = " + reader.getLocalName());
		}
		
	}


}
