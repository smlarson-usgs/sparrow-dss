package gov.usgswim.sparrow.domain;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.test.SparrowTestBase;

import javax.xml.stream.XMLStreamReader;

import org.junit.Test;


public class PredictionContextTest extends SparrowTestBase {

	// ============
	// TEST METHODS
	// ============
	@Test
	public void testParseSourceShareComparisonXML() throws Exception {
		XMLStreamReader reader = getSharedXMLAsReader("predict-context-2.xml");
		reader.next();
		PredictionContext context = new PredictionContext();
		context.parse(reader);
		
		
		int initialId = context.getId();
		
		assertTrue(context.isValid());
		
		//Comparison
		assertTrue(context.getComparison() instanceof SourceShareComparison);
		assertEquals(ComparisonType.percent, context.getComparison().getComparisonType());
		
		//Analysis
		assertEquals(new Integer(3), context.getAnalysis().getSource());
		assertEquals(DataSeriesType.total, context.getAnalysis().getDataSeries());
		assertEquals(null, context.getAnalysis().getAggFunction());
		assertEquals(null, context.getAnalysis().getGroupBy());
		
		//Terminal Reaches
		assertEquals(0, context.getTerminalReaches().getReachIdsAsList().size());
		
		//Adjustment Groups
		assertEquals("accumulate", context.getAdjustmentGroups().getConflicts());
		assertEquals(null, context.getAdjustmentGroups().getDefaultGroup());
		assertEquals(1, context.getAdjustmentGroups().getReachGroups().size());
		assertEquals("Simple Adjustments", context.getAdjustmentGroups().getReachGroups().get(0).getName());
		
		/////////////////////////
		// Convert to no-source clone
		/////////////////////////
		context = context.getNoSourceClone();
		int noSourceId = context.getId();
		
		//Check that the ID changes
		assertTrue(initialId != noSourceId);
		
		//This should be the only thing that changes
		assertEquals(null, context.getAnalysis().getSource());
		
		//These should all be unchanged
		assertTrue(context.getComparison() instanceof SourceShareComparison);
		assertEquals(DataSeriesType.total, context.getAnalysis().getDataSeries());
		assertEquals(0, context.getTerminalReaches().getReachIdsAsList().size());
		assertEquals(1, context.getAdjustmentGroups().getReachGroups().size());
		
		/////////////////////////
		// Convert to no-comparison clone
		/////////////////////////
		context = context.getNoComparisonVersion();
		int noComparisonId = context.getId();
		
		//Check that the ID changes
		assertTrue(initialId != noComparisonId);
		assertTrue(noComparisonId != noSourceId);
		
		//This should be the only thing that changes
		assertEquals(ComparisonType.none, context.getComparison().getComparisonType());
		
		//These should all be unchanged
		assertEquals(null, context.getAnalysis().getSource());
		assertEquals(DataSeriesType.total, context.getAnalysis().getDataSeries());
		assertEquals(0, context.getTerminalReaches().getReachIdsAsList().size());
		assertEquals(1, context.getAdjustmentGroups().getReachGroups().size());
	}

}
