package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.util.DLUtils;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

public class VerifyBinningResolutionTest {
	DataTableWritable baseTable;
	
	@Before
	public void doInit() throws Exception {
		InputStream fileStream =
			getClass().getResourceAsStream("/gov/usgswim/sparrow/tab_delimit_sample_data.txt");

		BufferedReader br = new BufferedReader(new InputStreamReader(
				fileStream
			));
		
		baseTable = TabDelimFileUtil.readAsDouble(br,
				true, DLUtils.DO_NOT_INDEX);
		
		//build row IDs matching the row index
		for (int i=0; i< baseTable.getRowCount(); i++) {
			baseTable.setRowId(i, i);
		}
		

	}
	
	@Test
	public void testBinningResolution() throws Exception{
		SparrowColumnSpecifier tens = new SparrowColumnSpecifier(baseTable, 0, Integer.valueOf(0));
		
		assertEquals(
				"Test all values in one bin", 
				Boolean.FALSE, 
				(new VerifyBinningResolution(
					tens, 
					new String[]{"100", "200", "300"},
					new String[]{"-1", "101", "201"}
				)).run()
			);
		
		assertEquals(
				"Test all values in one bin, but only 2 bins", 
				Boolean.TRUE, 
				(new VerifyBinningResolution(
					tens, 
					new String[]{"100", "200"},
					new String[]{"-1", "101"}
				)).run()
			);
	
		assertEquals(
				"Test values spread accross all 3 bins", 
				Boolean.TRUE, 
				(new VerifyBinningResolution(
					tens, 
					new String[]{"31", "61", "91"},
					new String[]{"1", "31", "61"}
				)).run()
			);
	}
}
