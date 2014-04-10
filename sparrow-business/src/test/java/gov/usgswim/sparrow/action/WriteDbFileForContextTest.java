package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.HashMapColumnIndex;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.test.SparrowTestBase;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
public class WriteDbFileForContextTest extends SparrowTestBase {
	
	private final static double comp_err = .001d;
	long[] idArray;
	HashMapColumnIndex columnIndex;
	StandardNumberColumnDataWritable valueCol;

	@Before
	public void setup() {
		
		idArray = new long[] {1, 2, 3};
		columnIndex = new HashMapColumnIndex(idArray);

		//
		valueCol = new StandardNumberColumnDataWritable();
		valueCol.setName("Load");
		valueCol.setValue(10D, 0);
		valueCol.setValue(200D, 1);
		valueCol.setValue(3000D, 2);
	}
	
	
	@Test
	public void writeSimpleValues() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID");

			File resultFile = action.run();
			
			//
			//Test Metadata
			DbaseFileHeader header = readDbfFileHeader(tempFile);
			assertEquals(2, header.getNumFields());
			//Field 0 - the id
			assertEquals("COMID", header.getFieldName(0));
			assertEquals(Integer.class, header.getFieldClass(0));
			assertEquals(9, header.getFieldLength(0));
			assertEquals(0, header.getFieldDecimalCount(0));
			assertEquals('N', header.getFieldType(0));
			//Field 1 - the value
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(14, header.getFieldLength(1));
			assertEquals(4, header.getFieldDecimalCount(1));
			assertEquals('N', header.getFieldType(1));
			
			
			
