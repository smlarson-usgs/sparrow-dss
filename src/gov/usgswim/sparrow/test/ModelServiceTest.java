package gov.usgswim.sparrow.test;

import com.ctc.wstx.evt.WstxEventWriter;
import com.ctc.wstx.stax.WstxOutputFactory;

import gov.usgswim.sparrow.service.DomainSerializer;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.service.HttpServiceHandler;

import gov.usgswim.sparrow.service.ModelRequest;
import gov.usgswim.sparrow.service.ModelService;
import gov.usgswim.sparrow.service.ServiceHandler;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.List;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import javax.xml.stream.XMLStreamReader;

import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;

import oracle.jdbc.OracleDriver;

import org.codehaus.stax2.XMLInputFactory2;

public class ModelServiceTest extends TestCase {
	//private Connection conn;

	public ModelServiceTest(String sTestName) {
		super(sTestName);
	}

	/**
	 */
	public void testPrintEventInfo() throws Exception {
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/model_request_1.xml"));
		
		ServiceHandler handler = new ModelService();
		handler.dispatch(xsr, System.out);
		
		
	}
}
