package gov.usgs.cida.datatable.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.usgs.cida.datatable.ColumnDataWritable;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgs.cida.datatable.impl.StandardStringColumnDataWritable;
import static org.junit.Assert.*;

public class SimpleDataTableWritableTest {
	private static int INT_COL_INDEX = 0;
	private static int FLOAT_COL_INDEX = 1;
	private static int STRING_COL_INDEX = 2;
	private static int LONG_COL_INDEX = 3;
	private static int DOUBLE_COL_INDEX = 4;

	ColumnDataWritable intCol;
	ColumnDataWritable floatCol;
	ColumnDataWritable stringCol;
	ColumnDataWritable longCol;
	ColumnDataWritable doubleCol;
	DataTableWritable builder;
	DataTable data;

	@Before public void setUp() throws Exception {
		// configure the DataTable by adding columns to it
		intCol = new StandardNumberColumnDataWritable<Integer>("ints", "attosec");
		floatCol = new StandardNumberColumnDataWritable<Float>("floats", "eV");
		stringCol = new StandardStringColumnDataWritable("name", null);
		longCol = new StandardNumberColumnDataWritable<Long>("longs", "mass");
		doubleCol = new StandardNumberColumnDataWritable<Double>("doubles", "momentum");

		// set column properties
		intCol.setProperty("units description", "pulse time");
		floatCol.setProperty("constants", "mathematical constants");
		stringCol.setProperty("particle", "fundamental particles");
		longCol.setProperty("a", "1");
		longCol.setProperty("b", "2");
		// no properties for double

		// set the column description
		intCol.setDescription("I'm an int");
		floatCol.setDescription("I'm a float");
		stringCol.setDescription("I'm a String");


		// Configure the columns in the builder
		builder = new SimpleDataTableWritable();
		builder.addColumn(intCol)
			.addColumn(floatCol)
			.addColumn(stringCol)
			.addColumn(longCol)
			.addColumn(doubleCol);

		// Set metadata on the original
		builder.setDescription("This is a Data2D");
		builder.setName("TEST_DATA");
		builder.setProperty("size", "6");
	};

	@After public void tearDown() throws Exception {
		intCol = null; floatCol = null; stringCol = null;
		builder = null;
	};

	@Test public void testBuilderAccessToColumnConfiguration() {
		// Verify that builder reads the units correctly
		assertEquals("attosec", builder.getUnits(INT_COL_INDEX));
		assertEquals("eV", builder.getUnits(FLOAT_COL_INDEX));
		assertNull(builder.getUnits(STRING_COL_INDEX));

		// Verify that builder reads the names correctly
		assertEquals("ints", builder.getName(INT_COL_INDEX));
		assertEquals("floats", builder.getName(FLOAT_COL_INDEX));
		assertEquals("name", builder.getName(STRING_COL_INDEX));

		// Verify read column properties
		assertEquals("pulse time", builder.getProperty(INT_COL_INDEX, "units description"));
		assertEquals("mathematical constants", builder.getProperty(FLOAT_COL_INDEX, "constants"));
		assertEquals("fundamental particles", builder.getProperty(STRING_COL_INDEX, "particle"));

		// Verify the column property names
		assertEquals(1, builder.getPropertyNames(INT_COL_INDEX).size());
		assertNotNull(builder.getPropertyNames(INT_COL_INDEX).contains("units description"));
		assertEquals(1, builder.getPropertyNames(FLOAT_COL_INDEX).size());
		assertNotNull(builder.getPropertyNames(FLOAT_COL_INDEX).contains("constants"));
		assertEquals(1, builder.getPropertyNames(STRING_COL_INDEX).size());
		assertNotNull(builder.getPropertyNames(STRING_COL_INDEX).contains("particle"));

		// Verify column descriptions
		assertEquals("I'm an int", builder.getDescription(INT_COL_INDEX));
		assertEquals("I'm a float", builder.getDescription(FLOAT_COL_INDEX));
		assertEquals("I'm a String", builder.getDescription(STRING_COL_INDEX));
	}

	@Test public void testMutableNameUnits() {
		// Verify that builder reads the units correctly
		SimpleDataTableWritable dtw = (SimpleDataTableWritable) builder;
		dtw.setName("changed", INT_COL_INDEX);
		dtw.setName("changed", FLOAT_COL_INDEX);
		dtw.setName("changed", STRING_COL_INDEX);
		dtw.setUnits("changed", INT_COL_INDEX);
		dtw.setUnits("changed", FLOAT_COL_INDEX);
		dtw.setUnits("changed", STRING_COL_INDEX);

		assertEquals("changed", dtw.getUnits(INT_COL_INDEX));
		assertEquals("changed", dtw.getUnits(FLOAT_COL_INDEX));
		assertEquals("changed", dtw.getUnits(STRING_COL_INDEX));

		// Verify that builder reads the names correctly
		assertEquals("changed", dtw.getName(INT_COL_INDEX));
		assertEquals("changed", dtw.getName(FLOAT_COL_INDEX));
		assertEquals("changed", dtw.getName(STRING_COL_INDEX));

		// test changes after toImmutable()
		populateBuilder();
		data = dtw.toImmutable();

		assertEquals("changed", data.getUnits(INT_COL_INDEX));
		assertEquals("changed", data.getUnits(FLOAT_COL_INDEX));
		assertEquals("changed", data.getUnits(STRING_COL_INDEX));

		// Verify that builder reads the names correctly
		assertEquals("changed", data.getName(INT_COL_INDEX));
		assertEquals("changed", data.getName(FLOAT_COL_INDEX));
		assertEquals("changed", data.getName(STRING_COL_INDEX));
	}