			//
			//Test Values
			List<Object[]> result = readDbfFile(tempFile);
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);
			
			//Row 0
			assertTrue(result.get(0)[0] instanceof Integer);
			assertEquals(1, result.get(0)[0]);
			assertTrue(result.get(0)[1] instanceof Double);
			assertEquals(10D, ((Double)result.get(0)[1]), comp_err);
			
			//Row 1
			assertTrue(result.get(1)[0] instanceof Integer);
			assertEquals(2, result.get(1)[0]);
			assertTrue(result.get(1)[1] instanceof Double);
			assertEquals(200D, ((Double)result.get(1)[1]), comp_err);
			
			//Row 2
			assertTrue(result.get(2)[0] instanceof Integer);
			assertEquals(3, result.get(2)[0]);
			assertTrue(result.get(2)[1] instanceof Double);
			assertEquals(3000D, ((Double)result.get(2)[1]), comp_err);
		
		} finally {
			tempFile.delete();
		}
	}
	
         @Test
         public void testGetDefaultDataDirectoryWithoutSysprops() throws IOException {
             System.out.println("testGetDefaultDataDirectoryWithoutSysprops");
             WriteDbfFileForContext obj = new WriteDbfFileForContext();
             File result = obj.getDataDirectory();
             String  assertion = System.getProperty("user.home") 
                                + File.separatorChar 
                                + "sparrow"
                                + File.separatorChar
                                + "data";
             assertNotNull(result);
             assertEquals(result.getCanonicalPath(), assertion);
         }
         
        @Test
         public void testGetDefaultDataDirectoryWithSysprops() throws IOException {
             System.out.println("testGetDefaultDataDirectoryWithSysprops");
             String TEST_PATH = "/i/am/a/test/property/file/path";
             SharedApplication.getInstance().getConfiguration().setProperty("geoserver-cache-dir", TEST_PATH);
             WriteDbfFileForContext obj = new WriteDbfFileForContext();
             File result = obj.getDataDirectory();
             String  assertion = TEST_PATH;
             assertNotNull(result);
             assertEquals(result.getCanonicalPath(), assertion);
         }

	@Test
	public void writeAndReadBigValues() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		//////////////123456789
		Long BIG_ID = 999999999L;	//This should be the largest allowed value
		
		idArray[0] = BIG_ID;
		columnIndex = new HashMapColumnIndex(idArray);
		
		///////////////////1234567890.1234
		double BIG_VALUE = 9999999999.9999D;
		valueCol.setValue(BIG_VALUE, 0);	//max 10 digits + 4 decimal places
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID");

			File resultFile = action.run();
			
			List<Object[]> result = readDbfFile(tempFile);
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);
			
			//Row 0
			assertTrue(result.get(0)[0] instanceof Integer);
			assertTrue(isEqual(BIG_ID, result.get(0)[0], comp_err));
			assertTrue(result.get(0)[1] instanceof Double);
			assertTrue(isEqual(BIG_VALUE, result.get(0)[1], comp_err));
			
		
		} finally {
			tempFile.delete();
		}
	}

	@Test
	public void writeAndReadIDThatIsTooLarge() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		//////////////1234567890
		Long BIG_ID = 9999999999L;	//max 9 digits accepted	
		idArray[0] = BIG_ID;
		columnIndex = new HashMapColumnIndex(idArray);
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID");

			File resultFile = action.run();
			

			//Should fail due to too large of an ID value
			assertNull(resultFile);
			
		
		} finally {
			tempFile.delete();
		}
	}
	

	@Test
	public void writeAndReadValueThatIsTooLarge() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		//////////////123456789
		Long BIG_ID = 999999999L;	//This should be the largest allowed value
		idArray[0] = BIG_ID;
		columnIndex = new HashMapColumnIndex(idArray);
		
		///////////////////12345678901.2345
		double BIG_VALUE = 99999999999.9999D;	//TOO BIG!!
		valueCol.setValue(BIG_VALUE, 0);	//max 10 digits + 4 decimal places
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID");

			File resultFile = action.run();

			assertNull(resultFile);
			
		
		} finally {
			tempFile.delete();
		}
	}

	
	@Test
	public void writeAndReadSmallValue() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		
		/////////////////////.1234
		double SMALL_VALUE = .0001D;
		valueCol.setValue(SMALL_VALUE, 0);	//max 10 digits + 4 decimal places
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID");

			File resultFile = action.run();
			
			List<Object[]> result = readDbfFile(tempFile);
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);
			
			//Row 0
			assertTrue(result.get(0)[1] instanceof Double);
			
			double diff = Math.abs(SMALL_VALUE - ((Double)result.get(0)[1]));
			if (diff > .0002D) {
				System.out.println("diff: " + diff);
			}
			assertEquals(SMALL_VALUE, ((Double)result.get(0)[1]), .0002D);
			
		
		} finally {
			tempFile.delete();
		}
	}

	public List<Object[]> readDbfFile(File file) throws Exception {
		FileChannel fc = null;
		FileInputStream fis = null;
		DbaseFileReader dbfReader = null;
		try {
			fis = new FileInputStream(file);
			fc = fis.getChannel();
			dbfReader = new DbaseFileReader(fc, false, Charset.forName("UTF-8"));
		
		
			ArrayList<Object[]> values = new ArrayList<Object[]>();
			

			while (dbfReader.hasNext()) {
				
				Object[] oneRow = dbfReader.readEntry();
				values.add(oneRow);
			}
			
			return values;
		} finally {
			
			try {
				if (dbfReader != null) {
					dbfReader.close();
				}
				if (fc != null) {
					fc.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException iOException) {
				//Ignore - failure to close only
			}
		}
	}
	
	public DbaseFileHeader readDbfFileHeader(File file) throws Exception {
		FileChannel fc = null;
		FileInputStream fis = null;
		DbaseFileReader dbfReader = null;
		try {
			fis = new FileInputStream(file);
			fc = fis.getChannel();
			dbfReader = new DbaseFileReader(fc, false, Charset.forName("UTF-8"));
		
		
			ArrayList<Object[]> values = new ArrayList<Object[]>();
			return dbfReader.getHeader();
			
		} finally {
			
			try {
				if (dbfReader != null) {
					dbfReader.close();
				}
				if (fc != null) {
					fc.close();
				}
				if (fis != null) {
					fis.close();
				}
			} catch (IOException iOException) {
				//Ignore - failure to close only
			}
		}
	}
	
	protected boolean isEqual(Number expected, Number actual, double allowedDiff) {
		
		double diff = Math.abs(expected.doubleValue() - actual.doubleValue());
		if (diff > allowedDiff) {
			System.out.println("Diff Exceeded.  Expected: " + expected.toString() + " found: " + actual.toString() + " (difference of " + diff + ")");
			return false;
		} else {
			return true;
		}
		
		
	}
}
