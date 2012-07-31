package gov.usgswim.datatable.impl;

import org.junit.Before;
import org.junit.Test;

import gov.usgswim.datatable.*;
import static org.junit.Assert.*;

public class DataTableSetWritableSimpleTest {

	DataTableSetWritableSimple dataTableSet;

	@Before
	public void setUp() throws Exception {
		
		SimpleDataTableWritable tab1 = buildTable("tab1", "tab1 desc", 1);
		SimpleDataTableWritable tab2 = buildTable("tab2", "tab2 desc", 10);
		SimpleDataTableWritable tab3 = buildTable("tab3", "tab3 desc", 100);

		dataTableSet = new DataTableSetWritableSimple("my set name", "my set desc");
		dataTableSet.addTable(tab1);
		dataTableSet.addTable(tab2);
		dataTableSet.addTable(tab3);
	};
	
	public SimpleDataTableWritable buildTable(String name, String desc, int multiplier) throws Exception {
		
		SimpleDataTableWritable build;
		
		// configure the DataTable by adding columns to it
		ColumnDataWritable col1 = new StandardNumberColumnDataWritable<Integer>("ints", "attosec");
		ColumnDataWritable col2 = new StandardNumberColumnDataWritable<Integer>("ints", "attosec");

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
		assertEquals(2, dataTableSet.getRowCount());
		assertEquals(6, dataTableSet.getColumnCount());
		assertTrue(dataTableSet.isValid());
		
		// Verify Table access and props
		assertEquals("tab1", dataTableSet.getName(0));
		assertEquals("tab1 desc", dataTableSet.getDescription(0));
		assertTrue(dataTableSet.isValid(0));
		assertEquals("tab2", dataTableSet.getName(1));
		assertEquals("tab2 desc", dataTableSet.getDescription(1));
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
	}


	@Test
	public void testImmutable() {
		DataTableSet immSet = dataTableSet.toImmutable();
		assertTrue(immSet != dataTableSet);

	}
}