	@Test public void testImmutableAccessToColumnConfiguration() {
		populateBuilder();
		data = builder.toImmutable();

		// Verify that builder reads the units correctly
		assertEquals("attosec", data.getUnits(INT_COL_INDEX));
		assertEquals("eV", data.getUnits(FLOAT_COL_INDEX));
		assertNull(data.getUnits(STRING_COL_INDEX));

		// Verify that builder reads the names correctly
		assertEquals("ints", data.getName(INT_COL_INDEX));
		assertEquals("floats", data.getName(FLOAT_COL_INDEX));
		assertEquals("name", data.getName(STRING_COL_INDEX));

		// Verify read column properties
		assertEquals("pulse time", data.getProperty(INT_COL_INDEX, "units description"));
		assertEquals("mathematical constants", data.getProperty(FLOAT_COL_INDEX, "constants"));
		assertEquals("fundamental particles", data.getProperty(STRING_COL_INDEX, "particle"));

		// Verify the column property names
		assertEquals(1, data.getPropertyNames(INT_COL_INDEX).size());
		assertNotNull(data.getPropertyNames(INT_COL_INDEX).contains("units description"));
		assertEquals(1, data.getPropertyNames(FLOAT_COL_INDEX).size());
		assertNotNull(data.getPropertyNames(FLOAT_COL_INDEX).contains("constants"));
		assertEquals(1, data.getPropertyNames(STRING_COL_INDEX).size());
		assertNotNull(data.getPropertyNames(STRING_COL_INDEX).contains("particle"));


		// Verify read column properties
		assertEquals("pulse time", data.getProperty(INT_COL_INDEX, "units description"));
		assertEquals("mathematical constants", data.getProperty(FLOAT_COL_INDEX, "constants"));
		assertEquals("fundamental particles", data.getProperty(STRING_COL_INDEX, "particle"));

		// Verify column descriptions
		assertEquals("I'm an int", data.getDescription(INT_COL_INDEX));
		assertEquals("I'm a float", data.getDescription(FLOAT_COL_INDEX));
		assertEquals("I'm a String", data.getDescription(STRING_COL_INDEX));

	}

	@Test public void testBuilderAccessToColumnData() {
		populateBuilder();

		// check int column data values
		assertEquals("ints", builder.getName(INT_COL_INDEX));
		Integer intValue = Integer.valueOf(10);
		assertEquals(intValue, builder.getValue(0, INT_COL_INDEX));
		assertEquals(intValue, builder.getInt(0, INT_COL_INDEX));

		intValue = Integer.valueOf(11);
		assertEquals(intValue, builder.getValue(1, INT_COL_INDEX));
		assertEquals(intValue, builder.getInt(1, INT_COL_INDEX));

		// check float column data values
		assertEquals("floats", builder.getName(FLOAT_COL_INDEX));
		Float floatValue = Float.valueOf(2.718f);
		assertEquals(floatValue, builder.getValue(0, FLOAT_COL_INDEX));
		assertEquals(floatValue, builder.getFloat(0, FLOAT_COL_INDEX));

		floatValue = Float.valueOf(3.14f);
		assertEquals(floatValue, builder.getValue(1, FLOAT_COL_INDEX));
		assertEquals(floatValue, builder.getFloat(1, FLOAT_COL_INDEX));

		// check string column data values
		assertEquals("name", builder.getName(STRING_COL_INDEX));
		assertEquals("baryons", builder.getValue(0, STRING_COL_INDEX));
		assertEquals("baryons", builder.getString(0, STRING_COL_INDEX));

		assertEquals("leptons", builder.getValue(1, STRING_COL_INDEX));
		assertEquals("leptons", builder.getString(1, STRING_COL_INDEX));

	}
	@Test public void testImmutableAccessToColumnData() {
		populateBuilder();
		data = builder.toImmutable();

		// check int column data values
		assertEquals("ints", data.getName(INT_COL_INDEX));
		Integer intValue = Integer.valueOf(10);
		assertEquals(intValue, data.getValue(0, INT_COL_INDEX));
		assertEquals(intValue, data.getInt(0, INT_COL_INDEX));

		intValue = Integer.valueOf(11);
		assertEquals(intValue, data.getValue(1, INT_COL_INDEX));
		assertEquals(intValue, data.getInt(1, INT_COL_INDEX));

		// check float column data values
		assertEquals("floats", data.getName(FLOAT_COL_INDEX));
		Float floatValue = Float.valueOf(2.718f);
		assertEquals(floatValue, data.getValue(0, FLOAT_COL_INDEX));
		assertEquals(floatValue, data.getFloat(0, FLOAT_COL_INDEX));

		floatValue = Float.valueOf(3.14f);
		assertEquals(floatValue, data.getValue(1, FLOAT_COL_INDEX));
		assertEquals(floatValue, data.getFloat(1, FLOAT_COL_INDEX));

		// check string column data values
		assertEquals("name", data.getName(STRING_COL_INDEX));
		assertEquals("baryons", data.getValue(0, STRING_COL_INDEX));
		assertEquals("baryons", data.getString(0, STRING_COL_INDEX));

		assertEquals("leptons", data.getValue(1, STRING_COL_INDEX));
		assertEquals("leptons", data.getString(1, STRING_COL_INDEX));

	}

