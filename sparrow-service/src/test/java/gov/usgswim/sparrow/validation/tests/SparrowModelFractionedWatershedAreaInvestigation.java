package gov.usgswim.sparrow.validation.tests;

import gov.usgswim.sparrow.validation.tests.TestResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardLongColumnData;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.*;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.domain.reacharearelation.AreaRelation;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.domain.reacharearelation.ReachAreaRelations;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.ModelAggregationRequest;
import gov.usgswim.sparrow.request.ModelHucsRequest;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.extras.DOMConfigurator;

/**
 * Compares the db value for cumulative watershed area to the aggregated value,
 * built by adding up all the catchments upstream of the reach.
 * 
 * @author eeverman
 */
public class SparrowModelFractionedWatershedAreaInvestigation extends SparrowModelValidationBase {
	
	//Default fraction that the value may vary from the expected value.
	public static final double ALLOWED_FRACTIONAL_VARIANCE = .1D;
	
	private double allowedFractialVariance = ALLOWED_FRACTIONAL_VARIANCE;
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	
	public TestResult testModel(Long modelId) throws Exception {
		return compareFractionedWatershedAreasToUnfractioned(modelId);
	}
	
	
		/**
	 * Runs QA checks against the data.
	 * @param modelId
	 * @return
	 * @throws Exception
	 */
	public TestResult compareFractionedWatershedAreasToUnfractioned(Long modelId) throws Exception {
		
		DataTable incrementalAreasFromDb = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(modelId, AggregationLevel.REACH, false));
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		DataTable topo = predictData.getTopo();
		
		for (int row = 0; row < topo.getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			
			
			//Calculate the fractioned watershed area, skipping the cache
			CalcReachAreaFractionMap areaMapAction = new CalcReachAreaFractionMap(topo, reachId);
			ReachRowValueMap areaMap = areaMapAction.run();
		
			CalcFractionedWatershedArea fractionedAreaAction = new CalcFractionedWatershedArea(areaMap, incrementalAreasFromDb);
			Double fractionalWatershedArea = fractionedAreaAction.run();
			
			CalcFractionedWatershedArea unfractionedAreaAction = new CalcFractionedWatershedArea(areaMap, incrementalAreasFromDb, true);
			Double unfractionalWatershedArea = unfractionedAreaAction.run();

			if (! comp(fractionalWatershedArea, unfractionalWatershedArea, allowedFractialVariance)) {
				Boolean shoreReach = topo.getInt(row, PredictData.TOPO_SHORE_REACH_COL) == 1;
				Boolean ifTran = topo.getInt(row, PredictData.TOPO_IFTRAN_COL) == 1;
				recordRowError(modelId, reachId, row, fractionalWatershedArea, unfractionalWatershedArea, "frac", "unfrac", shoreReach, ifTran, "Fractioned Watershed area != unfractioned area.");
			}
			

		}
		
		
		return result;
	}
	
	
}

