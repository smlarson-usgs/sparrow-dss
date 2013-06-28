package gov.usgswim.sparrow.datatable;

import static org.junit.Assert.assertEquals;
import gov.usgs.cida.datatable.ColumnAttribsBuilder;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.impl.StandardDoubleColumnData;

import org.junit.Before;
import org.junit.Test;

public class SingleColumnCoefDataTest {

	DataTable baseDataTable;	//The base data w/o any coefs
	
	double[] coefs;		//the coefs for column 1
	
	StandardDoubleColumnData coefColumn;	//The column of coefs
	
	SingleColumnCoefDataTable table;	//The table w/ coefs on column 1

	@Before
	public void doTestSetup() {

		double[][] baseData;
		String[] baseHeadings;
		
		coefs = new double[] {1.1d,2.1d,3.2d,4.1d,5.3d};
		
		coefColumn = new
			StandardDoubleColumnData(coefs, "myName", "myUnits", "myDesc",
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

		table = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, null);

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
		assertEquals(.2d * 1.1d, table.getDouble(0, 1), .0000000000001d);
		assertEquals(.3d, table.getDouble(0, 2), .0000000000001d);
		assertEquals(.4d, table.getDouble(0, 3), .0000000000001d);
		
		//The last row
		assertEquals(0d, table.getDouble(4, 0), .0000000000001d);
		assertEquals(4.2d * 5.3d, table.getDouble(4, 1), .0000000000001d);
		assertEquals(4.3d, table.getDouble(4, 2), .0000000000001d);
		assertEquals(4.4d, table.getDouble(4, 3), .0000000000001d);
		
		//column 1 attribs
		assertEquals("col1Name", table.getName(1));
		assertEquals("col1Units", table.getUnits(1));
		assertEquals("col1Desc", table.getDescription(1));
		assertEquals("col1_val1", table.getProperty(1, "col1_prop1"));
	}
	
	@Test
	public void divideByZeroTest() throws Exception {
		double[] coefs = new double[] {0d,0d,0d,0d,0d};
		StandardDoubleColumnData coefColumn = new
		StandardDoubleColumnData(coefs, "myName", "myUnits", "myDesc",
			null, false);
		
		table = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 0, null, true);
		
		
		assertEquals(Double.POSITIVE_INFINITY, table.getDouble(0, 0), .0000000000001d);
		assertEquals(Double.POSITIVE_INFINITY, table.getDouble(1, 0), .0000000000001d);
		assertEquals(Double.POSITIVE_INFINITY, table.getDouble(2, 0), .0000000000001d);
		assertEquals(Double.NaN, table.getDouble(3, 0), .0000000000001d);
		assertEquals(Double.NaN, table.getDouble(4, 0), .0000000000001d);
	}
	

	@Test
	public void OverrideColumnAttributes() throws Exception {
		
		//Override the column attributes one at a time
		
		coefs = new double[] {1d,1d,1d,1d,1d};
		
		coefColumn = new
			StandardDoubleColumnData(coefs, "myName", "myUnits", "myDesc",
				null, false); 
		

		//
		//Override Name
		
		ColumnAttribsBuilder ca = new ColumnAttribsBuilder();
		ca.setName("NameOR");
		SingleColumnCoefDataTable tab = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("col1Desc", tab.getDescription(1));
		assertEquals("col1Units", tab.getUnits(1));
		assertEquals("col1_val1", tab.getProperty(1, "col1_prop1"));
		
		//
		//Override Description
		ca.setDescription("DescOR");
		tab = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("col1Units", tab.getUnits(1));
		assertEquals("col1_val1", tab.getProperty(1, "col1_prop1"));
		
		//
		//Override Units
		ca.setUnits("UnitsOR");
		tab = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("col1_val1", tab.getProperty(1, "col1_prop1"));
		
		//
		//Override a Property (edit value)
		ca.setProperty("col1_prop1", "col1_val2");
		tab = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("col1_val2", tab.getProperty(1, "col1_prop1"));
		
		//
		//Override a Property (add new value)
		ca.setProperty("col1_prop1", "col1_val2");
		ca.setProperty("col1_prop2", "col1_val99");
		tab = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("col1_val2", tab.getProperty(1, "col1_prop1"));
		assertEquals("col1_val99", tab.getProperty(1, "col1_prop2"));
		
		//
		//Override a Property (set all null)
		ca.setPropertiesNull();
		tab = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("col1_val1", tab.getProperty(1, "col1_prop1"));
		
		//
		//Override a Property (set empty)
		ca.setProperty("col1_prop1", "");
		tab = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("", tab.getProperty(1, "col1_prop1"));
		
		//
		//Override a Property (set single prop null)
		ca.setProperty("col1_prop1", null);
		tab = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1, ca);
		
		assertEquals("NameOR", tab.getName(1));
		assertEquals("DescOR", tab.getDescription(1));
		assertEquals("UnitsOR", tab.getUnits(1));
		assertEquals("col1_val1", tab.getProperty(1, "col1_prop1"));
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorWithTooManyRows() {
		
		//Too many rows
		double[] badCoefs = new double[] {1.1d,2.1d,3.2d,4.1d,5.3d,6.5d};
		
		StandardDoubleColumnData badCoefColumn = new
			StandardDoubleColumnData(badCoefs, "myName", "myUnits", "myDesc",
				null, false); 
		
		table = new SingleColumnCoefDataTable(baseDataTable, badCoefColumn, 1, null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorWithSpecifiedColumnIndexTooBig() {
		table = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 4, null);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorWithColumnIndexTooSmall() {
		table = new SingleColumnCoefDataTable(baseDataTable, coefColumn, -1, null);
	}
	


}