	@Test public void testBuilderAccessToColumnTypes() {
		populateBuilder();

		// Verify that builder reads the column types correctly
		assertEquals(Integer.class, builder.getDataType(INT_COL_INDEX));
		assertEquals(Float.class, builder.getDataType(FLOAT_COL_INDEX));
		assertEquals(String.class, builder.getDataType(STRING_COL_INDEX));
	}

	@Test public void testImmutableAccessToColumnTypes() {
		populateBuilder();
		data = builder.toImmutable();

		// Verify that builder reads the column types correctly
		assertEquals(Integer.class, data.getDataType(INT_COL_INDEX));
		assertEquals(Float.class, data.getDataType(FLOAT_COL_INDEX));
		assertEquals(String.class, data.getDataType(STRING_COL_INDEX));
	}
	@Test public void testBuilderMetadata() {
		populateBuilder();

		// Verify metadata
		assertEquals("This is a Data2D", builder.getDescription());
		assertEquals("TEST_DATA", builder.getName());
		assertEquals("6", builder.getProperty("size"));

		assertEquals(5, builder.getColumnCount());
		assertEquals(4, builder.getRowCount());
	}

	@Test public void testImmutableMetadata() {
		populateBuilder();
		data = builder.toImmutable();

		// Verify metadata copied over
		assertEquals("This is a Data2D", data.getDescription());
		assertEquals("TEST_DATA", data.getName());
		assertEquals("6", data.getProperty("size"));

		// Check Data2D metadata
		assertEquals(5, data.getColumnCount());
		assertEquals(4, data.getRowCount());
	}

	@Test public void testColumnsFind() {
		populateBuilder();

		// Check the findmethods on the columns
		assertEquals(0, intCol.findFirst(10));
		assertEquals(2, intCol.findLast(10));
		assertEquals(1, intCol.findFirst(11));
		assertEquals(3, intCol.findLast(11));
		assertEquals(2, intCol.findAll(10).length);
		// test nonexistent values
		assertEquals(-1, intCol.findFirst(-100));
		assertEquals(-1, intCol.findLast(-100));
		assertEquals(0, intCol.findAll(-100).length);

		// Check the findmethods on the columns
		assertEquals(0, floatCol.findFirst(2.718f));
		assertEquals(2, floatCol.findLast(1.618f));
		assertEquals(1, floatCol.findFirst(3.14f));
		assertEquals(3, floatCol.findLast(3.14f));
		assertEquals(2, floatCol.findAll(3.14f).length);
		// test nonexistent values
		assertEquals(-1, floatCol.findFirst(-100f));
		assertEquals(-1, floatCol.findLast(-100f));
		assertEquals(0, floatCol.findAll(-100f).length);

		// Check the findmethods on the columns
		assertEquals(0, stringCol.findFirst("baryons"));
		assertEquals(3, stringCol.findLast("baryons"));
		assertEquals(1, stringCol.findFirst("leptons"));
		assertEquals(1, stringCol.findLast("leptons"));
		assertEquals(3, stringCol.findAll("baryons").length);
		// test nonexistent values
		assertEquals(-1, stringCol.findFirst("does not exist"));
		assertEquals(-1, stringCol.findLast("does not exist"));
		assertEquals(0, stringCol.findAll("does not exist").length);

		// build the index
		assertFalse(stringCol.isIndexed());
		stringCol.buildIndex();
		assertTrue(stringCol.isIndexed());

		// RECHECK THE FIND METHODS

		// Check the findmethods on the Int column
		assertEquals(0, intCol.findFirst(10));
		assertEquals(2, intCol.findLast(10));
		assertEquals(1, intCol.findFirst(11));
		assertEquals(3, intCol.findLast(11));
		assertEquals(2, intCol.findAll(10).length);
		// test nonexistent values
		assertEquals(-1, intCol.findFirst(-100));
		assertEquals(-1, intCol.findLast(-100));
		assertEquals(0, intCol.findAll(-100).length);

		// Check the findmethods on the Float column
		assertEquals(0, floatCol.findFirst(2.718f));
		assertEquals(2, floatCol.findLast(1.618f));
		assertEquals(1, floatCol.findFirst(3.14f));
		assertEquals(3, floatCol.findLast(3.14f));
		assertEquals(2, floatCol.findAll(3.14f).length);
		// test nonexistent values
		assertEquals(-1, floatCol.findFirst(-100f));
		assertEquals(-1, floatCol.findLast(-100f));
		assertEquals(0, floatCol.findAll(-100f).length);

		// Check the findmethods on the String column
		assertEquals(0, stringCol.findFirst("baryons"));
		assertEquals(3, stringCol.findLast("baryons"));
		assertEquals(1, stringCol.findFirst("leptons"));
		assertEquals(1, stringCol.findLast("leptons"));
		assertEquals(3, stringCol.findAll("baryons").length);
		// test nonexistent values
		assertEquals(-1, stringCol.findFirst("does not exist"));
		assertEquals(-1, stringCol.findLast("does not exist"));
		assertEquals(0, stringCol.findAll("does not exist").length);

	}


