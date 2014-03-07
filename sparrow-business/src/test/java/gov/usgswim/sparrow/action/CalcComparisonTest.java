package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.service.ConfiguredCache.DeliveryFraction;
import static gov.usgswim.sparrow.service.ConfiguredCache.PredictData;
import static gov.usgswim.sparrow.service.ConfiguredCache.PredictResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgs.cida.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.AreaOfInterest;
import gov.usgswim.sparrow.domain.BasicAnalysis;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.NominalComparison;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.junit.Before;
import org.junit.Test;



public class CalcComparisonTest  extends SparrowTestBase {
	
	PredictionContext compCtx;		//Full context w/ comparison
	PredictionContext noCompCtx;	//Same as above, but no comparison
	PredictionContext noSourceCtx;	//Same as noComp, but no sorce is spec'ed
	
	SparrowColumnSpecifier compResult;		//Result of compCtx CalcComparison action
	SparrowColumnSpecifier noCompResult;		//Result of noCompCtx CalcAnalysis
	SparrowColumnSpecifier noSourceResult;	//Result of noSourceResult CalcAnalysis
	
	@Before
	public void setup() throws Exception {
		
		//
		//Set up contexts
		XMLStreamReader reader = getSharedXMLAsReader("predict-context-2.xml");
		reader.next();
		compCtx = new PredictionContext();
		compCtx.parse(reader);
		
		reader = getSharedXMLAsReader("predict-context-2a.xml");
		reader.next();
		noCompCtx = new PredictionContext();
		noCompCtx.parse(reader);
		
		reader = getSharedXMLAsReader("predict-context-2b.xml");
		reader.next();
		noSourceCtx = new PredictionContext();
		noSourceCtx.parse(reader);
		
		//
		//Set up Results
		CalcComparison compAction = new CalcComparison();
		compAction.setContext(compCtx);
		compResult = compAction.run();
		
		CalcAnalysis noCompAnalysisAction = new CalcAnalysis();
		noCompAnalysisAction.setContext(noCompCtx);
		noCompResult = noCompAnalysisAction.run();
		
		CalcAnalysis noSourceAnalysisAction = new CalcAnalysis();
		noSourceAnalysisAction.setContext(noSourceCtx);
		noSourceResult = noSourceAnalysisAction.run();
	}
	
	@Test
	public void sourceShareComparisonTest() throws Exception {

		for (int r = 0; r < noCompResult.getRowCount(); r++) {
			double compValue = compResult.getDouble(r);
			
			double srcValue = noCompResult.getDouble(r);
			double noSrcValue = noSourceResult.getDouble(r);
			double expected = (srcValue / noSrcValue) * 100;
			
			//System.out.println("Expect / Found: " + expected + " / " + compValue);
			assertEquals(expected, compValue, .0000000001d);
			
		}
	}
	

	
}

