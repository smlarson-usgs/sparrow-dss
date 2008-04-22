package gov.usgswim.sparrow.test;

import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import gov.usgswim.service.ResponseFormat;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class ResponseFormatTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParse() throws XMLStreamException {
		String testRequest="<response-format name='pre-configured format name' compress='zip'>"
			+ "	<mime-type>text</mime-type>"
			+ "	<template>beige</template>"
			+ "	<params>"
			+ "		<param name='gov.usgswim.WordGenerator.marin-top'>1.5in</param>"
			+ "	</params>"
			+ "</response-format>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ResponseFormat rf = new ResponseFormat();
		reader.next();
		rf.parse(reader);
		assertEquals("pre-configured format name", rf.name);
		assertEquals("zip", rf.getCompression());
		assertEquals("text", rf.getMimeType());
		
	}
	
	public void testAlternateMimetypeString() throws XMLStreamException {
		String testRequest="<response-format name='pre-configured format name' compress='zip'>"
			+ "	<mimeType>text</mimeType>"
			+ "	<template>beige</template>"
			+ "	<params>"
			+ "		<param name='gov.usgswim.WordGenerator.marin-top'>1.5in</param>"
			+ "	</params>"
			+ "</response-format>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ResponseFormat rf = new ResponseFormat();
		reader.next();
		rf.parse(reader);
		assertEquals("text", rf.getMimeType());
		
	}

//	public void testGetOutputType() {
//
//	}

	public void testSetMimeType() {
		ResponseFormat rf = new ResponseFormat();
		rf.setMimeType("csv");
		// output type is set implicitly
		assertEquals(OutputType.CSV, rf.getOutputType());
	}

	public void testSetCompression() {
		ResponseFormat rf = new ResponseFormat();

		rf.setMimeType("GzIp");
		assertNull("currently unrecognized output type", rf.getOutputType());

		// note that setting the attribute is case-insensitive.
		rf.setMimeType("zIp"); 
		assertEquals("zip", rf.getMimeType());

	}

}