	@Test public void testBuilderFind() {
		populateBuilder();

		// FIND THROUGH THE DATA2DWRITABLE

		// Check the findmethods on the Int column
		assertEquals(0, builder.findFirst(INT_COL_INDEX, 10));
		assertEquals(2, builder.findLast(INT_COL_INDEX, 10));
		assertEquals(1, builder.findFirst(INT_COL_INDEX, 11));
		assertEquals(3, builder.findLast(INT_COL_INDEX, 11));
		assertEquals(2, builder.findAll(INT_COL_INDEX, 10).length);
		// test nonexistent values
		assertEquals(-1, builder.findFirst(INT_COL_INDEX, -100));
		assertEquals(-1, builder.findLast(INT_COL_INDEX, -100));
		assertEquals(0, builder.findAll(INT_COL_INDEX, -100).length);

		// Check the findmethods on the Float column
		assertEquals(0, builder.findFirst(FLOAT_COL_INDEX, 2.718f));
		assertEquals(2, builder.findLast(FLOAT_COL_INDEX, 1.618f));
		assertEquals(1, builder.findFirst(FLOAT_COL_INDEX, 3.14f));
		assertEquals(3, builder.findLast(FLOAT_COL_INDEX, 3.14f));
		assertEquals(2, builder.findAll(FLOAT_COL_INDEX, 3.14f).length);
		// test nonexistent values
		assertEquals(-1, builder.findFirst(FLOAT_COL_INDEX, -100f));
		assertEquals(-1, builder.findLast(FLOAT_COL_INDEX, -100f));
		assertEquals(0, builder.findAll(FLOAT_COL_INDEX, -100f).length);

		// Check the findmethods on the String column
		assertEquals(0, builder.findFirst(STRING_COL_INDEX, "baryons"));
		assertEquals(3, builder.findLast(STRING_COL_INDEX, "baryons"));
		assertEquals(1, builder.findFirst(STRING_COL_INDEX, "leptons"));
		assertEquals(1, builder.findLast(STRING_COL_INDEX, "leptons"));
		assertEquals(3, builder.findAll(STRING_COL_INDEX, "baryons").length);
		// test nonexistent values
		assertEquals(-1, builder.findFirst(STRING_COL_INDEX, "does not exist"));
		assertEquals(-1, builder.findLast(STRING_COL_INDEX, "does not exist"));
		assertEquals(0, builder.findAll(STRING_COL_INDEX, "does not exist").length);

		// build the index
		assertFalse(builder.isIndexed(INT_COL_INDEX));
		assertFalse(builder.isIndexed(FLOAT_COL_INDEX));
		assertFalse(builder.isIndexed(STRING_COL_INDEX));
		builder.buildIndex(INT_COL_INDEX);
		builder.buildIndex(FLOAT_COL_INDEX);
		builder.buildIndex(STRING_COL_INDEX);
		assertTrue(builder.isIndexed(INT_COL_INDEX));
		assertTrue(builder.isIndexed(FLOAT_COL_INDEX));
		assertTrue(builder.isIndexed(STRING_COL_INDEX));

		// FIND THROUGH THE DATA2DWRITABLE

		// Check the findmethods on the Int column
		assertEquals(0, builder.findFirst(INT_COL_INDEX, 10));
		assertEquals(2, builder.findLast(INT_COL_INDEX, 10));
		assertEquals(1, builder.findFirst(INT_COL_INDEX, 11));
		assertEquals(3, builder.findLast(INT_COL_INDEX, 11));
		assertEquals(2, builder.findAll(INT_COL_INDEX, 10).length);
		// test nonexistent values
		assertEquals(-1, builder.findFirst(INT_COL_INDEX, -100));
		assertEquals(-1, builder.findLast(INT_COL_INDEX, -100));
		assertEquals(0, builder.findAll(INT_COL_INDEX, -100).length);

		// Check the findmethods on the Float column
		assertEquals(0, builder.findFirst(FLOAT_COL_INDEX, 2.718f));
		assertEquals(2, builder.findLast(FLOAT_COL_INDEX, 1.618f));
		assertEquals(1, builder.findFirst(FLOAT_COL_INDEX, 3.14f));
		assertEquals(3, builder.findLast(FLOAT_COL_INDEX, 3.14f));
		assertEquals(2, builder.findAll(FLOAT_COL_INDEX, 3.14f).length);
		// test nonexistent values
		assertEquals(-1, builder.findFirst(FLOAT_COL_INDEX, -100f));
		assertEquals(-1, builder.findLast(FLOAT_COL_INDEX, -100f));
		assertEquals(0, builder.findAll(FLOAT_COL_INDEX, -100f).length);

		// Check the findmethods on the String column
		assertEquals(0, builder.findFirst(STRING_COL_INDEX, "baryons"));
		assertEquals(3, builder.findLast(STRING_COL_INDEX, "baryons"));
		assertEquals(1, builder.findFirst(STRING_COL_INDEX, "leptons"));
		assertEquals(1, builder.findLast(STRING_COL_INDEX, "leptons"));
		assertEquals(3, builder.findAll(STRING_COL_INDEX, "baryons").length);
		// test nonexistent values
		assertEquals(-1, builder.findFirst(STRING_COL_INDEX, "does not exist"));
		assertEquals(-1, builder.findLast(STRING_COL_INDEX, "does not exist"));
		assertEquals(0, builder.findAll(STRING_COL_INDEX, "does not exist").length);
	}

