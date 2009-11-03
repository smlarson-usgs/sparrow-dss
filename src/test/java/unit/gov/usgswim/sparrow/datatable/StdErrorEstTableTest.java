package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataImm;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StdErrorEstTableTest {

	public DataTable baseDataTable;
	public double[][] baseData;
	public String[] baseHeadings;

	private float uncertaintyArray[][];
	UncertaintyData uncertaintyData;

	@Before
	public void doTestSetup() {

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

		
		//note that uncertainty data is rotated (two columns shown below)
		uncertaintyArray = new float[][] {
				new float[] {1f,  2f,   0f,  -4f, 5f}, /* mean (bias adj) */
				new float[] {.1f, .4f, .6f, .8f, -1f} /* standard error */
			};

		uncertaintyData = new UncertaintyDataImm(uncertaintyArray);

	}

	@Test
	public void stdErrorNullForZeroTest() throws Exception {
		StdErrorEstTable stdErr = 
			new StdErrorEstTable(baseDataTable, uncertaintyData, 0, true, 0);
		
		//The first value
		assertEquals(.1d * (.1f/1f), stdErr.getDouble(0, 0), 0d);
		
		//The second value
		assertEquals(1.1d * (.4f/2f), stdErr.getDouble(1, 0), 0d);
		
		//The 3rd value - a zero value for the mean --> zero coef --> null
		assertNull(stdErr.getDouble(2, 0));
		
		//The last value - zero predicted value
		assertEquals(0d, stdErr.getDouble(4, 0), 0d);
		
		//Headings and other stuff...
		assertEquals("one", stdErr.getName(0));
		assertEquals("BaseTable", stdErr.getName());
		assertEquals("The BaseTable", stdErr.getDescription());
		assertEquals("prop1Value", stdErr.getProperty("prop1"));
		assertEquals(0, stdErr.getRowForId(1L));
		assertEquals(0, stdErr.getColumnByName("one").intValue());
		assertEquals(Double.class, stdErr.getDataType(0));
		assertEquals("Unitless", stdErr.getUnits(0));
		
	}
	


}
