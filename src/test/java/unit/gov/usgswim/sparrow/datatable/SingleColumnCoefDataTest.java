package gov.usgswim.sparrow.datatable;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;

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
				null, null); 
		
		
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
		rwBaseDataTable.setUnits("BaseUnit", 0);
		
		baseDataTable = rwBaseDataTable.toImmutable();

		table = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 1);

	}

	/**
	 * Only column 1 should be multiplied by the passed coef column.
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
		
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorRowCount() {
		
		double[] badCoefs = new double[] {1.1d,2.1d,3.2d,4.1d,5.3d,6.5d};
		
		StandardDoubleColumnData badCoefColumn = new
			StandardDoubleColumnData(badCoefs, "myName", "myUnits", "myDesc",
				null, null); 
		
		table = new SingleColumnCoefDataTable(baseDataTable, badCoefColumn, 1);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorColumnIndexBig() {
		table = new SingleColumnCoefDataTable(baseDataTable, coefColumn, 4);
	}
	
	@Test (expected = IllegalArgumentException.class)
	public void testBadConstructorColumnIndexSmall() {
		table = new SingleColumnCoefDataTable(baseDataTable, coefColumn, -1);
	}
	


}