	@Test public void testImmutableFindWithoutIndex() {
		populateBuilder();
		data = builder.toImmutable();

		assertFalse(data.isIndexed(INT_COL_INDEX));
		assertFalse(data.isIndexed(FLOAT_COL_INDEX));
		assertFalse(data.isIndexed(STRING_COL_INDEX));
		// find through the Data2D

		// Check the findmethods on the Int column
		assertEquals(0, data.findFirst(INT_COL_INDEX, 10));
		assertEquals(2, data.findLast(INT_COL_INDEX, 10));
		assertEquals(1, data.findFirst(INT_COL_INDEX, 11));
		assertEquals(3, data.findLast(INT_COL_INDEX, 11));
		assertEquals(2, data.findAll(INT_COL_INDEX, 10).length);
		// test nonexistent values
		assertEquals(-1, data.findFirst(INT_COL_INDEX, -100));
		assertEquals(-1, data.findLast(INT_COL_INDEX, -100));
		assertEquals(0, data.findAll(INT_COL_INDEX, -100).length);

		// Check the findmethods on the Float column
		assertEquals(0, data.findFirst(FLOAT_COL_INDEX, 2.718f));
		assertEquals(2, data.findLast(FLOAT_COL_INDEX, 1.618f));
		assertEquals(1, data.findFirst(FLOAT_COL_INDEX, 3.14f));
		assertEquals(3, data.findLast(FLOAT_COL_INDEX, 3.14f));
		assertEquals(2, data.findAll(FLOAT_COL_INDEX, 3.14f).length);
		// test nonexistent values
		assertEquals(-1, data.findFirst(FLOAT_COL_INDEX, -100f));
		assertEquals(-1, data.findLast(FLOAT_COL_INDEX, -100f));
		assertEquals(0, data.findAll(FLOAT_COL_INDEX, -100f).length);

		// Check the findmethods on the String column
		assertEquals(0, data.findFirst(STRING_COL_INDEX, "baryons"));
		assertEquals(3, data.findLast(STRING_COL_INDEX, "baryons"));
		assertEquals(1, data.findFirst(STRING_COL_INDEX, "leptons"));
		assertEquals(1, data.findLast(STRING_COL_INDEX, "leptons"));
		assertEquals(3, data.findAll(STRING_COL_INDEX, "baryons").length);
		// test nonexistent values
		assertEquals(-1, data.findFirst(STRING_COL_INDEX, "does not exist"));
		assertEquals(-1, data.findLast(STRING_COL_INDEX, "does not exist"));
		assertEquals(0, data.findAll(STRING_COL_INDEX, "does not exist").length);
	}

