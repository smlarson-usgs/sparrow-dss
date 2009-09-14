package gov.usgswim.sparrow.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.sparrow.PredictData;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import static gov.usgswim.sparrow.util.DataResourceLoader.*;


public class DataResourceLoaderTest {
	public static final long TEST_MODEL = -1;
	public static final int NUMBER_OF_TEST_SOURCES = 2;
	public static DataTableWritable sourceMetaData;


	@BeforeClass
	public static void loadMetadata() throws SQLException, IOException {
		sourceMetaData = loadSourceMetadata(TEST_MODEL);
	}

	@Test
	public void testLoadSourceMetadata() {
		assertTrue(sourceMetaData != null);
		assertEquals(8, sourceMetaData.getColumnCount());
		assertEquals("SOURCE_ID", sourceMetaData.getName(0));
		assertEquals("IS_POINT_SOURCE", sourceMetaData.getName(7));
		assertEquals("The test model only has only " + NUMBER_OF_TEST_SOURCES + " sources for simplicity",
				NUMBER_OF_TEST_SOURCES, sourceMetaData.getRowCount());
	}

	@Test
	public void testLoadTopo() throws SQLException, IOException {
		DataTableWritable topo = loadTopo(TEST_MODEL);

		assertTrue(topo != null);
		assertEquals(5, topo.getColumnCount());
		assertEquals("fnode", topo.getName(1));
		assertTrue(topo.getRowCount() > 10);
	}

	@Test
	public void testLoadSourceReachCoefficients() throws SQLException, IOException {
		DataTableWritable reachCoefficients = loadSourceReachCoef(TEST_MODEL, sourceMetaData);

		assertTrue(reachCoefficients != null);
		assertEquals(sourceMetaData.getRowCount(), reachCoefficients.getColumnCount());

	}

	@Test
	public void testLoadDecay() throws SQLException, IOException {
		DataTableWritable decay = loadDecay(TEST_MODEL);

		assertTrue(decay != null);
		assertEquals(2, decay.getColumnCount());

	}

	@Test
	public void testLoadSourceValues() throws SQLException, IOException {
		DataTableWritable topo = loadTopo(TEST_MODEL);
		DataTableWritable sourceValues = loadSourceValues(TEST_MODEL, sourceMetaData, topo);

		assertTrue(sourceValues != null);
		assertTrue(sourceValues.hasRowIds());
		assertEquals("There should be one value for each source", sourceMetaData.getRowCount(), sourceValues.getColumnCount());
		assertEquals("Only two sources in the test model", NUMBER_OF_TEST_SOURCES, sourceValues.getColumnCount());
	}

	@Test
	public void testDataConsistency() throws SQLException, IOException {
		PredictData predictData = loadModelData(TEST_MODEL);

		int numberOfReaches = predictData.getTopo().getRowCount();
		assertEquals("There should be a pair of decay coefficients for each reach", numberOfReaches, predictData.getDecay().getRowCount());
		assertEquals("There should be a set of reach coefficients for each reach", numberOfReaches, predictData.getCoef().getRowCount());
		assertEquals("There should be a set of source values for each reach", numberOfReaches, predictData.getSrc().getRowCount());

	}


	// ==========================
	// The following don't belong TODO delete later
	// ==========================
	public static class Person {
		  private String firstname;
		  private String lastname;

		  public Person(String firstname, String lastname) {
			  this.firstname = firstname;
			  this.lastname = lastname;
		  }
	}

	@Test
	public void testXStreamToXML() {
		XStream xstream = new XStream();
		xstream.alias("person", Person.class);

		Person ilin = new Person("I-Lin", "Kuo");
		//System.out.println(xstream.toXML(ilin));
	}

	@Test
	public void testXStreamFromXML() {
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("person", Person.class);

		String ilinXML = "	<person>\r\n" +
				"	  <firstname>I-Lin</firstname>\r\n" +
				"	  <lastname>Kuo</lastname>\r\n" +
				"	</person>";

		Person ilin = (Person) xstream.fromXML(ilinXML);
		//System.out.println(ilin.firstname + " " + ilin.lastname);
	}





}
