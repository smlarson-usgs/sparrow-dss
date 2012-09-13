package gov.usgswim.sparrow.validation.tests;

import gov.usgswim.sparrow.validation.tests.TestResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.ColumnDataWritable;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.impl.StandardLongColumnData;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
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
 * Checks that the database model's FRAC values total to ONE at each fnode.
 * 
 * @author eeverman
 */
public class FracValuesShouldTotalToOne extends SparrowModelValidationBase {
	
	//Default fraction that the value may vary from the expected value.
	public static final double ALLOWED_FRACTIONAL_VARIANCE = .00000001D;
	
	private double allowedFractialVariance = ALLOWED_FRACTIONAL_VARIANCE;
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return false; }
	
	
	public FracValuesShouldTotalToOne() {}
	
	public FracValuesShouldTotalToOne(double allowedFractialVariance) {
		this.allowedFractialVariance = allowedFractialVariance;
	}
	
	public TestResult testModel(Long modelId) throws Exception {
		
		PredictData predictData = SharedApplication.getInstance().getPredictData(modelId);
		DataTable topo = predictData.getTopo();
		
		for (int row = 0; row < topo.getRowCount(); row++) {
			Long reachId = predictData.getIdForRow(row);
			Integer fnode = topo.getInt(row, PredictData.TOPO_FNODE_COL);
			
			Boolean isShoreReach = topo.getInt(row, PredictData.TOPO_SHORE_REACH_COL) == 1;
			Boolean ifTran = topo.getInt(row, PredictData.TOPO_IFTRAN_COL) == 1;
				
			
			if (!isShoreReach) {
				//This is a regular reach
				
				int[] allReachesAtFromFnode = topo.findAll(PredictData.TOPO_FNODE_COL, fnode);

				if (allReachesAtFromFnode.length == 0) {
					this.recordTestException(modelId, null, 
							"Could not find any reaches with this fnode '" + fnode + "' for reach id " + reachId + " at row " + row);
					continue;
				}

				double fracTotal = 0d;

				for (int i = 0; i < allReachesAtFromFnode.length; i++) {
					double thisFrac = topo.getDouble(allReachesAtFromFnode[i], PredictData.TOPO_FRAC_COL);
					fracTotal+= thisFrac;
				}

				if (! comp(1d, fracTotal, allowedFractialVariance)) {

					if (allReachesAtFromFnode.length == 1) {
						this.recordRowError(modelId, reachId, row, "1", fracTotal, "1", "db frac total", isShoreReach, ifTran, 
								"FRAC total != 1 for SINGLE REACH.  FNODE: " + fnode);
					} else {
						this.recordRowError(modelId, reachId, row, "1", fracTotal, "1", "db frac total", isShoreReach, ifTran, 
								"FRAC total != 1 for " + allReachesAtFromFnode.length + " reach diversion.  FNODE: " + fnode);
					}

				}
			
			} else {
				//This is a shore reach - it doesn't really matter what the FRAC is,
				//but it probably should be one.
				double thisFrac = topo.getDouble(row, PredictData.TOPO_FRAC_COL);
				
				if (! comp(1d, thisFrac, allowedFractialVariance)) {
					this.recordRowError(modelId, reachId, row, "1", thisFrac, "1", "db frac total", isShoreReach, ifTran, 
							"FRAC total != 1 for SINGLE SHORE REACH.  FNODE: " + fnode);
				}
			}
		}
		
		
		return result;
	}
	
	
}

