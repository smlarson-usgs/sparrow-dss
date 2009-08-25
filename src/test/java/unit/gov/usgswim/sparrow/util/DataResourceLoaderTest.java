package gov.usgswim.sparrow.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTableWritable;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DataResourceLoaderTest {



	@Test
	public void testLoadTopoFromResources() throws SQLException, IOException {
		DataTableWritable topo = DataResourceLoader.loadTopo(-1);

		assertTrue(topo != null);
		assertEquals(5, topo.getColumnCount());
		assertEquals("fnode", topo.getName(1));
		assertTrue(topo.getRowCount() > 10);
	}

	public static class Person {
		  private String firstname;
		  private String lastname;

		  public Person(String firstname, String lastname) {
			  this.firstname = firstname;
			  this.lastname = lastname;
		  }
	}
	// ==========================
	// The following don't belong
	// ==========================

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
