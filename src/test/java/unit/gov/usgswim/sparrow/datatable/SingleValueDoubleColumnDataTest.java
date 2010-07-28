package gov.usgswim.sparrow.datatable;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;

import org.junit.Before;
import org.junit.Test;

public class SingleValueDoubleColumnDataTest {

	//double comparison error
	static final double COMP = .000000000001d;
	
	//The double value for all row values
	static final Double VAL = new Double(.0011198d);
	
	SingleValueDoubleColumnData col1;
	SingleValueDoubleColumnData col2;

	@Before
	public void doTestSetup() {

		ColumnAttribsBuilder ca = new ColumnAttribsBuilder();
		ca.setName("name");
		ca.setDescription("desc");
		ca.setUnits("no_unit");
		
		col1 =
			new SingleValueDoubleColumnData(VAL, 10, ca);

		col2 =
			new SingleValueDoubleColumnData(VAL, 10, "name", "no_unit", "desc", null);
	}


	@Test
	public void basicTest() throws Exception {
		assertEquals(new Integer(10), col1.getRowCount());
		assertEquals(VAL, col1.getDouble(0), COMP);
		assertEquals(VAL, col1.getDouble(9), COMP);
		assertEquals("name", col1.getName());
		assertEquals("desc", col1.getDescription());
		assertEquals("no_unit", col1.getUnits());
		
		assertEquals(new Integer(10), col2.getRowCount());
		assertEquals(VAL, col2.getDouble(0), COMP);
		assertEquals(VAL, col2.getDouble(9), COMP);
		assertEquals("name", col2.getName());
		assertEquals("desc", col2.getDescription());
		assertEquals("no_unit", col2.getUnits());
	}
	
}