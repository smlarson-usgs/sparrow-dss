package gov.usgswim.sparrow.test;

import com.ctc.wstx.evt.WstxEventWriter;

import gov.usgswim.sparrow.Data2DCompare;
import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.service.DomainSerializer;

import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.service.PredictService;
import gov.usgswim.sparrow.service.PredictionSerializer;
import gov.usgswim.sparrow.util.JDBCUtil;

import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.SQLException;

import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import oracle.jdbc.OracleDriver;

import org.codehaus.stax2.XMLInputFactory2;

public class PredictionSerializerTest extends TestCase {
	private Connection conn;
	
	public PredictionSerializerTest(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}
	
	
	/**
	 * Not really much of a test - it just writes the document out to a temp file.
	 * @throws Exception
	 */
	public void testBasicPrediction() throws Exception {
		
		XMLInputFactory xinFact = XMLInputFactory2.newInstance();
		XMLStreamReader xsr = xinFact.createXMLStreamReader(
			this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-request-0.xml"));
		
		PredictService service = new PredictService();
		Double2D result = (Double2D) service.dispatch(xsr);

		PredictionSerializer ps = new PredictionSerializer();
		
		File outFile = File.createTempFile("predict-serilizer-test", ".xml");
		FileOutputStream fos = new FileOutputStream(outFile);
		
		ps.writeResponse(fos, null, result);
		fos.close();
		System.out.println("Result of prediction serialization written to: " + outFile.getAbsolutePath());

	}
	

}