	@Test public void testImmutableFindWithIndex() {
		populateBuilder();
		builder.buildIndex(INT_COL_INDEX);
		builder.buildIndex(FLOAT_COL_INDEX);
		builder.buildIndex(STRING_COL_INDEX);
		data = builder.toImmutable();

		assertTrue(data.isIndexed(INT_COL_INDEX));
		assertTrue(data.isIndexed(FLOAT_COL_INDEX));
		assertTrue(data.isIndexed(STRING_COL_INDEX));
		// find through the Data2D

		// Check the findmethods on the Int column
		assertEquals(0, data.findFirst(INT_COL_INDEX, 10));
		assertEquals(2, data.findLast(INT_COL_INDEX, 10));
		assertEquals(1, data.findFirst(INT_COL_INDEX, 11));
		assertEquals(3, data.findLast(INT_COL_INDEX, 11));
		assertEquals(2, data.findAll(INT_COL_INDEX, 10).length);
		// test nonexistent values
		assertEquals(-1, data.findFirst(INT_COL_INDEX, -100));
		assertEquals(-1, data.findLast(INT_COL_INDEX, -100));
		assertEquals(0, data.findAll(INT_COL_INDEX, -100).length);

		// Check the findmethods on the Float column
		assertEquals(0, data.findFirst(FLOAT_COL_INDEX, 2.718f));
		assertEquals(2, data.findLast(FLOAT_COL_INDEX, 1.618f));
		assertEquals(1, data.findFirst(FLOAT_COL_INDEX, 3.14f));
		assertEquals(3, data.findLast(FLOAT_COL_INDEX, 3.14f));
		assertEquals(2, data.findAll(FLOAT_COL_INDEX, 3.14f).length);
		// test nonexistent values
		assertEquals(-1, data.findFirst(FLOAT_COL_INDEX, -100f));
		assertEquals(-1, data.findLast(FLOAT_COL_INDEX, -100f));
		assertEquals(0, data.findAll(FLOAT_COL_INDEX, -100f).length);

		// Check the findmethods on the String column
		assertEquals(0, data.findFirst(STRING_COL_INDEX, "baryons"));
		assertEquals(3, data.findLast(STRING_COL_INDEX, "baryons"));
		assertEquals(1, data.findFirst(STRING_COL_INDEX, "leptons"));
		assertEquals(1, data.findLast(STRING_COL_INDEX, "leptons"));
		assertEquals(3, data.findAll(STRING_COL_INDEX, "baryons").length);
		// test nonexistent values
		assertEquals(-1, data.findFirst(STRING_COL_INDEX, "does not exist"));
		assertEquals(-1, data.findLast(STRING_COL_INDEX, "does not exist"));
		assertEquals(0, data.findAll(STRING_COL_INDEX, "does not exist").length);
	}

	@Test public void testColumnMinMaxFunctions() {
		populateBuilder();

		// integer column
		assertEquals(Integer.valueOf(11), intCol.getMaxInt());
		assertEquals(Double.valueOf(11), intCol.getMaxDouble());
		assertEquals(Integer.valueOf(10), intCol.getMinInt());
		assertEquals(Double.valueOf(10), intCol.getMinDouble());

		// float column. Note that float "equality" must be checked via range
		assertEquals(Double.valueOf(3.14), floatCol.getMaxDouble(), .0001);
		assertEquals(Double.valueOf(1.618), floatCol.getMinDouble(), .0001);

		// string columns return null;
		assertNull(stringCol.getMaxInt());
		assertNull(stringCol.getMinInt());
		assertNull(stringCol.getMaxDouble());
		assertNull(stringCol.getMinDouble());

		// long column
		assertEquals(Integer.valueOf(2008), longCol.getMaxInt());
		assertEquals(Double.valueOf(2008), longCol.getMaxDouble());
		assertEquals(Integer.valueOf(1776), longCol.getMinInt());
		assertEquals(Double.valueOf(1776), longCol.getMinDouble());

		// double column. Note that float "equality" must be checked via range
		assertEquals(Double.valueOf(137.036 ), doubleCol.getMaxDouble(), .0001);
		assertEquals(Double.valueOf(1.752), doubleCol.getMinDouble(), .0001);

	}
	@Test public void testBuilderMinMaxFunctions() {
		populateBuilder();

		// integer column
		assertEquals(Integer.valueOf(11), builder.getMaxInt(INT_COL_INDEX));
		assertEquals(Double.valueOf(11), builder.getMaxDouble(INT_COL_INDEX));
		assertEquals(Integer.valueOf(10), builder.getMinInt(INT_COL_INDEX));
		assertEquals(Double.valueOf(10), builder.getMinDouble(INT_COL_INDEX));

		// float column. Note that float "equality" must be checked via range
		assertEquals(Double.valueOf(3.14), builder.getMaxDouble(FLOAT_COL_INDEX), .0001);
		assertEquals(Double.valueOf(1.618), builder.getMinDouble(FLOAT_COL_INDEX), .0001);

		// float column, note that int values are truncated
		assertEquals(Integer.valueOf(3), builder.getMaxInt(FLOAT_COL_INDEX));
		assertEquals(Integer.valueOf(1), builder.getMinInt(FLOAT_COL_INDEX));

		// string columns return null;
		assertNull(builder.getMaxInt(STRING_COL_INDEX));
		assertNull(builder.getMinInt(STRING_COL_INDEX));
		assertNull(builder.getMaxDouble(STRING_COL_INDEX));
		assertNull(builder.getMinDouble(STRING_COL_INDEX));

		// Verify global max mins correct, strings ignored
		assertEquals(Double.valueOf(2008), builder.getMaxDouble());
		assertEquals(Integer.valueOf(2008), builder.getMaxInt());
		assertEquals(Integer.valueOf(1), builder.getMinInt());
		assertEquals(Double.valueOf(1.618), builder.getMinDouble(), .0001);
	}

