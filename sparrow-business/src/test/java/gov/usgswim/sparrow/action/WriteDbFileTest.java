package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.HashMapColumnIndex;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import static gov.usgswim.sparrow.action.WriteDbfFile.getMaxValueforDigits;
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
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
@Deprecated 
public class WriteDbFileTest {

	private final static double comp_err = .001d;
	long[] idArray;
	HashMapColumnIndex columnIndex;
	StandardNumberColumnDataWritable valueCol;
	
	/** Column that only returns nulls when double values are requested */
	StandardNumberColumnDataWritable nullCol = new StandardNumberColumnDataWritable() {
		public Double getDouble(int row) { return null; }	
	};
	
	/** Column that only returns nulls when double values are requested */
	StandardNumberColumnDataWritable nanCol = new StandardNumberColumnDataWritable() {
		public Double getDouble(int row) { return Double.NaN; }	
	};
	
	/** Column that only returns nulls when double values are requested */
	StandardNumberColumnDataWritable infiniteCol = new StandardNumberColumnDataWritable() {
		public Double getDouble(int row) { return Double.POSITIVE_INFINITY; }	
	};

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
		
		nullCol.setName("Load");
		nullCol.setValue(0, 2);	//force there to be 3 rows
		
		nanCol.setName("Load");
		nanCol.setValue(0, 2);	//force there to be 3 rows
		
