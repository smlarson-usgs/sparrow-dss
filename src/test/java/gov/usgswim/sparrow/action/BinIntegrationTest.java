package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class BinIntegrationTest extends SparrowServiceTestBaseWithDBandCannedModel50 {
	
	private static final String PREDICT_CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";

	/** Acceptable error in the range of values in each bin. */
	public static final double EQUAL_RANGE_TOLERANCE = 0.0001;
	
	/**
     * Ensure that specifying zero bins will throw an exception.
	 */
	@Test(expected=IllegalArgumentException.class)
	public void testZeroBins() throws Exception {
        BinningRequest req = new BinningRequest(0, 0, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        fail("Requesting 0 bins did not throw an exception.");
	}
    
    /**
     * Validate that specifying one bin returns bin values that encompass the
     * entire range of data points.
     */
	@Test
	public void testEqualCountOneBin() throws Exception {

        Integer contextId = registerBasicPredictContext();
        
        float[] values = getSortedResultValues(contextId);
        
        // Request the bin values from the caching mechanism
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT)).getActualPostValues();
        
        assertTrue(bins.length == 2);
        
        // Test the min and max values
        // Note that if min and max values match, it follows that sequencing is
        // correct, all values will fit within the bin, and the bin count is correct.
        // The rounded range value of the bin should include all values
        assertTrue(bins[0].doubleValue() <= values[0]);
        assertTrue(bins[bins.length - 1].doubleValue() >= values[values.length - 1]);
        // Implied by previous assertions and sorting, but verifying
        assertTrue(bins[0].doubleValue() <= bins[bins.length - 1].doubleValue());
    }
    
    /**
     * Validate that specifying one bin returns bin values that encompass the
     * entire range of data points.
     */
	@Test
	public void testEqualRangeOneBin() throws Exception {
		
		Integer contextId = registerBasicPredictContext();
        
        float[] values = getSortedResultValues(contextId);
        
        // Request the bin values from the caching mechanism
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, 1, BinningRequest.BIN_TYPE.EQUAL_RANGE)).getActualPostValues();
        
        assertTrue(bins.length == 2);
        
        // Test the min and max values
        // Note that if min and max values match, it follows that sequencing is
        // correct, all values will fit within the bin, and the bin range is correct.
        assertTrue(bins[0].doubleValue() <= values[0]);
        assertTrue(bins[bins.length - 1].doubleValue() >= values[values.length - 1]);
        // Implied by previous assertions and sorting, but verifying
        assertTrue(bins[0].doubleValue() <= bins[bins.length - 1].doubleValue());
    }
    
    /**
     * Validate that the first and last values specified by the bin boundaries
     * are equivalent to the first and last values in the prediction data.
     */
	@Test
	public void testEqualCountMinMax() throws Exception {

        Integer contextId = registerBasicPredictContext();
        float[] values = getSortedResultValues(contextId);
        
        for (int binCount = 2; binCount < 150 ; binCount++) {
            // Request the bin values from the caching mechanism
            SharedApplication sharedApp = SharedApplication.getInstance();
            BigDecimal[] eqcBins = sharedApp.getDataBinning(new BinningRequest(
                    contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_COUNT)).getActualPostValues();
            BigDecimal[] eqrBins = sharedApp.getDataBinning(new BinningRequest(
                    contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_RANGE)).getActualPostValues();
            
            //Test eq count values
            assertTrue(eqcBins.length == binCount + 1);
            assertTrue(eqcBins[0].doubleValue() <= values[0]);
            assertTrue(eqcBins[eqcBins.length - 1].doubleValue() >= values[values.length - 1]);
            for (int i = 1; i < eqcBins.length; i++) {
                boolean greater = eqcBins[i].doubleValue() >= eqcBins[i - 1].doubleValue();
                assertTrue(greater);
            }
            
            //Test eq range values
            assertTrue(eqrBins.length == binCount + 1);
            assertTrue(eqrBins[0].doubleValue() <= values[0]);
            assertTrue(eqrBins[eqrBins.length - 1].doubleValue() >= values[values.length - 1]);
            for (int i = 1; i < eqrBins.length; i++) {
                boolean greater = eqrBins[i].doubleValue() >= eqrBins[i - 1].doubleValue();
                assertTrue(greater);
            }
        }

        

    }
	

	
	/**
	 * Test equivalence of two {@code BinningRequest}s with identical
	 * constructor arguments.
	 */
	@Test
	public void testEquals() throws Exception {
        BinningRequest req = new BinningRequest(0, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        BinningRequest req2 = new BinningRequest(0, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        
        assertEquals(req, req2);
	}
	
	/**
	 * Test non-equivalence of two {@code BinningRequest}s with differing
	 * constructor arguments.
	 */
	@Test
	public void testNotEquals() throws Exception {
        BinningRequest req = new BinningRequest(0, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        BinningRequest req2 = new BinningRequest(1, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        assertFalse(req.equals(req2));
        
        req = new BinningRequest(0, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        req2 = new BinningRequest(0, 2, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        assertFalse(req.equals(req2));
        
        req = new BinningRequest(0, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        req2 = new BinningRequest(0, 1, BinningRequest.BIN_TYPE.EQUAL_RANGE);
        assertFalse(req.equals(req2));
	}
	
	/**
	 * Test to determine whether the bins created by the equal count mechanism
	 * define bins with an approximately equal number of values.
	 */
	@Test
	public void testEqualCountBinSize() throws Exception {
	    // Pull prediction data from an empty context
		Integer contextId = registerBasicPredictContext();
		float[] values = getSortedResultValues(contextId);
        
        // Setup a random number of bins picked from [2, 100)
        int binCount = ((int)(Math.random() * 98.0d)) + 2;
        
        // Request the bin values from the caching mechanism
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_COUNT)).getActualPostValues();
        
        // Make sure the correct number of bins were returned
        assertEquals(binCount + 1, bins.length);

        int binIndex = 1; // current bin index
        int targetCount = values.length / binCount; // target number of data points in a bin
        int tolerance = (int) Math.ceil(targetCount * .2d);
        int count = 0; // number of data points in the current bin
        int adjustment = 0; // adjustment factor for sequences of equal values
        int leftoverAdj = 0; // used when length of sequence exceeds bin size

        // Iterate over the bins, determining whether the number of data points
        // between each boundary defined by the bins is approximately equal
        for (int i = 0; i < values.length; i++) {
            if (values[i] <= bins[binIndex].doubleValue()) {
                if (count < targetCount) {
                    count++;
                } else {
                    // Handle sequences of equivalent values
                    adjustment++;
                }
            } else {
                // We've hit a breakpoint, validate the count
                assertEquals(count, targetCount, tolerance);

                if (adjustment > targetCount) {
                    leftoverAdj = adjustment - targetCount;
                    adjustment = 0;
                    count = targetCount;
                } else if (leftoverAdj > targetCount) {
                    adjustment = 0;
                    leftoverAdj = leftoverAdj - targetCount;
                    count = targetCount;
                } else {
                    count = leftoverAdj + adjustment + 1;
                    adjustment = 0;
                    leftoverAdj = 0;
                }
                binIndex++;
            }
        }
        
        // Be sure to validate the count of the last bin
        assertEquals(count, targetCount, tolerance);
    }
	
	/**
	 * Test to determine whether the bins created by the equal range mechanism
	 * define bins of an approximately equal range of values.
	 */
	//This test no longer works b/c the new binning is not arbitrarily precise.
	//As more bins are added, we only resolve the bins down to a specific decimal
	//place, so we cannot expect that the equal range bins will really remain
	//equal range for the data.
	//@Test
	public void testEqualRangeBinRange() throws Exception {

        Integer contextId = registerBasicPredictContext();
        
        for (int binCount = 2; binCount < 150; binCount++) {
            SharedApplication sharedApp = SharedApplication.getInstance();
            BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                    contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_RANGE)).getActualPostValues();
            
            // Make sure the correct number of bins were returned
            assertEquals(binCount + 1, bins.length);

            double prev = 0.0; // previous bin value
            double cur = 0.0; // current bin value
            double targetDiff = (bins[0].doubleValue() - bins[bins.length - 1].doubleValue()) / (binCount);

            // Iterate over the bins, determining if the range of values between two
            // bins falls within the tolerance
            for (int i = 0; i < bins.length; i++) {
                cur = bins[i].doubleValue();
                if (i > 1) {
                    double diff = cur - prev;
                    assertEquals(targetDiff, diff, EQUAL_RANGE_TOLERANCE * targetDiff);
                } else if (i == 1) {
                    targetDiff = cur - prev;
                }
                prev = cur;
            }
        }
    }
	
	
	public Integer registerBasicPredictContext() throws Exception {
		String requestText = getSharedTestResource("predict-context-no-adj.xml");
		WebRequest request = new PostMethodWebRequest(PREDICT_CONTEXT_SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
		
		String contextIdString = getXPathValue("//*[local-name() = 'PredictionContext-response' ]/@context-id", actualResponse);
        Integer contextId = Integer.parseInt(contextIdString);
        return contextId;
	}

	/**
	 * Returns a sorted list of data values contained within the prediction context
	 * of the specified {@code contextId}.
	 * 
	 * @param contextId Identifier for the prediction context.
	 * @return The list of data values contained within the prediction context
     *         of the specified {@code contextId}.
	 */
	public float[] getSortedResultValues(Integer contextId) throws Exception {
	    // Retrieve the data table from the prediction context id
        PredictionContext context = SharedApplication.getInstance().getPredictionContext(contextId);
        SparrowColumnSpecifier dc = context.getDataColumn();
        
        // Export all values in the specified column to values[] and sort them
        int totalRows = dc.getRowCount();
        float[] values = new float[totalRows];
        for (int r = 0; r < totalRows; r++) {
            values[r] = dc.getDouble(r).floatValue();
        }
        Arrays.sort(values);
        
        return values;
	}

}

