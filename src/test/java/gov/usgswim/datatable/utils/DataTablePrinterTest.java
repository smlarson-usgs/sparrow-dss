package gov.usgswim.datatable.utils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;

import org.junit.Test;

public class DataTablePrinterTest {

	@Test
	public void testPrintDataTableSampleDataTable_HasEqualsDelimiters() throws IOException {
		SimpleDataTableWritable table = DataTableUtilsTest.configureTableDataWithHeaders();

		DataTableWritable result = null;
		result = DataTableUtils.fill(table, DataTableUtilsTest.TABLE_DATA_WITH_HEADERS, false, "\t", true);
		
		StringWriter writer= new StringWriter();
		DataTablePrinter.printDataTableSample(result, 10, 10, writer);
		
		String strResult = writer.toString();
		System.out.println(strResult);
		
		Pattern equalsDelimiter = Pattern.compile("=======");
		Matcher matcher = equalsDelimiter.matcher(strResult);
		
		assertTrue(matcher.find());
		assertTrue(matcher.find());
		assertFalse(matcher.find());
		
	}

}
