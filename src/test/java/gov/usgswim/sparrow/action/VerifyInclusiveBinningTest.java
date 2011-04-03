package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.util.DLUtils;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Before;
import org.junit.Test;

public class VerifyInclusiveBinningTest {
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
	public void testInclusiveBinning() throws Exception{
		SparrowColumnSpecifier tens = new SparrowColumnSpecifier(baseTable, 0, Integer.valueOf(0));
		
		assertEquals(
				"Test inclusive, no boundry cases", 
				"true", 
				(new VerifyInclusiveBinning(
					tens, 
					new String[]{"31", "61", "91"},
					new String[]{"-1", "31", "61"}
				)).run()
			);
		
		assertEquals(
				"Test inclusive, values on boundries", 
				"true", 
				(new VerifyInclusiveBinning(
					tens, 
					new String[]{"30", "60", "90"},
					new String[]{"0", "30", "60"}
				)).run()
			);
			
		
		assertEquals(
				"Test inclusive, one large bin", 
				"true", 
				(new VerifyInclusiveBinning(
					tens, 
					new String[]{"1000"},
					new String[]{"-1000"}
				)).run()
			);
		
		assertEquals(
				"Test exclusive, low value", 
				"false", 
				(new VerifyInclusiveBinning(
					tens, 
					new String[]{"31", "61", "91"},
					new String[]{"1", "31", "61"}
				)).run()
			);
		
		assertEquals(
				"Test exclusive, high value", 
				"false", 
				(new VerifyInclusiveBinning(
					tens, 
					new String[]{"29", "59", "89"},
					new String[]{"-1", "29", "59"}
				)).run()
			);
		
		assertEquals(
				"Test exclusive, value left out in middle", 
				"false", 
				(new VerifyInclusiveBinning(
					tens, 
					new String[]{"49", "100"},
					new String[]{"0", "51"}
				)).run()
			);
	}
}
