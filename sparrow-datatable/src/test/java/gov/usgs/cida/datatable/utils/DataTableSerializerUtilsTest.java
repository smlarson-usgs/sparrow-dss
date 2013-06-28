package gov.usgs.cida.datatable.utils;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.BeforeClass;
import org.junit.Test;

import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgs.cida.datatable.impl.StandardStringColumnDataWritable;
import static gov.usgs.cida.datatable.utils.DataTableSerializerUtils.*;
import static org.junit.Assert.*;

public class DataTableSerializerUtilsTest {
	public static final String TAB = "	";

	//	public static void setup() {
	//		DataTableUtilsTest.setupTest();
	//	}
	//
	//	public static void tearDown() {
	//		DataTableUtilsTest.cleanupTest();
	//	}

	public static DataTableWritable setupTestDataTable() {
		// set up table and its properties
		DataTableWritable table = new SimpleDataTableWritable();
		table.setName("TEST TABLE");
		table.setDescription("TEST description");
		table.setProperty("captain", "Kirk");
		table.setProperty("second in command", "Spock");

		// setup 3 table columns
		StandardStringColumnDataWritable strColumn = new StandardStringColumnDataWritable("Str Name", "chars");
		strColumn.setDescription("a column of strings");
		strColumn.setProperty("owner", "Dr. Who");
		strColumn.setProperty("food", "banana split");
		StandardNumberColumnDataWritable<Integer> intColumn = new StandardNumberColumnDataWritable<Integer>("Int rank", "ones");
		StandardNumberColumnDataWritable<Float> floatColumn = new StandardNumberColumnDataWritable<Float>("Floats strength", "decimals");

		table.addColumn(strColumn)
		.addColumn(intColumn)
		.addColumn(floatColumn);

		// populate with data TODO
		table.setValue("Spiderman", 0, 0);
		table.setValue(1, 0, 1);
		table.setValue(5.0, 0, 2);
		table.setValue("Hulk", 1, 0);
		table.setValue(5, 1, 1);
		table.setValue(9.99, 1, 2);

		return table;
	}

	private static DataTableWritable TEST_TABLE;

	@BeforeClass
	public static void initialize() {
		TEST_TABLE = setupTestDataTable();
	}

	@Test public void testSerializeGlobalMetadata() {
		String result = serializeGlobalMetadata(TEST_TABLE);
		assertTrue("table name is contained in metadata", result.contains(TEST_TABLE.getName()));
		assertTrue("table description is contained in metadata", result.contains(TEST_TABLE.getDescription()));
		assertTrue("whether table has ids is contained in metadata", result.contains(Boolean.toString(TEST_TABLE.hasRowIds())));
	}

	@Test public void testSerializeColumnMetadata() {
		String result = serializeColumnMetadata(TEST_TABLE);
		assertTrue("column metadata begins with a TAB", result.startsWith(TAB));
		assertEquals("The leading TAB results in an extra column", TEST_TABLE.getColumnCount(), result.split(TAB).length - 1);

		for (int col=0; col < TEST_TABLE.getColumnCount(); col++) {
			assertTrue("column name is contained in metadata", result.contains(TEST_TABLE.getName(col)));

			String description = "" + TEST_TABLE.getDescription(col);
			assertTrue("column description is contained in metadata", result.contains(description));

			String units = "" + TEST_TABLE.getUnits(col);
			assertTrue("column units are contained in metadata", result.contains(units));

			Class<?> type = null;
			try {
				type = TEST_TABLE.getDataType(col);
			} catch (Exception e) {
				// possible exception when column type is not defined explicitly or implicitly
			}
			String typeValue = (type == null)? "null": type.getCanonicalName();
			assertTrue("column type is contained in metadata", result.contains(typeValue));
		}
	}

	@Test public void testMakeHeaderLine() {
		String result = makeHeaderLine(TEST_TABLE).toString();

		assertEquals("The global metadata result in an extra column", TEST_TABLE.getColumnCount() + 1, result.split(TAB).length);
		assertTrue("The header line begins with the global metadata", result.startsWith(serializeGlobalMetadata(TEST_TABLE)));
	}


	@Test public void testDeserializeFromTextRoundTrip() throws IOException {
		StringBuilder source = serializeToText(TEST_TABLE, new StringBuilder());

		// Now deserialize
		DataTableWritable dataTable = deserializeFromText(source);
		StringBuilder result = serializeToText(dataTable, new StringBuilder());

		assertEquals(source.toString(), result.toString());
	}

	@Test public void testDeserializeFromTextRoundTripWithNulls() throws IOException {
		DataTableWritable orgTable = setupTestDataTable();
		orgTable.setValue((String)null, 0, 0);
		
		StringBuilder orgSerialized = serializeToText(orgTable, new StringBuilder());

		// Now deserialize
		DataTableWritable recreatedTable = deserializeFromText(orgSerialized);
		StringBuilder recreatedTableSerializtion = serializeToText(recreatedTable, new StringBuilder());

		String org = orgSerialized.toString();
		String rec = recreatedTableSerializtion.toString();
		assertEquals(org, rec);
		
		assertNull(recreatedTable.getString(0, 0));
	}

	@Test public void testDifferentSerializationsMatch() throws IOException {
		StringBuilder source = serializeToText(TEST_TABLE, new StringBuilder());

		LoggingPrintStream recorder = new LoggingPrintStream();
		recorder.disable();
		PrintWriter writer = new PrintWriter(recorder);

		serializeToText(TEST_TABLE, writer);
		writer.flush(); // Flushing this is crucial!
		String writerOutput = recorder.getRecord();

		assertEquals(source.toString(), writerOutput);
	}


}
