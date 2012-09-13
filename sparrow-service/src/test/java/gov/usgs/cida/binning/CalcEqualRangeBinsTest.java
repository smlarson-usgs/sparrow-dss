package gov.usgs.cida.binning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.binning.domain.Bin;
import gov.usgs.cida.binning.domain.BinSet;
import gov.usgs.cida.binning.domain.BinType;
import gov.usgs.cida.binning.domain.InProcessBinSet;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.impl.SimpleDataTable;
import gov.usgs.cida.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.action.DeliveryReach;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.ComparisonType;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgswim.sparrow.service.ServiceResponseOperation;
import gov.usgswim.sparrow.service.ServiceResponseStatus;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.ServletResponseParser;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

public class CalcEqualRangeBinsTest extends SparrowTestBase {

	//Basic values used in tests
	static BigDecimal TEN = new BigDecimal("10").stripTrailingZeros();
	static BigDecimal ONE_HUNDRED = new BigDecimal("100").stripTrailingZeros();
	static BigDecimal DOT_1 = new BigDecimal(".1").stripTrailingZeros();
	static BigDecimal DOT_01 = new BigDecimal(".01").stripTrailingZeros();
	static BigDecimal DOT_02 = new BigDecimal(".02").stripTrailingZeros();
	static BigDecimal DOT_001 = new BigDecimal(".001").stripTrailingZeros();
	
	//Datasets used in tests
	SparrowColumnSpecifier zeroTo50in50ValuesWithTopOverage;	//the top value is 50.00000000001
	SparrowColumnSpecifier zeroTo50in50ValuesWithInfiniteTop;	//Top is infinite
	SparrowColumnSpecifier zeroToNeg50in50ValuesWithTopOverage;	//the top value is 0.00000000001
	
	SparrowColumnSpecifier zeroTo50in50ValuesWithBtmOverage;	//the btm value is -0.00000000001
	SparrowColumnSpecifier zeroToNeg50in50ValuesWithBtmOverage;	//the top value is -50.00000000001
	
	SparrowColumnSpecifier smallValuesWithTopOverage;	//the top value is .00030000001
	SparrowColumnSpecifier sameSame;	//all 99
	
