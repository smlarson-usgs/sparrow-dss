package gov.usgswim.sparrow.datatable;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.domain.ComparisonType;

import org.junit.*;
import static org.junit.Assert.assertEquals;

public class DataTableCompareTest {

	public DataTable baseDataTable;
	public double[][] baseData;
	public String[] baseHeadings;

	public DataTable compareDataTable;
	public double[][] compareData;
	public String[] compareHeadings;

	@Before
	public void doTestSetup() {

		baseHeadings = new String[] { "one", "two", "three", "four" };
		baseData = new double[][] {
				{ .1, .2, .3, .4 },
				{ 1.1, 1.2, 1.3, 1.4 },
				{ 0, 0, 0, 0 },
				{ 0, 0, 3.3, 3.4 },
				{ 4.1, 4.2, 4.3, 4.4 }
		};
		int[] baseRowIds = new int[] {1, 2, 3, 4, 5};
		SimpleDataTableWritable rwBaseDataTable = 
			new SimpleDataTableWritable(baseData, baseHeadings, baseRowIds);
		rwBaseDataTable.setName("BaseTable");
		rwBaseDataTable.setDescription("The BaseTable");
		rwBaseDataTable.setProperty("prop1", "prop1Value");
		rwBaseDataTable.setUnits("BaseUnit", 0);
		
		baseDataTable = rwBaseDataTable.toImmutable();

		compareHeadings = new String[] { "c_one", "c_two", "c_three", "c_four" };
		compareData = new double[][] {
			{ .2, .2, .3, .4 }, /* 1st value doubled */
			{ 1.1, 1.2, 1.3, 1.4 },
			{ 0, 0, 2.3, 2.4 }, /* 1st two zeroed */
			{ 0, 0, 0, 0 }, /* row zeroed */
			{ 4.1, 4.2, 4.3, 2.2 } /* last value halved */
		};
		SimpleDataTableWritable rwCompareDataTable = new SimpleDataTableWritable(compareData,
				compareHeadings);
		rwCompareDataTable.setUnits("CompareUnit", 0);
		compareDataTable = rwCompareDataTable.toImmutable();

	}

	@Test
	public void compareAbsoluteTest() throws Exception {
		DataTableCompare comp = new DataTableCompare(baseDataTable,
				compareDataTable, ComparisonType.absolute);
		
		//Comparing some of the 'interesting' values
		//Row 0
		assertEquals(.1d, comp.getDouble(0, 0), 0d);
		assertEquals(0d, comp.getDouble(0, 3), 0d);
		
		
		//Row 2
		assertEquals(0d, comp.getDouble(2, 0), 0d);
		assertEquals(2.4d, comp.getDouble(2, 3), 0d);
		
		//Row 3
		assertEquals(0d, comp.getDouble(3, 0), 0d);
		assertEquals(-3.4d, comp.getDouble(3, 3), 0d);
		
		//Row 4
		assertEquals(0d, comp.getDouble(4, 0), 0d);
		assertEquals(-2.2d, comp.getDouble(4, 3), 0d);
		
		//Max values
		assertEquals(.1d, comp.getMaxDouble(0), .000000000001d);
		assertEquals(2.4d, comp.getMaxDouble(3), .000000000001d);
		
		//Headings and other stuff...
		assertEquals("one", comp.getName(0));
		assertEquals("BaseTable", comp.getName());
		assertEquals("The BaseTable", comp.getDescription());
		assertEquals("prop1Value", comp.getProperty("prop1"));
		assertEquals(0, comp.getRowForId(1L));
		assertEquals(0, comp.getColumnByName("one").intValue());
		assertEquals(Double.class, comp.getDataType(0));
		assertEquals("BaseUnit", comp.getUnits(0));
		
	}
	