	@Test public void testImmutableMinMaxFunctions() {
		populateBuilder();
		data = builder.toImmutable();

		// integer column
		assertEquals(Integer.valueOf(11), data.getMaxInt(INT_COL_INDEX));
		assertEquals(Double.valueOf(11), data.getMaxDouble(INT_COL_INDEX));
		assertEquals(Integer.valueOf(10), data.getMinInt(INT_COL_INDEX));
		assertEquals(Double.valueOf(10), data.getMinDouble(INT_COL_INDEX));

		// float column. Note that float "equality" must be checked via range
		assertEquals(Double.valueOf(3.14), data.getMaxDouble(FLOAT_COL_INDEX), .0001);
		assertEquals(Double.valueOf(1.618), data.getMinDouble(FLOAT_COL_INDEX), .0001);
		// float column, note that int values are truncated
		assertEquals(Integer.valueOf(3), data.getMaxInt(FLOAT_COL_INDEX));
		assertEquals(Integer.valueOf(1), data.getMinInt(FLOAT_COL_INDEX));

		// string columns return null;
		assertNull(data.getMaxInt(STRING_COL_INDEX));
		assertNull(data.getMinInt(STRING_COL_INDEX));
		assertNull(data.getMaxDouble(STRING_COL_INDEX));
		assertNull(data.getMinDouble(STRING_COL_INDEX));

		// Verify global max mins correct, strings ignored
		assertEquals(Double.valueOf(2008), data.getMaxDouble());
		assertEquals(Integer.valueOf(2008), data.getMaxInt());
		assertEquals(Integer.valueOf(1), data.getMinInt());
		assertEquals(Double.valueOf(1.618), data.getMinDouble(), .0001);
	}

	@Test public void testToImmutableInvalidation() {
		populateBuilder();
		stringCol.buildIndex();

		// Verify that the original is valid
		assertTrue(intCol.isValid());
		assertTrue(floatCol.isValid());
		assertTrue(stringCol.isValid());
		assertTrue("Check that the builder is valid before making it immutable", builder.isValid());

		DataTable data = builder.toImmutable();
		assertTrue("Check that the data is built in a valid manner", data.isValid());

		// Verify that the original is no longer valid;
		assertFalse(intCol.isValid());
		assertFalse(floatCol.isValid());
		assertFalse(stringCol.isValid());
		assertFalse(builder.isValid());
	}

	/**
	 * Modified test to replicated a null pointer that occurs in SimpleDataTable
	 * when a row id is not found.
	 */
	@Test public void testRowID() {
		populateBuilder();

		Long ROW_0_ID = 40000L;
		Long ROW_1_ID = 30001L;
		Long ROW_2_ID = 20002L;
		Long ROW_3_ID = 10003L;


		assertFalse(builder.hasRowIds());
		builder.setRowId(ROW_0_ID, 0);
		builder.setRowId(ROW_1_ID, 1);
		builder.setRowId(ROW_2_ID, 2);
		builder.setRowId(ROW_3_ID, 3);
		assertTrue(builder.hasRowIds());

		// test getIdForRow()
		assertEquals(ROW_0_ID, builder.getIdForRow(0));
		assertEquals(ROW_1_ID, builder.getIdForRow(1));
		assertEquals(ROW_2_ID, builder.getIdForRow(2));
		assertEquals(ROW_3_ID, builder.getIdForRow(3));

		// test getRowForId()
		assertEquals(0, builder.getRowForId(ROW_0_ID));
		assertEquals(1, builder.getRowForId(ROW_1_ID));
		assertEquals(2, builder.getRowForId(ROW_2_ID));
		assertEquals(3, builder.getRowForId(ROW_3_ID));
		assertEquals(-1, builder.getRowForId(-12345L)); // nonexistent id

		//
		data = builder.toImmutable();
		assertTrue(data.hasRowIds());

		// test getIdForRow()
		assertEquals(ROW_0_ID, data.getIdForRow(0));
		assertEquals(ROW_1_ID, data.getIdForRow(1));
		assertEquals(ROW_2_ID, data.getIdForRow(2));
		assertEquals(ROW_3_ID, data.getIdForRow(3));

		// test getRowForId()
		assertEquals(0, data.getRowForId(ROW_0_ID));
		assertEquals(1, data.getRowForId(ROW_1_ID));
		assertEquals(2, data.getRowForId(ROW_2_ID));
		assertEquals(3, data.getRowForId(ROW_3_ID));
		assertEquals(-1, data.getRowForId(-12345L)); // nonexistent id

	}



