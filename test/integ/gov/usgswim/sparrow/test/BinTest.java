package gov.usgswim.sparrow.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.cachefactory.BinningRequest;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BinTest {
	public static LifecycleListener lifecycle = new LifecycleListener();
	
	/** Acceptable error in the number of values in each bin. */
	public static final int EQUAL_COUNT_TOLERANCE = 3;
	
	/** Acceptable error in the range of values in each bin. */
	public static final double EQUAL_RANGE_TOLERANCE = 0.0001;
	
	@BeforeClass public static void setUpOnce() {
	    lifecycle.contextInitialized(null, true);
	}

	@AfterClass public static void tearDownOnce() {
		lifecycle.contextDestroyed(null, true);
	}
	
	/**
     * Ensure that specifying zero bins will throw an exception.
	 */
	@Test public void testZeroBins() throws Exception {
	    try {
	        @SuppressWarnings("unused")
            BinningRequest req = new BinningRequest(0, 0, BinningRequest.BIN_TYPE.EQUAL_COUNT);
	        fail("Requesting 0 bins did not throw an exception.");
	    } catch (IllegalArgumentException iae) {
	    }
	}
    
    /**
     * Validate that specifying one bin returns bin values that encompass the
     * entire range of data points.
     */
	@Test public void testEqualCountOneBin() throws Exception {
        // Pull prediction data from an empty context
        Integer contextId = buildPredictContextEmpty();
        float[] values = getPredictContextDataValues(contextId);
        
        // Request the bin values from the caching mechanism
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = (BigDecimal[]) sharedApp.getDataBinning(new BinningRequest(
                contextId, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT));
        
        // Test the min and max values
        // Note that if min and max values match, it follows that sequencing is
        // correct, all values will fit within the bin, and the bin count is correct.
        // The rounded range value of the bin should include all values
        assertTrue(bins[0].doubleValue() <= (double)values[0]);
        assertTrue(bins[bins.length - 1].doubleValue() >= (double)values[values.length - 1]);
        // Implied by previous assertions and sorting, but verifying
        assertTrue(bins[0].doubleValue() <= bins[bins.length - 1].doubleValue());
    }
    
    /**
     * Validate that specifying one bin returns bin values that encompass the
     * entire range of data points.
     */
	@Test public void testEqualRangeOneBin() throws Exception {
        // Pull prediction data from an empty context
        Integer contextId = buildPredictContextEmpty();
        float[] values = getPredictContextDataValues(contextId);
        
        // Request the bin values from the caching mechanism
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, 1, BinningRequest.BIN_TYPE.EQUAL_RANGE));
        
        // Test the min and max values
        // Note that if min and max values match, it follows that sequencing is
        // correct, all values will fit within the bin, and the bin range is correct.
        assertTrue(bins[0].doubleValue() <= (double)values[0]);
        assertTrue(bins[bins.length - 1].doubleValue() >= (double)values[values.length - 1]);
        // Implied by previous assertions and sorting, but verifying
        assertTrue(bins[0].doubleValue() <= bins[bins.length - 1].doubleValue());
    }
    
    /**
     * Validate that the first and last values specified by the bin boundaries
     * are equivalent to the first and last values in the prediction data.
     */
	@Test public void testEqualCountMinMax() throws Exception {
        // Pull prediction data from an empty context
        Integer contextId = buildPredictContextEmpty();
        float[] values = getPredictContextDataValues(contextId);
        
        // Setup a random number of bins picked from [2, 100)
        int binCount = ((int)(Math.random() * 98.0d)) + 2;
        
        // Request the bin values from the caching mechanism
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_COUNT));
        
        // Test the min and max values
        assertTrue(bins[0].doubleValue() <= (double)values[0]);
        assertTrue(bins[bins.length - 1].doubleValue() >= (double)values[values.length - 1]);
    }
	
    /**
     * Validate that the first and last values specified by the bin boundaries
     * are equivalent to the first and last values in the prediction data.
     */
	@Test public void testEqualRangeMinMax() throws Exception {
        // Pull prediction data from an empty context
        Integer contextId = buildPredictContextEmpty();
        float[] values = getPredictContextDataValues(contextId);
        
        // Setup a random number of bins picked from [2, 100)
        int binCount = ((int)(Math.random() * 98.0d)) + 2;
        
        // Request the bin values from the caching mechanism
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_RANGE));
        
        // Test the min and max values
        assertTrue(bins[0].doubleValue() <= (double)values[0]);
        assertTrue(bins[bins.length - 1].doubleValue() >= (double)values[values.length - 1]);
	}
	
	/**
	 * Ensure that the bin boundary values are sorted in ascending order.
	 */
	@Test public void testEqualCountSequence() throws Exception {
        // Setup a random number of bins picked from [2, 100)
	    // Note that sequencing for one bin was verified in {@code testEqualCountOneBin}
        int binCount = ((int)(Math.random() * 98.0d)) + 2;
        
        // Request the bin values from the caching mechanism
        Integer contextId = buildPredictContextEmpty();
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_COUNT));
        
        // Ensure the bins are sorted properly
        for (int i = 1; i < bins.length; i++) {
            boolean greater = bins[i].doubleValue() >= bins[i - 1].doubleValue();
            assertTrue(greater);
        }
	}
	
    /**
     * Ensure that the bin boundary values are sorted in ascending order.
     */
	@Test public void testEqualRangeSequence() throws Exception {
        // Setup a random number of bins picked from [2, 100)
        // Note that sequencing for one bin was verified in {@code testEqualRangeOneBin}
        int binCount = ((int)(Math.random() * 98.0d)) + 2;
        
        // Request the bin values from the caching mechanism
        Integer contextId = buildPredictContextEmpty();
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_RANGE));
        
        // Ensure the bins are sorted properly
        for (int i = 1; i < bins.length; i++) {
            boolean greater = bins[i].doubleValue() >= bins[i - 1].doubleValue();
            assertTrue(greater);
        }
	}
	
	/**
	 * Test equivalence of two {@code BinningRequest}s with identical
	 * constructor arguments.
	 */
	@Test public void testEquals() throws Exception {
        BinningRequest req = new BinningRequest(0, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        BinningRequest req2 = new BinningRequest(0, 1, BinningRequest.BIN_TYPE.EQUAL_COUNT);
        
        assertEquals(req, req2);
	}
	
	/**
	 * Test non-equivalence of two {@code BinningRequest}s with differing
	 * constructor arguments.
	 */
	@Test public void testNotEquals() throws Exception {
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
	@Test public void testEqualCountBinSize() throws Exception {
	    // Pull prediction data from an empty context
		Integer contextId = buildPredictContextEmpty();
		float[] values = getPredictContextDataValues(contextId);
        
        // Setup a random number of bins picked from [2, 100)
        int binCount = ((int)(Math.random() * 98.0d)) + 2;
        
        // Request the bin values from the caching mechanism
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_COUNT));
        
        // Make sure the correct number of bins were returned
        assertEquals(binCount + 1, bins.length);

        int binIndex = 1; // current bin index
        int targetCount = values.length / binCount; // target number of data points in a bin
        int count = 0; // number of data points in the current bin
        int adjustment = 0; // adjustment factor for sequences of equal values
        int leftoverAdj = 0; // used when length of sequence exceeds bin size

        // Iterate over the bins, determining whether the number of data points
        // between each boundary defined by the bins is approximately equal
        for (int i = 0; i < values.length; i++) {
            if ((double)values[i] <= bins[binIndex].doubleValue()) {
                if (count < targetCount) {
                    count++;
                } else {
                    // Handle sequences of equivalent values
                    adjustment++;
                }
            } else {
                // We've hit a breakpoint, validate the count
                assertEquals(count, targetCount, EQUAL_COUNT_TOLERANCE);

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
        assertEquals(count, targetCount, EQUAL_COUNT_TOLERANCE);
    }
	
	/**
	 * Test to determine whether the bins created by the equal range mechanism
	 * define bins of an approximately equal range of values.
	 */
	@Test public void testEqualRangeBinRange() throws Exception {
        // Setup a random number of bins picked from [2, 100)
        int binCount = ((int)(Math.random() * 98.0d)) + 2;

        // Request the bin values from the caching mechanism
        Integer contextId = buildPredictContextEmpty();
        SharedApplication sharedApp = SharedApplication.getInstance();
        BigDecimal[] bins = sharedApp.getDataBinning(new BinningRequest(
                contextId, binCount, BinningRequest.BIN_TYPE.EQUAL_RANGE));
        
        // Make sure the correct number of bins were returned
        assertEquals(binCount + 1, bins.length);

        double prev = 0.0; // previous bin value
        double cur = 0.0; // current bin value
        double targetDiff = (bins[0].doubleValue() - bins[bins.length - 1].doubleValue()) / (double)(binCount);

        // Iterate over the bins, determining if the range of values between two
        // bins falls within the tolerance
        for (int i = 0; i < bins.length; i++) {
            cur = bins[i].doubleValue();
            if (i > 1) {
                double diff = cur - prev;
                assertEquals(targetDiff, diff, EQUAL_RANGE_TOLERANCE);
            } else if (i == 1) {
                targetDiff = cur - prev;
            }
            prev = cur;
        }
    }
	
	public Integer buildPredictContextEmpty() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-empty.xml");
		String xml = TestHelper.readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		PredictContextRequest contextReq = pipe.parse(xml);
		
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        pipe.dispatch(contextReq, out);

        return contextReq.getPredictionContext().getId();
	}

	/**
	 * Returns a sorted list of data values contained within the prediction context
	 * of the specified {@code contextId}.
	 * 
	 * @param contextId Identifier for the prediction context.
	 * @return The list of data values contained within the prediction context
     *         of the specified {@code contextId}.
	 */
	public float[] getPredictContextDataValues(Integer contextId) throws Exception {
	    // Retrieve the data table from the prediction context id
        PredictionContext context = SharedApplication.getInstance().getPredictionContext(contextId);
        PredictionContext.DataColumn dc = context.getDataColumn();
        DataTable data = dc.getTable();
        
        // Export all values in the specified column to values[] and sort them
        int totalRows = data.getRowCount();
        float[] values = new float[totalRows];
        for (int r = 0; r < totalRows; r++) {
            values[r] = data.getFloat(r, dc.getColumn());
        }
        Arrays.sort(values);
        
        return values;
	}

}

