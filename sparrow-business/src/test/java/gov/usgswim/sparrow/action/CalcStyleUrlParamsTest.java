package gov.usgswim.sparrow.action;

import gov.usgs.cida.binning.CalcEqualRangeBins;
import gov.usgs.cida.binning.domain.BinSet;
import gov.usgs.cida.binning.domain.BinType;
import gov.usgs.cida.binning.domain.InProcessBinSet;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class CalcStyleUrlParamsTest {


	@Test
	public void simpleTest() throws Exception {
		BinSet bs = buildBinSet(0d, 100d, null, 5);
		
		assertEquals(5, bs.getBins().length);
		
		CalcStyleUrlParams styleAct = new CalcStyleUrlParams(bs);
		String url = styleAct.run();
		
		//System.out.println(url);
		assertEquals("binLowList=0.0,20.0,40.0,60.0,80.0&binHighList=20.0,40.0,60.0,80.0,100.0&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014&bounded=true", url);
	}
	
	@Test
	public void unlimitedBottomBound() throws Exception {
		BinSet bs = buildBinSet(0d, 100d, null, 5);
		
		assertEquals(5, bs.getBins().length);
		
		InProcessBinSet ipbs = bs.createInProcessBinSet();
		ipbs.functional = bs.getActualPostValues();
		bs = BinSet.createBins(ipbs, new DecimalFormat("0.000"), new DecimalFormat("0.000"),
				true, false, "", BinType.EQUAL_RANGE);
		
		
		CalcStyleUrlParams styleAct = new CalcStyleUrlParams(bs);
		String url = styleAct.run();
		
		//System.out.println(url);
		assertEquals("binLowList=0.0,20.0,40.0,60.0,80.0&binHighList=20.0,40.0,60.0,80.0,100.0&binColorList=FFFFD4,FEE391,FEC44F,FE9929,EC7014&bounded=false", url);
	}
	
	@Test
	public void detectionLimitShouldResultInSixBins() throws Exception {
		BinSet bs = buildBinSet(0d, 100d, new BigDecimal("10.0"), 5);
		
		assertEquals(6, bs.getBins().length);
		
		CalcStyleUrlParams styleAct = new CalcStyleUrlParams(bs);
		String url = styleAct.run();
		
		//System.out.println(url);
		assertEquals("binLowList=0.0,10.0,20.0,40.0,60.0,80.0&binHighList=10.0,20.0,40.0,60.0,80.0,100.0&binColorList=FFFFD4,FEE391,FEC44F,FE9929,F5851F,EC7014&bounded=false", url);
	}
	
	
	
	public static BinSet buildBinSet(double low, double high, BigDecimal detectionLimit, int binCount) throws Exception {
		double[] vals = new double[100];
		for (int i = 0; i < 100; i++) {
			vals[i] = low + ((high - low) * (i / 99));
		}
		
		CalcEqualRangeBins calcBins = new CalcEqualRangeBins();

		calcBins.setMaxValue(new BigDecimal(high));
		calcBins.setMinValue(new BigDecimal(low));
		calcBins.setBinCount(binCount);
		
		if (detectionLimit != null) {
			calcBins.setDetectionLimit(detectionLimit);
		}
		
		
		return calcBins.doAction();
	}
}