	@Test
	public void comparePercentageChangeTest() throws Exception {
		DataTableCompare comp = new DataTableCompare(baseDataTable,
				compareDataTable, ComparisonType.percent_change);
		
		//Comparing some of the 'interesting' values
		//Row 0
		assertEquals(100d, comp.getDouble(0, 0), 0d);
		assertEquals(0d, comp.getDouble(0, 3), 0d);
		
		//Row 2
		assertEquals(0d, comp.getDouble(2, 0), 0d);
		assertEquals(100d, comp.getDouble(2, 3), 0d); //Increase from 0 --> 100%
		
		//Row 3
		assertEquals(0d, comp.getDouble(3, 0), 0d);
		assertEquals(-100d, comp.getDouble(3, 3), 0d); //Decrease from 0 --> -100%
		
		//Row 4
		assertEquals(0d, comp.getDouble(4, 0), 0d);
		assertEquals(-50d, comp.getDouble(4, 3), 0d);
		
		//Headings and other stuff...
		assertEquals("one", comp.getName(0));
		assertEquals("BaseTable", comp.getName());
		assertEquals("The BaseTable", comp.getDescription());
		assertEquals("prop1Value", comp.getProperty("prop1"));
		assertEquals(0, comp.getRowForId(1L));
		assertEquals(0, comp.getColumnByName("one").intValue());
		assertEquals(Double.class, comp.getDataType(0));
		assertEquals("Percentage", comp.getUnits(0));
		
	}
	
	@Test
	public void comparePercentageTest() throws Exception {
		DataTableCompare comp = new DataTableCompare(baseDataTable,
				compareDataTable, ComparisonType.percent);
		
		//Comparing some of the 'interesting' values
		//Row 0
		assertEquals(200d, comp.getDouble(0, 0), 0d);
		assertEquals(100d, comp.getDouble(0, 3), 0d);
		
		//Row 2
		assertEquals(0d, comp.getDouble(2, 0), 0d);
		assertEquals(100d, comp.getDouble(2, 3), 0d); //Increase from 0 --> 100%
		
		//Row 3
		assertEquals(0d, comp.getDouble(3, 0), 0d);
		assertEquals(0d, comp.getDouble(3, 3), 0d); //Decrease from 0 --> -100%
		
		//Row 4
		assertEquals(100d, comp.getDouble(4, 0), 0d);
		assertEquals(50d, comp.getDouble(4, 3), 0d);
		
		//Headings and other stuff...
		assertEquals("one", comp.getName(0));
		assertEquals("BaseTable", comp.getName());
		assertEquals("The BaseTable", comp.getDescription());
		assertEquals("prop1Value", comp.getProperty("prop1"));
		assertEquals(0, comp.getRowForId(1L));
		assertEquals(0, comp.getColumnByName("one").intValue());
		assertEquals(Double.class, comp.getDataType(0));
		assertEquals("Percentage", comp.getUnits(0));
		
	}
	
	@Test
	public void compareMixedColumns() throws Exception {
		String[] headings = new String[] {
				"ZERO",	//DOUBLE
				"ONE"	//STRING
		};
		
		Class<?>[] types= {
				Double.class,
				String.class
				};
		DataTableWritable base =
			new SimpleDataTableWritable(headings, null, types);
		base.setValue(0.5d, 0, 0);
		base.setValue("test_0", 0, 1);
		base.setValue(1.5d, 1, 0);
		base.setValue("test_1", 1, 1);
		base.setValue(2.5d, 2, 0);
		base.setValue((String)null, 2, 1);
		base.setValue(3.5d, 3, 0);
		base.setValue("test_3", 3, 1);
		
		DataTableWritable compareTo =
			new SimpleDataTableWritable(headings, null, types);
		compareTo.setValue(0.5d, 0, 0);
		compareTo.setValue("test_0", 0, 1);
		compareTo.setValue(1.4d, 1, 0);
		compareTo.setValue("test_1asdf", 1, 1);
		compareTo.setValue(2.6d, 2, 0);
		compareTo.setValue("test_2", 2, 1);
		compareTo.setValue(3.5d, 3, 0);
		compareTo.setValue("", 3, 1);
		
		
		DataTableCompare comp = new DataTableCompare(base,
				compareTo, ComparisonType.absolute);
		
		assertEquals(0d, comp.getDouble(0, 1), 0.000000000000001d);
		assertEquals(4d, comp.getDouble(1, 1), 0.000000000000001d);
		assertEquals(6d, comp.getDouble(2, 1), 0.000000000000001d);
		assertEquals(6d, comp.getDouble(3, 1), 0.000000000000001d);
	}

}
