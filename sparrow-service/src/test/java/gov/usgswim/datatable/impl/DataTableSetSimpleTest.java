package gov.usgswim.datatable.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableSet;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.datatable.impl.StandardStringColumnDataWritable;
import static org.junit.Assert.*;

public class DataTableSetSimpleTest {

	DataTableSetSimple dataTableSet;

	@Before
	public void setUp() throws Exception {
		
		SimpleDataTableWritable tab1 = buildTable("tab0", "desc0", 1);
		SimpleDataTableWritable tab2 = buildTable("tab1", "desc1", 10);
		SimpleDataTableWritable tab3 = buildTable("tab2", "desc2", 100);
		DataTable.Immutable[] tables = new DataTable.Immutable[] {tab1.toImmutable(), tab2.toImmutable(), tab3.toImmutable()};
		dataTableSet = new DataTableSetSimple(tables, "my set name", "my set desc");
	};
	
	public SimpleDataTableWritable buildTable(String name, String desc, int multiplier) throws Exception {
		
		SimpleDataTableWritable build;
		
		// configure the DataTable by adding columns to it
		ColumnDataWritable col1 = new StandardNumberColumnDataWritable<Integer>("col_name_0_" + multiplier, "attosec");
		col1.setDescription("col_desc_0_" + multiplier);
		col1.setProperty("test_prop", "col_prop_0_" + multiplier);
		ColumnDataWritable col2 = new StandardNumberColumnDataWritable<Integer>("col_name_1_" + multiplier, "attosec");
		col2.setDescription("col_desc_1_" + multiplier);
		col2.setProperty("test_prop", "col_prop_1_" + multiplier);
		
		// Configure the columns in the builder
		build = new SimpleDataTableWritable();
		build.addColumn(col1).addColumn(col2);

		// Set metadata on the original
		build.setDescription(desc);
		build.setName(name);
		build.setProperty("name", name);
		
		
		//Set some values
		build.setValue(0 * multiplier, 0, 0);
		build.setValue(1 * multiplier, 0, 1);
		build.setValue(10 * multiplier, 1, 0);
		build.setValue(11 * multiplier, 1, 1);
		
		return build;
	};

	@Test
	public void testBasic() {
		// Verify basic set props
		assertEquals("my set name", dataTableSet.getName());
		assertEquals("my set desc", dataTableSet.getDescription());
		assertEquals(3, dataTableSet.getTableCount());
		
		assertEquals("tab0", dataTableSet.getTableName(0));
		assertEquals("tab1", dataTableSet.getTableName(1));
		assertEquals("tab2", dataTableSet.getTableName(2));
		assertEquals("desc0", dataTableSet.getTableDescription(0));
		assertEquals("desc1", dataTableSet.getTableDescription(1));
		assertEquals("desc2", dataTableSet.getTableDescription(2));
		
		assertEquals(2, dataTableSet.getRowCount());
		assertEquals(6, dataTableSet.getColumnCount());
		assertTrue(dataTableSet.isValid());
		
		// Verify column props access the columns
		assertEquals("col_name_0_1", dataTableSet.getName(0));
		assertEquals("col_desc_0_1", dataTableSet.getDescription(0));
		assertEquals("col_prop_0_1", dataTableSet.getProperty(0, "test_prop"));
		assertTrue(dataTableSet.isValid(0));
		assertEquals("col_name_1_1", dataTableSet.getName(1));
		assertEquals("col_desc_1_1", dataTableSet.getDescription(1));
		assertEquals("col_prop_1_1", dataTableSet.getProperty(1, "test_prop"));
		assertTrue(dataTableSet.isValid(1));
		
		//Verify value access - row 0
		assertEquals(0, dataTableSet.getInt(0, 0).intValue());
		assertEquals(1, dataTableSet.getInt(0, 1).intValue());
		
		assertEquals(0, dataTableSet.getInt(0, 2).intValue());
		assertEquals(10, dataTableSet.getInt(0, 3).intValue());
		
		assertEquals(0, dataTableSet.getInt(0, 4).intValue());
		assertEquals(100, dataTableSet.getInt(0, 5).intValue());
		
		//Verify value access - row 1
		assertEquals(10, dataTableSet.getInt(1, 0).intValue());
		assertEquals(11, dataTableSet.getInt(1, 1).intValue());
		
		assertEquals(100, dataTableSet.getInt(1, 2).intValue());
		assertEquals(110, dataTableSet.getInt(1, 3).intValue());
		
		assertEquals(1000, dataTableSet.getInt(1, 4).intValue());
		assertEquals(1100, dataTableSet.getInt(1, 5).intValue());
		
				// Verify column props access the columns
		assertEquals("col_name_0_1", dataTableSet.getName(0));
		assertEquals("col_desc_0_1", dataTableSet.getDescription(0));
		assertEquals("col_prop_0_1", dataTableSet.getProperty(0, "test_prop"));
		assertTrue(dataTableSet.isValid(0));
		assertEquals("col_name_1_1", dataTableSet.getName(1));
		assertEquals("col_desc_1_1", dataTableSet.getDescription(1));
		assertEquals("col_prop_1_1", dataTableSet.getProperty(1, "test_prop"));
		assertTrue(dataTableSet.isValid(1));
		assertEquals("col_name_0_10", dataTableSet.getName(2));
		assertEquals("col_desc_0_10", dataTableSet.getDescription(2));
		assertEquals("col_prop_0_10", dataTableSet.getProperty(2, "test_prop"));
		assertTrue(dataTableSet.isValid(2));
		assertEquals("col_name_1_10", dataTableSet.getName(3));
		assertEquals("col_desc_1_10", dataTableSet.getDescription(3));
		assertEquals("col_prop_1_10", dataTableSet.getProperty(3, "test_prop"));
		assertTrue(dataTableSet.isValid(3));
		assertEquals("col_name_0_100", dataTableSet.getName(4));
		assertEquals("col_desc_0_100", dataTableSet.getDescription(4));
		assertEquals("col_prop_0_100", dataTableSet.getProperty(4, "test_prop"));
		assertTrue(dataTableSet.isValid(4));
	}
	
		@Test
	public void testImmutable() {
		DataTableSet immSet = dataTableSet.toImmutable();
		assertTrue(immSet == dataTableSet);

	}


}
