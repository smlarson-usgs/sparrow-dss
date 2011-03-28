package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowServiceTestWithCannedModel50;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.AreaOfInterest;
import gov.usgswim.sparrow.domain.BasicAnalysis;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.NominalComparison;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;



/**
 * Minimal integration test.
 * 
 * @author eeverman
 */
public class CalcBinningLongRunTest  extends SparrowServiceTestWithCannedModel50 {
	
	/**
	 * Replicate a bug where the the uncertainty estimates can generate
	 * null values, which causes binning to die.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNullUncertaintyValues() throws Exception {
		
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total_std_error_estimate, null, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID,
				new AdjustmentGroups(TEST_MODEL_ID), analysis,
				new TerminalReaches(TEST_MODEL_ID), 
				new AreaOfInterest(TEST_MODEL_ID),
				NominalComparison.getNoComparisonInstance());
		
		SharedApplication.getInstance().putPredictionContext(context);
		BigDecimal[] bins = SharedApplication.getInstance().getDataBinning(new BinningRequest(
				context.getId(), 4, BinningRequest.BIN_TYPE.EQUAL_COUNT));
		
		float[] values = getSortedResultValues(context.getId());

		assertEquals(5, bins.length);

        assertTrue(bins[0].doubleValue() <= values[0]);
        assertTrue(bins[bins.length - 1].doubleValue() >= values[values.length - 1]);
        for (int i = 1; i < bins.length; i++) {
            boolean greater = bins[i].doubleValue() >= bins[i - 1].doubleValue();
            assertTrue(greater);
        }
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
        	if (dc.getDouble(r) != null) {
        		values[r] = dc.getDouble(r).floatValue();
        	}
        }
        Arrays.sort(values);
        
        return values;
	}
	
}

