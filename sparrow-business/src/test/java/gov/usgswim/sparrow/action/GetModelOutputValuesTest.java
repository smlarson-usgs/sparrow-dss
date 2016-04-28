package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.HashMapColumnIndex;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import static gov.usgswim.sparrow.action.GetModelOutputValues.getMaxValueforDigits;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author smlarson - brought over from eevermans tests from WriteDbFile (replaced the dbf with Postgres)
 */
public class GetModelOutputValuesTest {
    
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
        
    public GetModelOutputValuesTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
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
    
    @After
    public void tearDown() {
    }

    /**
     * Test of validate method, of class GetModelOutputValues.
     */
    @Test
    public void testValidate() {
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


    /**
     * Test of getRequiredDigitsLeftOfTheDecimal method, of class GetModelOutputValues.
     */
    @Test
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

    /**
     * Test of getRequiredDigitsRightOfTheDecimal method, of class GetModelOutputValues.
     */
    @Test
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

    /**
     * Test of getMaxValueforDigits method, of class GetModelOutputValues.
     */
    @Test
    public void testGetMaxValueforDigits() {
                assertEquals(9d, getMaxValueforDigits(1, 0), comp_err);
		assertEquals(9.9d, getMaxValueforDigits(1, 1), comp_err);
		assertEquals(9.99d, getMaxValueforDigits(1, 2), comp_err);
		assertEquals(.99d, getMaxValueforDigits(0, 2), comp_err);
		assertEquals(0d, getMaxValueforDigits(0, 0), comp_err);
    }
    
}