	@Test public void testMainUseCase() {
		// -------------------
		// populate the Data2D
		// -------------------
		populateBuilder();
		stringCol.buildIndex();

		// make the Data2D immutable
		assertTrue("Check that the builder is valid before making it immutable", builder.isValid());
		data = builder.toImmutable();
		assertTrue("Check that the data is built in a valid manner", data.isValid());
	}
	
	@Test public void testMovingColumns() {
		
		// -------------------
		// populate the Data2D
		// -------------------
		populateBuilder();

		ColumnDataWritable[] cols = builder.getColumns();
		
		assertEquals(builder.getColumnCount(), cols.length);
		
		//Swap the 0 and 1 column
		ColumnDataWritable old_0 = builder.setColumn(cols[1], 0);
		ColumnDataWritable old_1 = builder.setColumn(cols[0], 1);
		
		
		assertEquals(old_0, builder.getColumns()[1]);
		assertEquals(old_1, builder.getColumns()[0]);
		assertEquals(cols[0], builder.getColumns()[1]);
		assertEquals(cols[1], builder.getColumns()[0]);
		
	}
	
	@Test public void testRemovingColumns() {
		
		// -------------------
		// populate the Data2D
		// -------------------
		populateBuilder();

		ColumnDataWritable[] org_cols = builder.getColumns();
		
		//Remove col 1
		ColumnDataWritable old_1 = builder.removeColumn(1);
		
		ColumnDataWritable[] new_cols = builder.getColumns();
		
		assertEquals(4, builder.getColumnCount());
		
		assertEquals(old_1, org_cols[1]);
		assertEquals(new_cols[0], org_cols[0]);
		assertEquals(new_cols[1], org_cols[2]);
		assertEquals(new_cols[2], org_cols[3]);
		assertEquals(new_cols[3], org_cols[4]);
		
		assertEquals(builder.getName(1), org_cols[2].getName());
		
	}
	
		@Test public void testAccessingValuesFromUnsetRows() {
		
		// -------------------
		// populate the Data2D
		// -------------------
		populateBuilder();

		assertNull(builder.getInt(99, INT_COL_INDEX));
		assertNull(builder.getFloat(99, INT_COL_INDEX));
		assertNull(builder.getDouble(99, INT_COL_INDEX));
		assertNull(builder.getLong(99, INT_COL_INDEX));
		assertNull(builder.getString(99, INT_COL_INDEX));
		assertNull(builder.getValue(99, INT_COL_INDEX));
		
		//Now try setting it
		builder.setValue(new Integer(42), 99, INT_COL_INDEX);
		
		
		assertEquals(new Integer(42), builder.getInt(99, INT_COL_INDEX));
		assertEquals(new Float(42), builder.getFloat(99, INT_COL_INDEX));
		assertEquals(new Double(42), builder.getDouble(99, INT_COL_INDEX));
		assertEquals(new Long(42), builder.getLong(99, INT_COL_INDEX));
		assertEquals("42", builder.getString(99, INT_COL_INDEX));
		assertEquals(new Integer(42), builder.getValue(99, INT_COL_INDEX));
	}

	// =======================
	// private utility methods
	// =======================
	private void populateBuilder() {
		// set the first row
		builder.setValue(10, 0, INT_COL_INDEX);
		builder.setValue(2.718f, 0, FLOAT_COL_INDEX);
		builder.setValue("baryons", 0, STRING_COL_INDEX);
		builder.setValue(1976L, 0, LONG_COL_INDEX);
		builder.setValue(137.036D, 0, DOUBLE_COL_INDEX);

		// set the second row
		builder.setValue(11, 1, INT_COL_INDEX);
		builder.setValue(3.14f, 1, FLOAT_COL_INDEX);
		builder.setValue("leptons", 1, STRING_COL_INDEX);
		builder.setValue(1776L, 1, LONG_COL_INDEX);
		builder.setValue(1.752D, 1, DOUBLE_COL_INDEX);

		// set the third row
		builder.setValue(10, 2, INT_COL_INDEX);
		builder.setValue(1.618f, 2, FLOAT_COL_INDEX);
		builder.setValue("baryons", 2, STRING_COL_INDEX);
		builder.setValue(2008L, 2, LONG_COL_INDEX);
		builder.setValue(6.022D, 2, DOUBLE_COL_INDEX);

		// set the fourth row
		builder.setValue(11, 3, INT_COL_INDEX);
		builder.setValue(3.14f, 3, FLOAT_COL_INDEX);
		builder.setValue("baryons", 3, STRING_COL_INDEX);
		builder.setValue(1812L, 3, LONG_COL_INDEX);
		builder.setValue(137.036D, 3, DOUBLE_COL_INDEX);
	}


}
