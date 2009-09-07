package gov.usgswim.sparrow.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTableWritable;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


public class DataResourceLoaderTest {
	public static final int TEST_MODEL = -1;
	public static DataTableWritable sourceMetaData;


	@BeforeClass
	public static void loadSourceMetadata() throws SQLException, IOException {
		sourceMetaData = DataResourceLoader.loadSourceMetadata(TEST_MODEL);
	}

	@Test
	public void testLoadSourceMetadata() {
		assertTrue(sourceMetaData != null);
		assertEquals(8, sourceMetaData.getColumnCount());
		assertEquals("SOURCE_ID", sourceMetaData.getName(0));
		assertEquals("IS_POINT_SOURCE", sourceMetaData.getName(7));
		assertTrue("A real model should have at least 4 sources", sourceMetaData.getRowCount() >= 4);
//		DataTableUtils.printDataTable(sourceMetaData, "source metadata");
	}

	@Test
	public void testLoadTopo() throws SQLException, IOException {
		DataTableWritable topo = DataResourceLoader.loadTopo(TEST_MODEL);

		assertTrue(topo != null);
		assertEquals(5, topo.getColumnCount());
		assertEquals("fnode", topo.getName(1));
		assertTrue(topo.getRowCount() > 10);
	}

	@Test
	public void testLoadSourceReachCoefficients() throws SQLException, IOException {
		DataTableWritable reachCoefficients = DataResourceLoader.loadSourceReachCoef(TEST_MODEL, sourceMetaData);

		assertTrue(reachCoefficients != null);
		assertEquals(sourceMetaData.getRowCount(), reachCoefficients.getColumnCount());

	}

	@Test
	public void testLoadDecay() throws SQLException, IOException {
		DataTableWritable decay = DataResourceLoader.loadDecay(TEST_MODEL);

		assertTrue(decay != null);
		assertEquals(2, decay.getColumnCount());

	}

	@Test
	public void testLoadSourceValues() throws SQLException, IOException {
		DataTableWritable sourceValues = DataResourceLoader.loadSourceValues(TEST_MODEL, sourceMetaData);

		assertTrue(sourceValues != null);
		assertEquals(5, sourceValues.getColumnCount());
		assertEquals("fnode", sourceValues.getName(1));
		assertTrue(sourceValues.getRowCount() > 10);
	}


	// ==========================
	// The following don't belong
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
		System.out.println(xstream.toXML(ilin));
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
		System.out.println(ilin.firstname + " " + ilin.lastname);
	}





}