		infiniteCol.setName("Load");
		infiniteCol.setValue(0, 2);	//force there to be 3 rows
	}
	
	//@Test
        @Ignore
	public void testTest() throws Exception {
		//valueCol
		assertEquals("Load", valueCol.getName());
		assertEquals(10D, valueCol.getDouble(0), comp_err);
		assertEquals(200D, valueCol.getDouble(1), comp_err);
		assertEquals(3000D, valueCol.getDouble(2), comp_err);

		//Null col
		assertEquals("Load", nullCol.getName());
		assertNull(nullCol.getDouble(0));
		assertNull(nullCol.getDouble(1));
		assertNull(nullCol.getDouble(2));
		assertEquals(3, nullCol.getRowCount().intValue());
		
		//Nan col
		assertEquals("Load", nanCol.getName());
		assertEquals(Double.NaN, nanCol.getDouble(0), comp_err);
		assertEquals(Double.NaN, nanCol.getDouble(1), comp_err);
		assertEquals(Double.NaN, nanCol.getDouble(2), comp_err);
		assertEquals(3, nanCol.getRowCount().intValue());
		
		//Infinite col
		assertEquals("Load", infiniteCol.getName());
		assertEquals(Double.POSITIVE_INFINITY, infiniteCol.getDouble(0), comp_err);
		assertEquals(Double.POSITIVE_INFINITY, infiniteCol.getDouble(1), comp_err);
		assertEquals(Double.POSITIVE_INFINITY, infiniteCol.getDouble(2), comp_err);
		assertEquals(3, infiniteCol.getRowCount().intValue());
	}
	
	
	//@Test
        @Ignore
	public void testGetRequiredDigitsLeftOfTheDecimal() {
		assertEquals(4, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(1000d, 0d, 10));
		assertEquals(4, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(-1000d, 0d, 10));
		assertEquals(5, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(-1000d, -10000d, 10));
		assertEquals(6, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(100000d, -10000d, 10));
		assertEquals(6, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(999999d, -10000d, 10));
		assertEquals(7, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(999999d, -9999999d, 10));
		
		//Values that are very very close to the next ten's place do end up rounding up
		//for some reason.  Not really a problem to have too many digits to the right of
		//the decimal, though we do potentially lose a bit of precision.
		assertEquals(11, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(99999999999.0D, 0d, 6));
		assertEquals(11, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(99999999999.5D, 0d, 6));
		assertEquals(11, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(99999999999.999D, 0d, 6));
		assertEquals(12, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(99999999999.9999D, 0d, 6));

		
		//zero and fractional
		assertEquals(1, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(0d, 0d, 10));
		assertEquals(1, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(.2d, 0d, 10));
		assertEquals(1, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(0d, -.999999d, 10));
		assertEquals(1, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(1d, -.999999d, 10));
		
		//Edge case values
		assertEquals(1, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(Double.NaN, Double.NaN, 10));
		assertEquals(1, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(null, null, 10));
		assertEquals(30, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(Double.POSITIVE_INFINITY, 0d, 10));
		assertEquals(30, WriteDbfFile.getRequiredDigitsLeftOfTheDecimal(null, Double.NEGATIVE_INFINITY, 10));
	}
	
	//@Test
        @Ignore
	public void testGetRequiredDigitsRightOfTheDecimal() {
		
		//Have some decimal places available
		assertEquals(1, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(9, 10, 0));
		assertEquals(2, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(8, 10, 0));
		assertEquals(5, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(9, 10, 5));
		assertEquals(5, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(8, 10, 5));
		
		//No places available
		assertEquals(0, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(10, 10, 0));
		assertEquals(0, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(11, 10, 0));
		assertEquals(0, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(99, 10, 0));
		assertEquals(1, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(10, 10, 1));
		assertEquals(1, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(11, 10, 1));
		assertEquals(1, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(99, 10, 1));
		assertEquals(5, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(11, 16, 1));
		
		//Edge cases
		assertEquals(10, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(0, 10, 1));
		assertEquals(11, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(0, 10, 11));
		assertEquals(0, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(0, 0, 0));
		assertEquals(1, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(0, 0, 1));
		assertEquals(0, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(1, 0, 0));
		assertEquals(1, WriteDbfFile.getRequiredDigitsRightOfTheDecimal(1, 0, 1));
	}
	
	//@Test
        @Ignore
	public void getMaxValueforDigitsTest() {
		assertEquals(9d, getMaxValueforDigits(1, 0), comp_err);
		assertEquals(9.9d, getMaxValueforDigits(1, 1), comp_err);
		assertEquals(9.99d, getMaxValueforDigits(1, 2), comp_err);
		assertEquals(.99d, getMaxValueforDigits(0, 2), comp_err);
		assertEquals(0d, getMaxValueforDigits(0, 0), comp_err);
	}
	
	
	//@Test
        @Ignore
	public void writeSimpleValues() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID",
				null);

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
			//Max value is 4 digits and the default sigfigs is 6, so there
			//should be 6 digits available w/ 2 decimal places
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(6, header.getFieldLength(1));
			assertEquals(2, header.getFieldDecimalCount(1));
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
	

	//@Test
        @Ignore
	public void writeAndReadBigValues() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		//////////////123456789
		Long BIG_ID = 999999999L;	//This should be the largest allowed value
		
		idArray[0] = BIG_ID;
		columnIndex = new HashMapColumnIndex(idArray);
		
		///////////////////1234567890.1234
		double BIG_VALUE = 9999999999.9999D;
		valueCol.setValue(BIG_VALUE, 0);
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID",
				null);

			File resultFile = action.run();
			
			List<Object[]> result = readDbfFile(tempFile);
			
			//Test Metadata
			//Field 1 - the value
			//Max value is 10 integer digits and the default (minimum) sigfigs is 6, so there
			//should be 11 digits available w/ 1 decimal places (b/c 1 decimal place is a minimum)
			DbaseFileHeader header = readDbfFileHeader(tempFile);
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(11, header.getFieldLength(1));
			assertEquals(1, header.getFieldDecimalCount(1));
			assertEquals('N', header.getFieldType(1));
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);
			
			//Row 0
			assertTrue(result.get(0)[0] instanceof Integer);
			assertTrue(isEqual(BIG_ID, (Integer)result.get(0)[0], 1));
			assertTrue(result.get(0)[1] instanceof Double);
			assertTrue(isEqual(BIG_VALUE, (Double)result.get(0)[1], 1));
			
		
		} finally {
			tempFile.delete();
		}
	}

	
	//@Test
        @Ignore
	public void writeAndReadBigValues2() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		//////////////123456789
		Long BIG_ID = 999999999L;	//This should be the largest allowed value
		
		idArray[0] = BIG_ID;
		columnIndex = new HashMapColumnIndex(idArray);
		
		///////////////////12345678901.2345
		double BIG_VALUE = 99999999999.9999D;
		valueCol.setValue(BIG_VALUE, 0);
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID",
				null);

			File resultFile = action.run();
			
			List<Object[]> result = readDbfFile(tempFile);
			
			//Test Metadata
			//Field 1 - the value
			//Max value has 11 integer digits and the default (minimum) sigfigs is 6, so there
			//should be 12 digits available w/ one decimal place (one is the minimum).
			//HOWEVER, this is a place where the digits get rounded up, so 13 digits.
			DbaseFileHeader header = readDbfFileHeader(tempFile);
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(13, header.getFieldLength(1));
			assertEquals(1, header.getFieldDecimalCount(1));
			assertEquals('N', header.getFieldType(1));
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);
			
			//Row 0
			assertTrue(result.get(0)[0] instanceof Integer);
			assertTrue(isEqual(BIG_ID, (Integer)result.get(0)[0], comp_err));
			assertTrue(result.get(0)[1] instanceof Double);
			assertTrue(isEqual(BIG_VALUE, (Double)result.get(0)[1], comp_err));
			
		
		} finally {
			tempFile.delete();
		}
	}
	

	
	//@Test
        @Ignore
	public void writeAndReadSmallValues() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		
		/////////////////////.1234
		double SMALL_VALUE = .0001D;
		valueCol.setValue(SMALL_VALUE, 0);
		valueCol.setValue(SMALL_VALUE, 1);	
		valueCol.setValue(SMALL_VALUE, 2);
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID",
				null);

			File resultFile = action.run();
			
			//Test Metadata
			//Field 1 - the value
			//Max value has 4 decimal digits and zero integer.  The default (minimum) sigfigs is 6, so there
			//should be 6 digits available, one used for integer (the minimum), leaving 5 decimal places
			DbaseFileHeader header = readDbfFileHeader(tempFile);
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(6, header.getFieldLength(1));
			assertEquals(5, header.getFieldDecimalCount(1));
			assertEquals('N', header.getFieldType(1));
			
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
	

	//@Test
        @Ignore
	public void writeAndReadZeroValues() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");

		valueCol.setValue(0, 0);
		valueCol.setValue(0, 1);	
		valueCol.setValue(0, 2);
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				valueCol,
				tempFile,
				"COMID",
				null);

			File resultFile = action.run();
			
			//Test Metadata
			//Field 1 - the value
			//Max value has 0 digits and zero integer.  The default (minimum) sigfigs is 6, so there
			//should be 6 digits available, one reserver for integer (the minimum), leaving 5 decimal places
			DbaseFileHeader header = readDbfFileHeader(tempFile);
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(6, header.getFieldLength(1));
			assertEquals(5, header.getFieldDecimalCount(1));
			assertEquals('N', header.getFieldType(1));
			
			List<Object[]> result = readDbfFile(tempFile);
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);

		
		} finally {
			tempFile.delete();
		}
	}
	
	//@Test
        @Ignore
	public void writeAndReadNullValue() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				nullCol,
				tempFile,
				"COMID",
				null);

			File resultFile = action.run();
			
			//Test Metadata
			//Field 1 - the value
			//Max value is null.  The default (minimum) sigfigs is 6, so there
			//should be 1 integer digit (the min) w/ d decimal places
			DbaseFileHeader header = readDbfFileHeader(tempFile);
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(6, header.getFieldLength(1));
			assertEquals(5, header.getFieldDecimalCount(1));
			assertEquals('N', header.getFieldType(1));
			
			List<Object[]> result = readDbfFile(tempFile);
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);
			
			assertNull(result.get(0)[1]);
			assertNull(result.get(1)[1]);
			assertNull(result.get(2)[1]);
			
			
		
		} finally {
			tempFile.delete();
		}
	}
	
	/**
	 * Note:  The DBF lib converts NaN values to Null, so that is codified here.
	 * @throws Exception 
	 */
	//@Test
        @Ignore
	public void writeAndReadNaNValue() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				nanCol,
				tempFile,
				"COMID",
				null);

			File resultFile = action.run();
			
			//Test Metadata
			//Field 1 - the value
			//Max value is NaN.  The default (minimum) sigfigs is 6, so there
			//should be 1 integer digit (the min) w/ 5 decimal places
			DbaseFileHeader header = readDbfFileHeader(tempFile);
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(6, header.getFieldLength(1));
			assertEquals(5, header.getFieldDecimalCount(1));
			assertEquals('N', header.getFieldType(1));
			
			
			List<Object[]> result = readDbfFile(tempFile);
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);
			
			
			assertNull(result.get(0)[1]);
			assertNull(result.get(1)[1]);
			assertNull(result.get(2)[1]);
			
		
		} finally {
			tempFile.delete();
		}
	}
	

	/**
	 * @throws Exception 
	 */
	//@Test
        @Ignore
	public void writeAndReadInfiniteValues() throws Exception {
		
		File tempFile = File.createTempFile("predictExport", ".dbf");
		
		
		try {
			//PredictResult predictResult = getTestModelPredictResult();

			tempFile.deleteOnExit();

			WriteDbfFile action = new WriteDbfFile(
				columnIndex,
				infiniteCol,
				tempFile,
				"COMID",
				null);

			File resultFile = action.run();
			
			//Test Metadata
			//Field 1 - the value
			//Max value is Infinite.  By default we do 3x the sigfigs (6x3), so 18 + 1 decimal place.
			DbaseFileHeader header = readDbfFileHeader(tempFile);
			assertEquals("VALUE", header.getFieldName(1));
			assertEquals(Double.class, header.getFieldClass(1));
			assertEquals(19, header.getFieldLength(1));
			assertEquals(1, header.getFieldDecimalCount(1));
			assertEquals('N', header.getFieldType(1));
			
			
			List<Object[]> result = readDbfFile(tempFile);
			
			assertEquals(3, result.size());
			assertEquals(2, result.get(0).length);
			
			//This should be kicking back the max value, which for this
			//type of column should be
			Double MAX_VALUE = 999999999999999999.9d;
			//_________________123456789012345678.1
			
			assertEquals(MAX_VALUE, (Double)result.get(0)[1], comp_err);
			assertEquals(MAX_VALUE, (Double)result.get(1)[1], comp_err);
			assertEquals(MAX_VALUE, (Double)result.get(2)[1], comp_err);
			
		
		} finally {
			tempFile.delete();
		}
	}
	
	
	//@Test
        @Ignore
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
				"COMID",
				null);

			File resultFile = action.run();
			

			//Should fail due to too large of an ID value
			assertNull(resultFile);
			
		
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
