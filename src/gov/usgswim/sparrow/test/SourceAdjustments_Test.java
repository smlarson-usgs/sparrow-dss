package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Adjustment;
import gov.usgswim.sparrow.Adjustment.AdjustmentType;
import gov.usgswim.sparrow.AdjustmentSet;
import gov.usgswim.sparrow.AdjustmentSetBuilder;
import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.Data2DCompare;
import gov.usgswim.sparrow.Double2D;
import gov.usgswim.sparrow.AdjustmentSetImm;

import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.Map;

import junit.framework.TestCase;

public class SourceAdjustments_Test extends TestCase {
	Double2D data;
	
	public SourceAdjustments_Test(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		InputStream fileStream = this.getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/src.txt");
		data = TabDelimFileUtil.readAsDouble(fileStream, true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		data = null;
	}

	/**
	 * @see gov.usgswim.sparrow.AdjustmentSet#setAdjustment(String,String)
	 *
	 * The test file src.txt contains 11 sources.  Here we'll adjust some randomly
	 * and see if the coefficient adjustment matches normal multiplied values.
	 */

	public void testSetAdjustment() {
		//Adjustments per source (source #, coef).
		//Any skipped ones are assumed to be 1.
		String adjustString = "1,.5, 4,.1, 7,1, 8,0, 9,0 10,.5";
		Map adjMap = new HashMap(11);
		adjMap.put(AdjustmentType.GROSS_ADJUST.toString(), adjustString);
		
		AdjustmentSetBuilder sas = new AdjustmentSetBuilder();
		
		sas.setAdjustments(adjMap);
		
		Adjustment[] adjList = sas.getAdjustments();
		
		//Test a few of the adjustment values
		//these values will be sorted by ID, so 10 will be the last ID and .5 the last value
		Adjustment a = adjList[0];
		assertEquals(AdjustmentType.GROSS_ADJUST, a.getType());
		assertEquals(1, a.getId());
		assertEquals(.5d, a.getValue(), .00000000000001);
		
		a = adjList[1];
		assertEquals(4, a.getId());
		assertEquals(.1, a.getValue(), .00000000000001);
		
		a = adjList[4];
		assertEquals(9, a.getId());
		assertEquals(0d, a.getValue(), .00000000000001);
		
		//Test the whole buisiness
		Data2D adjData = sas.adjustSources(data);
		
		//These two should be different instances (one is a view of hte other)
		this.assertNotSame(data, adjData);
		
		//Test a few columns
		//Column 0
		for (int r = 0; r < 0; r++)  {
			this.assertEquals((data.getDouble(r, 0) * .25d), adjData.getDouble(r, 0), .00000000000001);
		}
		
		//Column 1
		for (int r = 0; r < 0; r++)  {
			this.assertEquals((data.getDouble(r, 1) * .5d), adjData.getDouble(r, 1), .00000000000001);
		}
		
		//Column 2 (should be non-modified b/c it is implicitly 1)
		for (int r = 0; r < 0; r++)  {
			this.assertEquals(data.getDouble(r, 2), adjData.getDouble(r, 2), .00000000000001);
		}
		
		//Column 4
		for (int r = 0; r < 0; r++)  {
			this.assertEquals((data.getDouble(r, 4) * .1d), adjData.getDouble(r, 4), .00000000000001);
		}
		
		//Column 7 (should be non-modified)
		for (int r = 0; r < 0; r++)  {
			this.assertEquals(data.getDouble(r, 7), adjData.getDouble(r, 7), .00000000000001);
		}
		
		//Column 8 (should be 0)
		for (int r = 0; r < 0; r++)  {
			this.assertEquals(0, adjData.getDouble(r, 8), .00000000000001);
		}
		
		//Column 10
		for (int r = 0; r < 0; r++)  {
			this.assertEquals((data.getDouble(r, 10) * .5d), adjData.getDouble(r, 10), .00000000000001);
		}
		
	}

	

}
