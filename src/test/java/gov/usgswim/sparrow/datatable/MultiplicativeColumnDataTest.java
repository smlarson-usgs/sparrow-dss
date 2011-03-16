package gov.usgswim.sparrow.datatable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;

import org.junit.Before;
import org.junit.Test;

public class MultiplicativeColumnDataTest {

	DataTableWritable primaryTable;
	DataTableWritable coefTable;

	@Before
	public void doTestSetup() {

		primaryTable = buildTable("primary");
		coefTable = buildTable("coef");

	}
	
	protected DataTableWritable buildTable(String tableName) {
		
		String[]  baseHeadings = new String[] { "one", "two", "three", "four" };
		double[][] baseData = new double[][] {
				{ .1, .2, .3, .4 },
				{ 1.1, 1.2, 1.3, 1.4 },
				{ 2.1, 2.2, 2.3, 2.4 },
				{ 0, 3.2, 3.3, 3.4 },
				{ 0, 4.2, 4.3, 4.4 }
		};
		int[] baseRowIds = new int[] {1, 2, 3, 4, 5};
		SimpleDataTableWritable rwTable = 
			new SimpleDataTableWritable(baseData, baseHeadings, baseRowIds);
		rwTable.setName(tableName);
		rwTable.setDescription("The Table '" + tableName + "'");
		rwTable.setProperty("prop1", tableName + "Prop1Value");
		
		for (int i=0; i<4; i++) {
			rwTable.getColumns()[i].setName(tableName + i + "ColName");
			rwTable.getColumns()[i].setDescription(tableName + i + "ColDesc");
			rwTable.getColumns()[i].setUnits(tableName + i + "ColUnits");
			rwTable.getColumns()[i].setProperty(tableName + i + "ColPropName", tableName + i + "ColPropVal");
			rwTable.getColumns()[i].setProperty(tableName + i + "ColPropName2", tableName + i + "ColPropVal2");
		}
		
		return rwTable;
	}


	@Test
	public void CheckValuesAndAttributes() throws Exception {
		
		ColumnAttribsBuilder attribs = new ColumnAttribsBuilder();
		attribs.setName("CustomName");
		attribs.setProperty("CustomKey", "CustomVal");	//New attrib
		attribs.setProperty("primary0ColPropName2", "CustomVal2");	//Override attrib
		
		MultiplicativeColumnData combi = new MultiplicativeColumnData(
				primaryTable.getColumn(0), coefTable.getColumn(3), attribs);
		
		assertEquals(5, combi.getRowCount().intValue());
		
		//Some value checks
		assertEquals(.1d * .4d, combi.getDouble(0), .000000001);
		assertEquals((int)(2.1d * 2.4d), combi.getInt(2).intValue());
		assertEquals(0d * 4.4d, combi.getDouble(4), .000000001);
		assertEquals(2.1d * 2.4d, combi.getMaxDouble(), .000000001);
		assertEquals(0d * 4.4d, combi.getMinDouble(), .000000001);
		//assertEquals(0, combi.findFirst(.04d)); Not really testable due to precision errs
		assertEquals(3, combi.findFirst(0d));
		assertEquals(4, combi.findLast(0d));
		
		//Attrib value checks
		assertEquals("CustomName", combi.getName());
		assertEquals("primary0ColDesc", combi.getDescription());
		assertEquals("primary0ColPropVal", combi.getProperty("primary0ColPropName"));
		assertEquals("CustomVal2", combi.getProperty("primary0ColPropName2"));
		assertEquals(3, combi.getPropertyNames().size());
		assertEquals(3, combi.getProperties().size());
		assertTrue(combi.getPropertyNames().contains("primary0ColPropName"));
		assertEquals("primary0ColPropVal", combi.getProperties().get("primary0ColPropName"));
		assertTrue(combi.getPropertyNames().contains("primary0ColPropName2"));
		assertEquals("CustomVal2", combi.getProperties().get("primary0ColPropName2"));
		assertTrue(combi.getPropertyNames().contains("CustomKey"));
		assertEquals("CustomVal", combi.getProperties().get("CustomKey"));
	}
	


}
