package gov.usgswim.sparrow.datatable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import gov.usgs.cida.datatable.ColumnAttribsBuilder;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;

import org.junit.Before;
import org.junit.Test;

public class DivideColumnDataTest extends DatatableTestBase {

	DataTableWritable table;

	@Before
	public void doTestSetup() {

		table = buildDoubleTable("primary");

	}


	/*
	 * Quick build a double table with basic data.
	 * 
	 * Table Data (cols 0 & 3 are used)
	 * 		*0*						*3*
	 * 		___________________________
	 *		.1	|	.2	|	.3	|	.4
	 *		1.1	|	1.2	|	1.3	|	1.4
	 *		2.1	|	2.2	|	2.3	|	2.4
	 *		0	|	3.2	|	3.3	|	3.4
	 *		0	|	4.2	|	4.3	|	4.4
	 *		___________________________
	 * 
	 */
	
	@Test
	public void CheckColumn0DividedByColumn3() throws Exception {
		
		ColumnAttribsBuilder attribs = new ColumnAttribsBuilder();
		attribs.setName("CustomName");
		attribs.setProperty("CustomKey", "CustomVal");	//New attrib
		attribs.setProperty("primary3ColPropName2", "CustomVal2");	//Override attrib
		
		DivideColumnData combi = new DivideColumnData(
				table.getColumn(0), table.getColumn(3), attribs);
		
		assertEquals(5, combi.getRowCount().intValue());
		
		//Some value checks
		assertEquals(.1d / .4d, combi.getDouble(0), .000000001);
		assertEquals((int)(2.1d / 2.4d), combi.getInt(2).intValue());
		assertEquals(0d, combi.getDouble(4), .000000001);
		assertEquals(2.1d / 2.4d, combi.getMaxDouble(), .000000001);
		assertEquals(0d, combi.getMinDouble(), .000000001);
		//assertEquals(0, combi.findFirst(.04d)); Not really testable due to precision errs
		assertEquals(3, combi.findFirst(0d));
		assertEquals(4, combi.findLast(0d));
		
		//Attrib value checks
		//Direct access
		assertEquals("CustomName", combi.getName());
		assertEquals("primary3ColDesc", combi.getDescription());
		assertEquals("CustomVal", combi.getProperty("CustomKey"));
		assertEquals("CustomVal2", combi.getProperty("primary3ColPropName2"));
		assertEquals(3, combi.getPropertyNames().size());
		assertEquals(3, combi.getProperties().size());
		
		//Access via combined properties object
		Set<String> propsNames = combi.getPropertyNames();
		Map<String, String> props = combi.getProperties();
		assertTrue(propsNames.contains("primary3ColPropName"));
		assertEquals("primary3ColPropVal", props.get("primary3ColPropName"));
		assertTrue(propsNames.contains("primary3ColPropName2"));
		assertTrue(propsNames.contains("CustomKey"));
		assertEquals("CustomVal", props.get("CustomKey"));
	}
	
	@Test
	public void CheckColumn3DividedByColumn0() throws Exception {
		
		ColumnAttribsBuilder attribs = new ColumnAttribsBuilder();
		attribs.setProperty("CustomKey", "CustomVal");	//New attrib
		attribs.setProperty("primary0ColPropName2", "CustomVal2");	//Override attrib
		
		DivideColumnData combi = new DivideColumnData(
				table.getColumn(3), table.getColumn(0), attribs);
		
		assertEquals(5, combi.getRowCount().intValue());
		
		//Some value checks
		assertEquals(.4d / .1d, combi.getDouble(0), .000000001);
		assertEquals((int)(2.4d / 2.1d), combi.getInt(2).intValue());
		assertEquals(Double.NaN, combi.getDouble(4), .000000001);
		assertEquals(.4d / .1d, combi.getMaxDouble(), .000000001);
		assertEquals(2.4d / 2.1d, combi.getMinDouble(), .000000001);
		//assertEquals(0, combi.findFirst(.04d)); Not really testable due to precision errs
		assertEquals(0, combi.findFirst(.4d / .1d));
		assertEquals(0, combi.findLast(.4d / .1d));
		
		
		//Attrib value checks
		//Direct access
		assertEquals("primary0ColName", combi.getName());
		assertEquals("primary0ColDesc", combi.getDescription());
		assertEquals("CustomVal", combi.getProperty("CustomKey"));
		assertEquals("CustomVal2", combi.getProperty("primary0ColPropName2"));
		assertEquals(3, combi.getPropertyNames().size());
		assertEquals(3, combi.getProperties().size());
		
		//Access via combined properties object
		Set<String> propsNames = combi.getPropertyNames();
		Map<String, String> props = combi.getProperties();
		assertTrue(propsNames.contains("primary0ColPropName"));
		assertEquals("primary0ColPropVal", props.get("primary0ColPropName"));
		assertTrue(propsNames.contains("primary0ColPropName2"));
		assertTrue(propsNames.contains("CustomKey"));
		assertEquals("CustomVal", props.get("CustomKey"));
	}
	


}