	SparrowColumnSpecifier zeroTo10In10Values;	//1-10, but all the values are at the bottom
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
	}
	
	@Before
	public void init() {
		{
			double[] data = new double[51];
			for (int d = 0; d < 51; d++) {
				data[d] = d;
			}
			data[50] = 50.00000000000001d;
			
			ColumnData cd = new StandardDoubleColumnData(data, "test", "unit",
					"desc",	null, false);
			SimpleDataTable table = new SimpleDataTable(new ColumnData[] {cd});
			zeroTo50in50ValuesWithTopOverage = new SparrowColumnSpecifier(table, 0, null);
		}
		
		{
			double[] data = new double[51];
			for (int d = 0; d < 51; d++) {
				data[d] = d;
			}
			data[50] = Double.POSITIVE_INFINITY;
			
			ColumnData cd = new StandardDoubleColumnData(data, "test", "unit",
					"desc",	null, false);
			SimpleDataTable table = new SimpleDataTable(new ColumnData[] {cd});
			zeroTo50in50ValuesWithInfiniteTop = new SparrowColumnSpecifier(table, 0, null);
		}
		
		{
			double[] data = new double[51];
			for (int d = 0; d < 51; d++) {
				data[d] = -1 * d;
			}
			data[0] = 0.00000000000001d;
			
			ColumnData cd = new StandardDoubleColumnData(data, "test", "unit",
					"desc",	null, false);
			SimpleDataTable table = new SimpleDataTable(new ColumnData[] {cd});
			zeroToNeg50in50ValuesWithTopOverage = new SparrowColumnSpecifier(table, 0, null);
		}
		
		{
			double[] data = new double[51];
			for (int d = 0; d < 51; d++) {
				data[d] = d;
			}
			data[0] = -0.00000000000001d;
			
			ColumnData cd = new StandardDoubleColumnData(data, "test", "unit",
					"desc",	null, false);
			SimpleDataTable table = new SimpleDataTable(new ColumnData[] {cd});
			zeroTo50in50ValuesWithBtmOverage = new SparrowColumnSpecifier(table, 0, null);
		}
		
		{
			double[] data = new double[51];
			for (int d = 0; d < 51; d++) {
				data[d] = -1 * d;
			}
			data[50] = -50.00000000000001d;
			
			ColumnData cd = new StandardDoubleColumnData(data, "test", "unit",
					"desc",	null, false);
			SimpleDataTable table = new SimpleDataTable(new ColumnData[] {cd});
			zeroToNeg50in50ValuesWithBtmOverage = new SparrowColumnSpecifier(table, 0, null);
		}
		
		{
			double[] data = new double[51];
			for (int d = 0; d < 51; d++) {
				data[d] = ((double)d) * .00000593333333333d;
			}
			data[50] = .00030000001d;
			
			ColumnData cd = new StandardDoubleColumnData(data, "test", "unit",
					"desc",	null, false);
			SimpleDataTable table = new SimpleDataTable(new ColumnData[] {cd});
			smallValuesWithTopOverage = new SparrowColumnSpecifier(table, 0, null);
		}
		
		{
			double[] data = new double[51];
			for (int d = 0; d < 51; d++) {
				data[d] = 99;
			}
			
			ColumnData cd = new StandardDoubleColumnData(data, "test", "unit",
					"desc",	null, false);
			SimpleDataTable table = new SimpleDataTable(new ColumnData[] {cd});
			sameSame = new SparrowColumnSpecifier(table, 0, null);
		}
		
		
		{
			double[] data = new double[11];
			for (int d = 0; d <= 10; d++) {
				data[d] = d / 2D;
			}
			
			data[9] = 5;
			data[10] = 10;
			
			ColumnData cd = new StandardDoubleColumnData(data, "test", "unit",
					"desc",	null, false);
			SimpleDataTable table = new SimpleDataTable(new ColumnData[] {cd});
			zeroTo10In10Values = new SparrowColumnSpecifier(table, 0, null);
		}
		
	}
	
	//
	//Basic fucntional Tests
	

	@Test
	public void testEqualRange_0to10in2bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(BigDecimal.ZERO, BigDecimal.TEN, 2,
				null, new BigDecimal(".01")).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(BigDecimal.ZERO));
		assertEquals(0, result[1].compareTo(new BigDecimal(5)));
		assertEquals(0, result[2].compareTo(BigDecimal.TEN));
	}
	
	@Test
	public void testEqualRange_0to10in2binsWithdot01Detect() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(BigDecimal.ZERO, BigDecimal.TEN, 2,
				DOT_01, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(BigDecimal.ZERO));
		assertEquals(0, result[1].compareTo(DOT_01));
		assertEquals(0, result[2].compareTo(new BigDecimal(5)));
		assertEquals(0, result[3].compareTo(BigDecimal.TEN));
	}
	
	@Test
	public void testEqualRange_0to10in2binsWithdot02Detect() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(BigDecimal.ZERO, BigDecimal.TEN, 2,
				DOT_02, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(BigDecimal.ZERO));
		assertEquals(0, result[1].compareTo(DOT_02));
		assertEquals(0, result[2].compareTo(new BigDecimal(5)));
		assertEquals(0, result[3].compareTo(BigDecimal.TEN));
	}
	
	
	@Test
	public void testEqualRange_0dot0000000000001to10in2bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("-.0000000001"), BigDecimal.TEN, 2,
				null, new BigDecimal(".01")).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("-.01")));
		assertEquals(0, result[1].compareTo(new BigDecimal("5")));
		assertEquals(0, result[2].compareTo(new BigDecimal("10")));
	}
	
	@Test
	public void testEqualRange_0dot0000000000001to10in10bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		InProcessBinSet ipbs = act.getEqualRangeBins(new BigDecimal("-.0000000001"), BigDecimal.TEN, 10,
				null, new BigDecimal(".01"));
		BigDecimal[] result = ipbs.posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("-.01")));
		assertEquals(0, result[1].compareTo(new BigDecimal("1")));
		assertEquals(0, result[2].compareTo(new BigDecimal("2")));
		assertEquals(0, result[10].compareTo(new BigDecimal("10")));
		assertFalse(ipbs.usesDetectionLimit);
	}
	
	@Test
	public void testEqualRange_0dot0000000000001to10in10binsWithDot02Detect() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		InProcessBinSet ipbs = act.getEqualRangeBins(new BigDecimal("-.0000000001"), BigDecimal.TEN, 10,
				DOT_02, new BigDecimal(".01"));
		BigDecimal[] result = ipbs.posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("-.01")));
		assertEquals(0, result[1].compareTo(new BigDecimal(".02")));
		assertEquals(0, result[2].compareTo(new BigDecimal("1")));
		assertEquals(0, result[3].compareTo(new BigDecimal("2")));
		assertEquals(0, result[11].compareTo(new BigDecimal("10")));
		assertTrue(ipbs.usesDetectionLimit);
	}
	
	@Test
	public void testEqualRange_1to11dot000000001in10bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("1"), new BigDecimal("11.0000000001"), 10,
				null, new BigDecimal(".01")).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("1")));
		assertEquals(0, result[1].compareTo(new BigDecimal("2")));
		assertEquals(0, result[2].compareTo(new BigDecimal("3")));
		assertEquals(0, result[9].compareTo(new BigDecimal("10")));
		assertEquals(0, result[10].compareTo(new BigDecimal("11.01")));
	}
	
	@Test
	public void testEqualRange_0to9dot9in10bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("0"), new BigDecimal("9.912"), 10,
				null, new BigDecimal(".01")).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal("1")));
		assertEquals(0, result[2].compareTo(new BigDecimal("2")));
		assertEquals(0, result[9].compareTo(new BigDecimal("9")));
		assertEquals(0, result[10].compareTo(new BigDecimal("10")));
	}
	
	///////////////////
	// Try some 3x bins looking for odd handling of .333333333333...
	///////////////////
	@Test
	public void testEqualRange_0to10in3bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("0"), new BigDecimal("10"), 3,
				null, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal("3.33")));
		assertEquals(0, result[2].compareTo(new BigDecimal("6.66")));
		assertEquals(0, result[3].compareTo(new BigDecimal("10")));
	}
	
	@Test
	public void testEqualRange_0to10in3binsWithDot02DetectLimit() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();

		InProcessBinSet ipbs = act.getEqualRangeBins(new BigDecimal("0"), new BigDecimal("10"), 3,
				DOT_02, DOT_01);
		BigDecimal[] result = ipbs.posts;
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal("0.02")));
		assertEquals(0, result[2].compareTo(new BigDecimal("3.33")));
		assertEquals(0, result[3].compareTo(new BigDecimal("6.66")));
		assertEquals(0, result[4].compareTo(new BigDecimal("10")));
	}
	
	@Test
	public void testEqualRange_neg0dot0346to10in3bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		InProcessBinSet ipbs = act.getEqualRangeBins(new BigDecimal("-0.0346"), new BigDecimal("10"), 3,
				null, DOT_01);
		BigDecimal[] result = ipbs.posts;
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("-0.04")));
		assertEquals(0, result[1].compareTo(new BigDecimal("3.33")));
		assertEquals(0, result[2].compareTo(new BigDecimal("6.66")));
		assertEquals(0, result[3].compareTo(new BigDecimal("10")));
		assertFalse(ipbs.usesDetectionLimit);
	}
	
	@Test
	public void testEqualRange_neg0dot0346to10in3binsWithDot02DetectLimit() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		InProcessBinSet ipbs = act.getEqualRangeBins(new BigDecimal("-0.0346"), new BigDecimal("10"), 3,
				DOT_02, DOT_01);
		BigDecimal[] result = ipbs.posts;
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("-0.04")));
		assertEquals(0, result[1].compareTo(new BigDecimal("0.02")));
		assertEquals(0, result[2].compareTo(new BigDecimal("3.33")));
		assertEquals(0, result[3].compareTo(new BigDecimal("6.66")));
		assertEquals(0, result[4].compareTo(new BigDecimal("10")));
		assertTrue(ipbs.usesDetectionLimit);
	}
	
	@Test
	public void testEqualRange_neg10to0in3bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("-10"), new BigDecimal("0"), 3,
				null, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("-10")));
		assertEquals(0, result[1].compareTo(new BigDecimal("-6.67")));
		assertEquals(0, result[2].compareTo(new BigDecimal("-3.34")));
		assertEquals(0, result[3].compareTo(new BigDecimal("0")));
	}
	
	@Test
	public void testEqualRange_neg10to0in3binsWithDot02DetectLimit() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		InProcessBinSet ipbs = act.getEqualRangeBins(new BigDecimal("-10"), new BigDecimal("0"), 3,
				DOT_02, DOT_01);
		BigDecimal[] result = ipbs.posts;
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("-10")));
		assertEquals(0, result[1].compareTo(new BigDecimal(".02")));
		assertEquals(0, result[2].compareTo(new BigDecimal(".03")));
		assertEquals(0, result[3].compareTo(new BigDecimal(".04")));
		assertEquals(0, result[4].compareTo(new BigDecimal(".05")));
		assertTrue(ipbs.usesDetectionLimit);
	}
	
	@Test
	public void testEqualRange_neg10To0dot0346in3bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("-10"), new BigDecimal("0.0346"), 3,
				null, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("-10")));
		assertEquals(0, result[1].compareTo(new BigDecimal("-6.67")));
		assertEquals(0, result[2].compareTo(new BigDecimal("-3.34")));
		assertEquals(0, result[3].compareTo(new BigDecimal("0.04")));
	}
	
	///////////////////
	// Relatively small values wrt the CUV
	///////////////////
	@Test
	public void testEqualRange_0To0dot15in5bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("0"), new BigDecimal("0.15"), 5,
				null, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal(".03")));
		assertEquals(0, result[2].compareTo(new BigDecimal(".06")));
		assertEquals(0, result[3].compareTo(new BigDecimal(".09")));
		assertEquals(0, result[4].compareTo(new BigDecimal(".12")));
		assertEquals(0, result[5].compareTo(new BigDecimal(".15")));
	}
	
	@Test
	public void testEqualRange_0To0dot15in5binsWithDot2DetectLimit() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		gov.usgs.cida.binning.domain.InProcessBinSet ipbs = act.getEqualRangeBins(new BigDecimal("0"), new BigDecimal("0.15"), 5,
				new BigDecimal(".2"), DOT_01);
		BigDecimal[] result = ipbs.posts;
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal(".2")));
		assertEquals(0, result[2].compareTo(new BigDecimal(".21")));
		assertEquals(0, result[3].compareTo(new BigDecimal(".22")));
		assertEquals(0, result[4].compareTo(new BigDecimal(".23")));
		assertEquals(0, result[5].compareTo(new BigDecimal(".24")));
		assertEquals(0, result[6].compareTo(new BigDecimal(".25")));
		assertTrue(ipbs.usesDetectionLimit);
	}
	
	@Test
	public void testEqualRange_0To0dot16in5bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("0"), new BigDecimal("0.16"), 5,
				null, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal(".03")));
		assertEquals(0, result[2].compareTo(new BigDecimal(".06")));
		assertEquals(0, result[3].compareTo(new BigDecimal(".09")));
		assertEquals(0, result[4].compareTo(new BigDecimal(".12")));
		assertEquals(0, result[5].compareTo(new BigDecimal(".16")));
	}
	
	@Test
	public void testEqualRange_0To0dot17in5bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("0"), new BigDecimal("0.17"), 5,
				null, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal(".03")));
		assertEquals(0, result[2].compareTo(new BigDecimal(".06")));
		assertEquals(0, result[3].compareTo(new BigDecimal(".09")));
		assertEquals(0, result[4].compareTo(new BigDecimal(".12")));
		assertEquals(0, result[5].compareTo(new BigDecimal(".17")));
	}
	
	@Test
	public void testEqualRange_0To0dot18in5bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("0"), new BigDecimal("0.18"), 5,
				null, DOT_01).posts;
		
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal(".04")));
		assertEquals(0, result[2].compareTo(new BigDecimal(".08")));
		assertEquals(0, result[3].compareTo(new BigDecimal(".12")));
		assertEquals(0, result[4].compareTo(new BigDecimal(".16")));
		assertEquals(0, result[5].compareTo(new BigDecimal(".20")));
	}
	
	///////////////////
	// Relatively BIG values wrt the CUV
	///////////////////
	@Test
	public void testEqualRange_BigNumbersin5bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BigDecimal[] result = act.getEqualRangeBins(new BigDecimal("84732949.473923"), new BigDecimal("984847329873.823473923"), 5,
				null, new BigDecimal("1000000000")).posts;
		debug(result);
		
		assertEquals(0, result[0].compareTo(new BigDecimal("0")));
		assertEquals(0, result[1].compareTo(new BigDecimal("200000000000")));
		assertEquals(0, result[2].compareTo(new BigDecimal("400000000000")));
		assertEquals(0, result[3].compareTo(new BigDecimal("600000000000")));
		assertEquals(0, result[4].compareTo(new BigDecimal("800000000000")));
		assertEquals(0, result[5].compareTo(new BigDecimal("1000000000000")));
	}
	
	@Test
	public void testEqualRangeFormated_BigRandomNumbersIn5bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BinSet binSet = act.getEqualRangeBins(
				new BigDecimal("84732949.473923"),
				new BigDecimal("984847329873.823473923"), 5,
				null, (Integer)null);
		String[] formatted = binSet.getFormattedPostValues();
		String[] fformatted = binSet.getFormattedFunctionalPostValues();
		
		assertEquals("0E0", formatted[0]);
		assertEquals("200E9", formatted[1]);
		assertEquals("400E9", formatted[2]);
		assertEquals("600E9", formatted[3]);
		assertEquals("800E9", formatted[4]);
		assertEquals("1E12", formatted[5]);
		
		//function is the same b/c the extreme bounds are set outside the
		//actual min and max values.
		assertEquals("0", fformatted[0]);
		assertEquals("200000000000", fformatted[1]);
		assertEquals("400000000000", fformatted[2]);
		assertEquals("600000000000", fformatted[3]);
		assertEquals("800000000000", fformatted[4]);
		assertEquals("1000000000000", fformatted[5]);
	}
	
	@Test
	public void testEqualRangeFormated_BigEvenNumbersIn5bins() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		BinSet binSet = act.getEqualRangeBins(
				new BigDecimal("0"),
				new BigDecimal("1000000000000"), 5,
				null, (Integer)null);
		String[] formatted = binSet.getFormattedPostValues();
		String[] fformatted = binSet.getFormattedFunctionalPostValues();
		
		assertEquals("0E0", formatted[0]);
		assertEquals("200E9", formatted[1]);
		assertEquals("400E9", formatted[2]);
		assertEquals("600E9", formatted[3]);
		assertEquals("800E9", formatted[4]);
		assertEquals("1E12", formatted[5]);
		
		//function is the same b/c the extreme bounds are set outside the
		//actual min and max values.
		assertEquals("-1000000000", fformatted[0]);
		assertEquals("200000000000", fformatted[1]);
		assertEquals("400000000000", fformatted[2]);
		assertEquals("600000000000", fformatted[3]);
		assertEquals("800000000000", fformatted[4]);
		assertEquals("1001000000000", fformatted[5]);
	}
	
	///////////////////
	// Delivery Series using filtered rows
	// For delivery, rows not upstream of the outlet are removed from the values.
	///////////////////
	@Test
	public void testEqualRange_1To10in10withInlcusionMap() throws Exception {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		//Create a delivery fraction map that indicates that all rows except
		//row 9 (which contains the value 10) are included.
		HashMap<Integer, DeliveryReach> map = new HashMap<Integer, DeliveryReach>();
		for (int i=0; i<10; i++) {
			map.put(i, new DeliveryReach(i, .5d, i));
		}
		ReachRowValueMap dfm = new ReachRowValueMap(map);
		
		act.setBinCount(5);
		act.setDataColumn(zeroTo10In10Values);
		act.setInclusionMap(dfm);
		
		BinSet binSet = act.run();
		BigDecimal[] posts = binSet.getActualPostValues();
		debug(posts);
		
		assertEquals(0, posts[0].compareTo(new BigDecimal("0")));
		assertEquals(0, posts[1].compareTo(new BigDecimal("1")));
		assertEquals(0, posts[2].compareTo(new BigDecimal("2")));
		assertEquals(0, posts[3].compareTo(new BigDecimal("3")));
		assertEquals(0, posts[4].compareTo(new BigDecimal("4")));
		assertEquals(0, posts[5].compareTo(new BigDecimal("5")));
	}
	
	///////////////////
	// Basic Util Methods
	///////////////////
	
	@Test
	public void testGetAllowedOutwardRangeStretch() {
		BigDecimal bd = new BigDecimal("18");
		assertEquals(0, CalcEqualRangeBins.getAllowedOutwardRangeStretch(bd, 3).compareTo(new BigDecimal("4")));
		
		BigDecimal result = CalcEqualRangeBins.getAllowedOutwardRangeStretch(new BigDecimal("9.99"), 5);
		assertEquals(0, result.compareTo(new BigDecimal("1.332")));
	}
	
	@Test
	public void testGetScaleOfMostSignificantDigit() {
		assertEquals(-5, CalcEqualRangeBins.getScaleOfMostSignificantDigit(new BigDecimal("985000")));
		assertEquals(4, CalcEqualRangeBins.getScaleOfMostSignificantDigit(new BigDecimal(".000985000")));
		assertEquals(0, CalcEqualRangeBins.getScaleOfMostSignificantDigit(new BigDecimal("1.0000")));
		assertEquals(0, CalcEqualRangeBins.getScaleOfMostSignificantDigit(new BigDecimal("0.0000")));
		assertEquals(2, CalcEqualRangeBins.getScaleOfMostSignificantDigit(new BigDecimal(".01")));
	}
	
	@Test
	public void testRoundAndGetScaleOfMostSignificantDigitInteraction() {
		BigDecimal bd985000 = new BigDecimal("985000");
		BigDecimal bd000985 = new BigDecimal(".000985000");
		
		//Try rounding to the most significant digit
		int scale = CalcEqualRangeBins.getScaleOfMostSignificantDigit(bd985000);
		BigDecimal rounded = CalcEqualRangeBins.round(bd985000, scale, RoundingMode.FLOOR);
		assertEquals(0, rounded.compareTo(new BigDecimal("900000")));
		
		scale = CalcEqualRangeBins.getScaleOfMostSignificantDigit(bd000985);
		rounded = CalcEqualRangeBins.round(bd000985, scale, RoundingMode.FLOOR);
		assertEquals(0, rounded.compareTo(new BigDecimal(".0009")));
	}
	
	@Test
	public void testBuildOneTimesTenToThePower() {
		assertEquals(0, CalcEqualRangeBins.oneTimesTenToThePower(0).compareTo(new BigDecimal("1")));
		assertEquals(0, CalcEqualRangeBins.oneTimesTenToThePower(1).compareTo(new BigDecimal("10")));
		assertEquals(0, CalcEqualRangeBins.oneTimesTenToThePower(2).compareTo(new BigDecimal("100")));
		assertEquals(0, CalcEqualRangeBins.oneTimesTenToThePower(-1).compareTo(new BigDecimal(".1")));
		assertEquals(0, CalcEqualRangeBins.oneTimesTenToThePower(-2).compareTo(new BigDecimal(".01")));
	}
	
	@Test
	public void testBuildCharacteristicUnitValueWithoutDecimalPlaceSpecified() {
		
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		BigDecimal cuv = null;
		
		cuv = act.getCharacteristicUnitValue(
				BigDecimal.ZERO, BigDecimal.ONE,
				5, null);
		assertEquals(0, cuv.compareTo(DOT_001));
		
		//Zero range non-zero values
		cuv = act.getCharacteristicUnitValue(
				ONE_HUNDRED, ONE_HUNDRED,
				5, null);
		assertEquals(0, cuv.compareTo(DOT_1));
		
		//Zero range zero values
		cuv = act.getCharacteristicUnitValue(
				BigDecimal.ZERO, BigDecimal.ZERO,
				5, null);
		assertEquals(0, cuv.compareTo(DOT_01));
		
		//Big numbers
		cuv = act.getCharacteristicUnitValue(
				BigDecimal.ZERO, new BigDecimal("1000000"),
				5, null);
		assertEquals(0, cuv.compareTo(new BigDecimal("1000")));
		
		cuv = act.getCharacteristicUnitValue(
				new BigDecimal("84732949.473923"), new BigDecimal("984847329873.823473923"),
				5, null);
		assertEquals(0, cuv.compareTo(new BigDecimal("1000000000")));
		
	}
	
	@Test
	public void testBuildCharacteristicUnitValueWithDecimalPlaceSpecified() {
		
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		BigDecimal cuv = null;
		
		
		//calculated CUV is larger than spec'ed decimal
		cuv = act.getCharacteristicUnitValue(
				BigDecimal.ZERO, new BigDecimal("10000"),
				5, 1);
		assertEquals(0, cuv.compareTo(TEN));
		
		
		//specified decimal is larger
		cuv = act.getCharacteristicUnitValue(
				BigDecimal.ZERO, BigDecimal.ONE,
				5, 1);
		assertEquals(0, cuv.compareTo(DOT_1));
		
		//Zero range non-zero values
		cuv = act.getCharacteristicUnitValue(
				ONE_HUNDRED, ONE_HUNDRED,
				5, 0);
		assertEquals(0, cuv.compareTo(BigDecimal.ONE));
		
		//Zero range zero values
		cuv = act.getCharacteristicUnitValue(
				BigDecimal.ZERO, BigDecimal.ZERO,
				5, 3);
		assertEquals(0, cuv.compareTo(DOT_001));
	}
	
	@Test
	public void testRoundToNiceIfPossible() {
		CalcEqualRangeBins act = new CalcEqualRangeBins();
		
		//
		//Assume bin width is 1
		BigDecimal rounded = act.roundToNiceIfPossible(
				new BigDecimal(".03"), new BigDecimal("-.47"), new BigDecimal(".53"),
				1);
		assertEquals(0, rounded.compareTo(BigDecimal.ZERO));
		
		rounded = act.roundToNiceIfPossible(
				new BigDecimal(".81"), new BigDecimal(".31"), new BigDecimal("1.31"),
				1);
		assertEquals(0, rounded.compareTo(new BigDecimal(".8")));
		
		rounded = act.roundToNiceIfPossible(
				new BigDecimal(".449"), new BigDecimal(".05"), new BigDecimal(".95"),
				1);
		assertEquals(0, rounded.compareTo(new BigDecimal(".5")));
		
		rounded = act.roundToNiceIfPossible(
				new BigDecimal(".449"), new BigDecimal("-.05"), new BigDecimal(".95"),
				1);
		assertEquals(0, rounded.compareTo(new BigDecimal("0")));
		
		rounded = act.roundToNiceIfPossible(
				new BigDecimal(".551"), new BigDecimal(".05"), new BigDecimal("1.05"),
				1);
		assertEquals(0, rounded.compareTo(new BigDecimal(".5")));
	}
	
	@Test
	public void testRound() {
		BigDecimal rounded = CalcEqualRangeBins.round(new BigDecimal(".04"), 1, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(BigDecimal.ZERO));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal(".05"), 1, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(BigDecimal.ZERO));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal(".06"), 1, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(new BigDecimal(".1")));
		
		//
		//Round to 0 decimal places
		rounded = CalcEqualRangeBins.round(new BigDecimal(".4"), 0, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(BigDecimal.ZERO));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal("1.4"), 0, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(BigDecimal.ONE));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal(".5"), 0, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(BigDecimal.ZERO));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal("1.5"), 0, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(new BigDecimal("2")));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal(".6"), 0, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(new BigDecimal("1")));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal("1.6"), 0, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(new BigDecimal("2")));
		
		//
		//Round to -1 decimal places
		rounded = CalcEqualRangeBins.round(new BigDecimal("144.4"), -1, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(new BigDecimal("140")));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal("145.00"), -1, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(new BigDecimal("140")));
		
		rounded = CalcEqualRangeBins.round(new BigDecimal("145.01"), -1, RoundingMode.HALF_EVEN);
		assertEquals(0, rounded.compareTo(new BigDecimal("150")));
		
	}
	
	@Test
	public void testFindNiceBinWidth() {
		BigDecimal width = CalcEqualRangeBins.findNiceBinWidth(new BigDecimal(".16"), 5, DOT_01);
		assertEquals(0, width.compareTo(new BigDecimal(".03")));
		
		width = CalcEqualRangeBins.findNiceBinWidth(new BigDecimal(".17"), 5, DOT_01);
		assertEquals(0, width.compareTo(new BigDecimal(".03")));
		
		width = CalcEqualRangeBins.findNiceBinWidth(new BigDecimal(".18"), 5, DOT_01);
		assertEquals(0, width.compareTo(new BigDecimal(".04")));
		
		width = CalcEqualRangeBins.findNiceBinWidth(new BigDecimal(".19"), 5, DOT_01);
		assertEquals(0, width.compareTo(new BigDecimal(".04")));
		
		width = CalcEqualRangeBins.findNiceBinWidth(new BigDecimal(".20"), 5, DOT_01);
		assertEquals(0, width.compareTo(new BigDecimal(".04")));
		
		width = CalcEqualRangeBins.findNiceBinWidth(new BigDecimal(".21"), 5, DOT_01);
		assertEquals(0, width.compareTo(new BigDecimal(".04")));
		
		width = CalcEqualRangeBins.findNiceBinWidth(new BigDecimal(".22"), 5, DOT_01);
		assertEquals(0, width.compareTo(new BigDecimal(".04")));
		
		width = CalcEqualRangeBins.findNiceBinWidth(new BigDecimal(".23"), 5, DOT_01);
		assertEquals(0, width.compareTo(new BigDecimal(".05")));
	}
	
	
	//
	//Result tests based on sets of data
	@Test
	public void getCharacteristicUnitValuePositive() {
		BigDecimal[] posts = new BigDecimal[3];
		posts[0] = new BigDecimal(0);
		posts[1] = new BigDecimal(25);
		posts[2] = new BigDecimal(50.000000001);
		
		BigDecimal charUnitValue = CalcEqualRangeBins.getCharacteristicUnitValue(
				BigDecimal.ZERO, new BigDecimal("50.000000001"), 2, null);
		assertEquals(new BigDecimal(".1"), charUnitValue);
	}
	
	@Test
	public void getCharacteristicUnitValueNegative() {
		BigDecimal[] posts = new BigDecimal[3];
		posts[0] = new BigDecimal(-50.000000001);
		posts[1] = new BigDecimal(-25);
		posts[2] = new BigDecimal(-.01);
		
		BigDecimal charUnitValue = CalcEqualRangeBins.getCharacteristicUnitValue(
				new BigDecimal("-50.000000001"), new BigDecimal("-.01"), 2, null);
		assertEquals(new BigDecimal(".1"), charUnitValue);
	}
	

	@Test
	public void testPositiveBinsWithTopOverage() throws Exception {

		BinningRequest req = new BinningRequest(99999, 2, BinType.EQUAL_RANGE,
				DataSeriesType.total, ComparisonType.none,
				SparrowModel.TN_CONSTITUENT_NAME, null, null);
		
		CalcEqualRangeBins action = new CalcEqualRangeBins();
		
		
		action.setDataColumn(zeroTo50in50ValuesWithTopOverage);
		action.setBinCount(req.getBinCount());
		
		
		BinSet bs = action.run();
		
		assertEquals("0.0", bs.getBins()[0].getBottom().getFormatted());
		assertEquals("30.0", bs.getBins()[0].getTop().getFormatted());
		assertEquals("30.0", bs.getBins()[1].getBottom().getFormatted());
		assertEquals("60.0", bs.getBins()[1].getTop().getFormatted());
		
		assertEquals("-0.1", bs.getBins()[0].getBottom().getFormattedFunctional());
		assertEquals("30.0", bs.getBins()[0].getTop().getFormattedFunctional());
		assertEquals("30.0", bs.getBins()[1].getBottom().getFormattedFunctional());
		assertEquals("60.0", bs.getBins()[1].getTop().getFormattedFunctional());
		
		
		for (int i = 0; i < bs.getBins().length; i++) {
			Bin b = bs.getBins()[i];
			log.debug(b.getBottom().getFormatted() + " - " + b.getTop().getFormatted());
		}
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(bs, 99999L, ServiceResponseStatus.OK,
				ServiceResponseOperation.CALCULATE);
		
		String xml = ServletResponseParser.getXMLXStream().toXML(wrap);
		
		log.debug(xml);
		
	}
	
	@Test
	public void testPositiveBinsWithInfiniteTop() throws Exception {

		BinningRequest req = new BinningRequest(99999, 2, BinType.EQUAL_RANGE,
				DataSeriesType.total, ComparisonType.none,
				SparrowModel.TN_CONSTITUENT_NAME, null, null);
		
		CalcEqualRangeBins action = new CalcEqualRangeBins();
		
		
		action.setDataColumn(zeroTo50in50ValuesWithInfiniteTop);
		action.setBinCount(req.getBinCount());
		
		
		BinSet bs = action.run();
		
		assertEquals("0.0", bs.getBins()[0].getBottom().getFormatted());
		assertEquals("25.0", bs.getBins()[0].getTop().getFormatted());
		assertEquals("25.0", bs.getBins()[1].getBottom().getFormatted());
		assertEquals("50.0", bs.getBins()[1].getTop().getFormatted());
		
		assertEquals("-0.1", bs.getBins()[0].getBottom().getFormattedFunctional());
		assertEquals("25.0", bs.getBins()[0].getTop().getFormattedFunctional());
		assertEquals("25.0", bs.getBins()[1].getBottom().getFormattedFunctional());
		assertEquals("50.0", bs.getBins()[1].getTop().getFormattedFunctional());
		
		
		for (int i = 0; i < bs.getBins().length; i++) {
			Bin b = bs.getBins()[i];
			log.debug(b.getBottom().getFormatted() + " - " + b.getTop().getFormatted());
		}
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(bs, 99999L, ServiceResponseStatus.OK,
				ServiceResponseOperation.CALCULATE);
		
		String xml = ServletResponseParser.getXMLXStream().toXML(wrap);
		
		log.debug(xml);
		
	}
	
	@Test
	public void testNegativeBinsWithTopOverage() throws Exception {
		
		BinningRequest req = new BinningRequest(99999, 2, BinType.EQUAL_RANGE,
				DataSeriesType.total, ComparisonType.none,
				SparrowModel.TN_CONSTITUENT_NAME, null, null);
		CalcEqualRangeBins action = new CalcEqualRangeBins();
		
		
		action.setDataColumn(zeroToNeg50in50ValuesWithTopOverage);
		action.setBinCount(req.getBinCount());
		
		BinSet bs = action.run();
		
		assertEquals("-50.0", bs.getBins()[0].getBottom().getFormatted());
		assertEquals("-25.0", bs.getBins()[0].getTop().getFormatted());
		assertEquals("-25.0", bs.getBins()[1].getBottom().getFormatted());
		assertEquals("0.1", bs.getBins()[1].getTop().getFormatted());
		
		BigDecimal neg50 = bs.getBins()[0].getBottom().getFunctional();
		log.debug(neg50.toPlainString());
		assertEquals("-50.1", bs.getBins()[0].getBottom().getFormattedFunctional());
		assertEquals("-25.0", bs.getBins()[0].getTop().getFormattedFunctional());
		assertEquals("-25.0", bs.getBins()[1].getBottom().getFormattedFunctional());
		assertEquals("0.1", bs.getBins()[1].getTop().getFormattedFunctional());
		
		
		for (int i = 0; i < bs.getBins().length; i++) {
			Bin b = bs.getBins()[i];
			log.debug(b.getBottom().getFormatted() + " - " + b.getTop().getFormatted());
		}
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(bs, 99999L, ServiceResponseStatus.OK,
				ServiceResponseOperation.CALCULATE);
		
		String xml = ServletResponseParser.getXMLXStream().toXML(wrap);
		
		log.debug(xml);
	}
	
	@Test
	public void testPositiveBinsWithBtmOverage() throws Exception {
		
		BinningRequest req = new BinningRequest(99999, 2, BinType.EQUAL_RANGE,
				DataSeriesType.total, ComparisonType.none,
				SparrowModel.TN_CONSTITUENT_NAME, null, null);
		CalcEqualRangeBins action = new CalcEqualRangeBins();
		
		
		action.setDataColumn(zeroTo50in50ValuesWithBtmOverage);
		action.setBinCount(req.getBinCount());
		
		BinSet bs = action.run();
		
		assertEquals("-0.1", bs.getBins()[0].getBottom().getFormatted());
		assertEquals("25.0", bs.getBins()[0].getTop().getFormatted());
		assertEquals("25.0", bs.getBins()[1].getBottom().getFormatted());
		assertEquals("50.0", bs.getBins()[1].getTop().getFormatted());
		
		assertEquals("-0.1", bs.getBins()[0].getBottom().getFormattedFunctional());
		assertEquals("25.0", bs.getBins()[0].getTop().getFormattedFunctional());
		assertEquals("25.0", bs.getBins()[1].getBottom().getFormattedFunctional());
		assertEquals("50.1", bs.getBins()[1].getTop().getFormattedFunctional());
	}
	
	@Test
	public void testNegativeBinsWithBtmOverage() throws Exception {
		
		BinningRequest req = new BinningRequest(99999, 2, BinType.EQUAL_RANGE,
				DataSeriesType.total, ComparisonType.none,
				SparrowModel.TN_CONSTITUENT_NAME, null, null);
		
		CalcEqualRangeBins action = new CalcEqualRangeBins();
		
		
		action.setDataColumn(zeroToNeg50in50ValuesWithBtmOverage);
		action.setBinCount(req.getBinCount());
		
		BinSet bs = action.run();
		
		assertEquals("-50.1", bs.getBins()[0].getBottom().getFormatted());
		assertEquals("-25.0", bs.getBins()[0].getTop().getFormatted());
		assertEquals("-25.0", bs.getBins()[1].getBottom().getFormatted());
		assertEquals("0.0", bs.getBins()[1].getTop().getFormatted());
		
		assertEquals("-50.1", bs.getBins()[0].getBottom().getFormattedFunctional());
		assertEquals("-25.0", bs.getBins()[0].getTop().getFormattedFunctional());
		assertEquals("-25.0", bs.getBins()[1].getBottom().getFormattedFunctional());
		assertEquals("0.1", bs.getBins()[1].getTop().getFormattedFunctional());
		
	}
	
	
	@Test
	public void testSmallBinsWithTopOverage() throws Exception {
		
		BinningRequest req = new BinningRequest(99999, 7, BinType.EQUAL_RANGE,
				DataSeriesType.total, ComparisonType.none,
				SparrowModel.TN_CONSTITUENT_NAME, null, null);
		CalcEqualRangeBins action = new CalcEqualRangeBins();
		
		
		action.setDataColumn(smallValuesWithTopOverage);
		action.setBinCount(req.getBinCount());
		
		BinSet bs = action.run();
		
//		assertEquals("-50.0", bs.getBins()[0].getBottom().getFormatted());
//		assertEquals("-25.0", bs.getBins()[0].getTop().getFormatted());
//		assertEquals("-25.0", bs.getBins()[1].getBottom().getFormatted());
//		assertEquals("0.0", bs.getBins()[1].getTop().getFormatted());
//		
//		assertEquals(new BigDecimal("-50.1"), bs.getBins()[0].getBottom().getFunctionalActual());
//		assertEquals(new BigDecimal("-25"), bs.getBins()[0].getTop().getFunctionalActual());
//		assertEquals(new BigDecimal("-25"), bs.getBins()[1].getBottom().getFunctionalActual());
//		assertEquals(new BigDecimal("0"), bs.getBins()[1].getTop().getFunctionalActual());
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(bs, 99999L, ServiceResponseStatus.OK,
				ServiceResponseOperation.CALCULATE);
		String xml = ServletResponseParser.getXMLXStream().toXML(wrap);
		
		log.debug(xml);
	}
	
	@Test
	public void testAllTheSameValue() throws Exception {
		
		BinningRequest req = new BinningRequest(99999, 7, BinType.EQUAL_RANGE,
				DataSeriesType.total, ComparisonType.none,
				SparrowModel.TN_CONSTITUENT_NAME, null, null);
		CalcEqualRangeBins action = new CalcEqualRangeBins();
		
		
		action.setDataColumn(sameSame);
		action.setBinCount(req.getBinCount());
		
		BinSet bs = action.run();
		
//		assertEquals("-50.0", bs.getBins()[0].getBottom().getFormatted());
//		assertEquals("-25.0", bs.getBins()[0].getTop().getFormatted());
//		assertEquals("-25.0", bs.getBins()[1].getBottom().getFormatted());
//		assertEquals("0.0", bs.getBins()[1].getTop().getFormatted());
//		
//		assertEquals(new BigDecimal("-50.1"), bs.getBins()[0].getBottom().getFunctionalActual());
//		assertEquals(new BigDecimal("-25"), bs.getBins()[0].getTop().getFunctionalActual());
//		assertEquals(new BigDecimal("-25"), bs.getBins()[1].getBottom().getFunctionalActual());
//		assertEquals(new BigDecimal("0"), bs.getBins()[1].getTop().getFunctionalActual());
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(bs, 99999L, ServiceResponseStatus.OK,
				ServiceResponseOperation.CALCULATE);
		String xml = ServletResponseParser.getXMLXStream().toXML(wrap);
		
		log.debug(xml);
	}
	
	
	
	
	/**
	 * 
	 * @param bottom
	 * @param top
	 * @param valueCount
	 * @param likelyhoodOfDuplicateBlocks 0 to 1 where 1 would generate all duplicate values.
	 * @param forceNonUniformDistribution
	 * @param rndSeed
	 * @return
	 */
	private double[] buildValues(double bottom, double top, int valueCount,
			double likelyhoodOfDuplicateBlocks, 
			boolean forceNonUniformDistribution, long rndSeed) {
		
		double[] values = new double[valueCount];
		Random rnd = new Random(rndSeed);
		double range = top - bottom;
		int currentDupCountDown = 0;	//if greater than 0, we are duplicating
		double newVal = 0;	//not yet assigned
		
		for (int i = 0; i < valueCount; i++) {
			
			if (currentDupCountDown > 0) {
				values[i] = newVal;
				currentDupCountDown--;
			} else {
				double rndNumber = rnd.nextDouble();
				
				if (rndNumber < likelyhoodOfDuplicateBlocks) {
					double repeatFactor1 = rnd.nextDouble();
					double repeatFactor2 = rnd.nextDouble();
					double repeatFactor3 = rnd.nextDouble();
					double repeatFactor = repeatFactor1 * repeatFactor2 * repeatFactor3;
					currentDupCountDown = (int)(repeatFactor * (double)valueCount);
					//currentDupCountDown = (int)(Math.pow(repeatFactor, 5) * (double)valueCount);
					rndNumber = rnd.nextDouble();	//new number b/c the previous is biased.
				}
				if (forceNonUniformDistribution) rndNumber = rndNumber * rndNumber;
				
				newVal = (range * rndNumber) + bottom;
				values[i] = newVal;
			}
		}
		
		return values;
		
	}
	
	/**
	 * 
	 * @param bottom
	 * @param top
	 * @param valueCount
	 * @param repeatFraction For a value, the number of repeats can be expected be (repeatFraction * valueCount)
	 * @param useIntegers
	 * @param rndSeed
	 * @return
	 */
	private double[] buildLumpyValues(double bottom, double top, int valueCount,
			double repeatFraction, 
			boolean useIntegers, long rndSeed) {
		
		double[] values = new double[valueCount];
		Random rnd = new Random(rndSeed);
		double range = top - bottom;
		int currentDupCountDown = 0;	//if greater than 0, we are duplicating
		double newVal = 0;	//not yet assigned
		
		for (int i = 0; i < valueCount; i++) {
			
			if (currentDupCountDown > 0) {
				values[i] = newVal;
				currentDupCountDown--;
			} else {
				double rndNumber = rnd.nextDouble();
				
				double repeatFactor = rnd.nextDouble() * repeatFraction;
				currentDupCountDown = (int)(repeatFactor * (double)valueCount);
				//currentDupCountDown = (int)(Math.pow(repeatFactor, 5) * (double)valueCount);

				newVal = (range * rndNumber) + bottom;
				if (useIntegers) {
					newVal = (int)newVal;
				}
				values[i] = newVal;
			}
		}
		
		return values;
		
	}
	
	void debug(BigDecimal[] bins) {
		if (log.isDebugEnabled()) {
			for (int i=0; i< bins.length; i++) {
				log.debug("Bin " + i + ": " + bins[i].toPlainString());
			}
			
		}
	}

}
