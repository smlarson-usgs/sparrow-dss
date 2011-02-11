package gov.usgswim.sparrow.datatable;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;

import org.junit.Before;
import org.junit.Test;

public class SingleColumnOverrideDataTest {

	DataTable baseDataTable;	//The base data w/o any coefs
	
	double[] overrideDoubles;		//the coefs for column 1
	
	StandardDoubleColumnData overColumn;	//The column of override vals
	
	SingleColumnOverrideDataTable table;	//The table w/ coefs on column 1

	@Before
	public void doTestSetup() {

		double[][] baseData;
		String[] baseHeadings;
		
		overrideDoubles = new double[] {1.1d,2.1d,3.2d,4.1d,5.3d};
		
		overColumn = new
			StandardDoubleColumnData(overrideDoubles, "myName", "myUnits", "myDesc",
				null, false); 
		
		
		baseHeadings = new String[] { "one", "two", "three", "four" };
		baseData = new double[][] {
				{ .1, .2, .3, .4 },
				{ 1.1, 1.2, 1.3, 1.4 },
				{ 2.1, 2.2, 2.3, 2.4 },
				{ 0, 3.2, 3.3, 3.4 },
				{ 0, 4.2, 4.3, 4.4 }
		};
		int[] baseRowIds = new int[] {1, 2, 3, 4, 5};
		SimpleDataTableWritable rwBaseDataTable = 
			new SimpleDataTableWritable(baseData, baseHeadings, baseRowIds);
		rwBaseDataTable.setName("BaseTable");
		rwBaseDataTable.setDescription("The BaseTable");
		rwBaseDataTable.setProperty("prop1", "prop1Value");
		
		rwBaseDataTable.getColumns()[1].setName("col1Name");
		rwBaseDataTable.getColumns()[1].setDescription("col1Desc");
		rwBaseDataTable.getColumns()[1].setUnits("col1Units");
		rwBaseDataTable.getColumns()[1].setProperty("col1_prop1", "col1_val1");
		
		rwBaseDataTable.setUnits("BaseUnit", 0);
		
		baseDataTable = rwBaseDataTable.toImmutable();

		table = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, null);

	}

	/**
	 * Only column 1 should be multiplied by the passed coef column and
	 * the multiplied column should have the same column attribs at the base table.
	 * @throws Exception
	 */
	@Test
	public void basicTest() throws Exception {
		
		//The first row
		assertEquals(.1d, table.getDouble(0, 0), .0000000000001d);
		assertEquals(1.1d, table.getDouble(0, 1), .0000000000001d);
		assertEquals(.3d, table.getDouble(0, 2), .0000000000001d);
		assertEquals(.4d, table.getDouble(0, 3), .0000000000001d);
		
		//The last row
		assertEquals(0d, table.getDouble(4, 0), .0000000000001d);
		assertEquals(5.3d, table.getDouble(4, 1), .0000000000001d);
		assertEquals(4.3d, table.getDouble(4, 2), .0000000000001d);
		assertEquals(4.4d, table.getDouble(4, 3), .0000000000001d);
		
		//column 1 attribs
		assertEquals("myName", table.getName(1));
		assertEquals("myUnits", table.getUnits(1));
		assertEquals("myDesc", table.getDescription(1));
		assertNull(table.getProperty(1, "col1_prop1"));
		
		//Check indexing
		assertTrue(table.hasRowIds());
		assertEquals(0, table.getRowForId(1L));
		assertEquals(4, table.getRowForId(5L));
	}
	

	@Test
	public void OverrideColumnAttributes() throws Exception {
		
		//Override the column attributes one at a time
		
		overrideDoubles = new double[] {1d,1d,1d,1d,1d};
		
		overColumn = new
			StandardDoubleColumnData(overrideDoubles, "myName", "myUnits", "myDesc",
				null, false); 
		

		//
		//Override Name
		
		ColumnAttribsBuilder ca = new ColumnAttribsBuilder();
		ca.setName("NameOR");
		SingleColumnOverrideDataTable tab = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("myDesc", tab.getDescription(1));
		assertEquals("myUnits", tab.getUnits(1));
		assertNull(tab.getProperty(1, "col1_prop1"));
		
		//
		//Override Description
		ca.setDescription("DescOR");
		tab = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("myUnits", tab.getUnits(1));
		assertNull(tab.getProperty(1, "col1_prop1"));
		
		//
		//Override Units
		ca.setUnits("UnitsOR");
		tab = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertNull(tab.getProperty(1, "col1_prop1"));
		
		//
		//Override a Property (edit value)
		ca.setProperty("col1_prop1", "col1_val2");
		tab = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("col1_val2", tab.getProperty(1, "col1_prop1"));
		
		//
		//Override a Property (add new value)
		ca.setProperty("col1_prop1", "col1_val2");
		ca.setProperty("col1_prop2", "col1_val99");
		tab = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("col1_val2", tab.getProperty(1, "col1_prop1"));
		assertEquals("col1_val99", tab.getProperty(1, "col1_prop2"));
		
		//
		//Override a Property (set all null)
		ca.setPropertiesNull();
		tab = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertNull(tab.getProperty(1, "col1_prop1"));
		
		//
		//Override a Property (set empty)
		ca.setProperty("col1_prop1", "");
		tab = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("", tab.getProperty(1, "col1_prop1"));
		
		//
		//Override a Property (set single prop null)
		ca.setProperty("col1_prop1", null);
		tab = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertNull(tab.getProperty(1, "col1_prop1"));
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorWithTooManyRows() {
		
		//Too many rows
		double[] badCoefs = new double[] {1.1d,2.1d,3.2d,4.1d,5.3d,6.5d};
		
		StandardDoubleColumnData badCoefColumn = new
			StandardDoubleColumnData(badCoefs, "myName", "myUnits", "myDesc",
				null, false); 
		
		table = new SingleColumnOverrideDataTable(baseDataTable, badCoefColumn, 1, null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorWithSpecifiedColumnIndexTooBig() {
		table = new SingleColumnOverrideDataTable(baseDataTable, overColumn, 4, null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorWithColumnIndexTooSmall() {
		table = new SingleColumnOverrideDataTable(baseDataTable, overColumn, -1, null);
	}
	


}
