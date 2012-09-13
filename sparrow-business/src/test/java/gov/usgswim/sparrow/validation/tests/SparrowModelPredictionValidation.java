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
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;
import gov.usgswim.sparrow.validation.ValidationTestUtils;

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
import net.sf.ehcache.Element;

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
public class SparrowModelPredictionValidation extends BaseTextFileTester {
	
	//Default fraction that the value may vary from the expected value.
	public static final double ALLOWED_FRACTIONAL_VARIANCE = .00001D;
	
	private double allowedFractialVariance = ALLOWED_FRACTIONAL_VARIANCE;
	
	//This flags marks if we use the decayed incremental (the standard) or non-decayed.
	private boolean useDecayedIncremental = true;
	
	public boolean requiresDb() { return true; }
	public boolean requiresTextFile() { return true; }
	
	
	public SparrowModelPredictionValidation() {

	}
	
	/**
	 * 
	 * @param forceAllAreaFractionsToOne Set true to do non-fractional area calcs
	 */
	public SparrowModelPredictionValidation(boolean useDecayedIncremental) {
		this.useDecayedIncremental = useDecayedIncremental;
	}
	
	public SparrowModelPredictionValidation(Double allowedVariance, boolean useDecayedIncremental) {
		this.allowedFractialVariance = allowedVariance;
		this.useDecayedIncremental = useDecayedIncremental;
	}
	
	public TestResult testModel(Long modelId) throws Exception {

		DataTable txtModel = this.runner.loadModelTextFile(modelId);
		
		if (txtModel != null) {
			AdjustmentGroups emptyAdjustmentGroups = new AdjustmentGroups(modelId);
			PredictData dbPredictData = SharedApplication.getInstance().getPredictData(modelId);
			PredictResult dbPredictResult = SharedApplication.getInstance().getPredictResult(emptyAdjustmentGroups);
			
			if (txtModel.getRowCount() != dbPredictResult.getRowCount()) {
				recordTestException(modelId, null, "The rows in the db model do not match the text file.  Text: " + txtModel.getRowCount() + " rows, db: " + dbPredictResult.getRowCount());
			} else {
				testComparison(txtModel, dbPredictResult, dbPredictData, useDecayedIncremental, modelId);
			}
			
		} else {
			recordTestException(modelId, null, "Could not load the text version of the model from file.");
		}


		return result;
	}
	
	/**
	 * 
	 * @param txt
	 * @param pred
	 * @param predData
	 * @param useDecay
	 * @param modelId
	 * @return The number of comparison errors (zero if no errors)
	 * @throws Exception
	 */
	public void testComparison(DataTable txt, PredictResult pred, PredictData predData, boolean useDecay, long modelId) throws Exception {
		

		
		String idColStr = txt.getProperty(ValidationTestUtils.ID_COL_KEY);
		int idCol = Integer.parseInt(idColStr);
		
		int maxSrc = pred.getSourceCount();	//max source #
		
		
		for (int r = 0; r < txt.getRowCount(); r++) {
			
			//Instream decay, if it needs to be applied (1 otherwise)
			double instreamDecay = 1;
			if (useDecay) {
				instreamDecay =
					predData.getDelivery().getDouble(r, PredictData.INSTREAM_DECAY_COL);
			}
			
			Long id = pred.getIdForRow(r);
			
			int txtRow = txt.findFirst(idCol, id);
			
			if (txtRow < 0) {
				this.recordRowError(modelId, id, r, "This id (from the text file) could not be found in the db set of data.");
				continue;
			}
			
			
			boolean rowIsShoreReach = predData.getTopo().getInt(r, PredictData.TOPO_SHORE_REACH_COL) == 1;
			boolean rowIsIfTranOn = predData.getTopo().getInt(r, PredictData.TOPO_IFTRAN_COL) == 1;			
			boolean rowMatches = true;	//assume this row matches

			
			//Compare Incremental Values (c is column in std data)
			for (int s = 1; s <= maxSrc; s++) {
			
				boolean thisSourceIncMatched = true;
				
				double txtIncForSourcValue =
						txt.getDouble(txtRow, getIncCol(s, txt, predData));
				double txtTotalForSourceValue =
						txt.getDouble(txtRow, getTotalCol(s, txt, predData));

				
				double dbCalcedIncForSourceValue =
					pred.getDouble(r, pred.getIncrementalColForSrc(s)) * instreamDecay;
				double dbCalcedTotalForSourceValue =
					pred.getDouble(r, pred.getTotalColForSrc(s));
				
				if (! comp(txtIncForSourcValue, dbCalcedIncForSourceValue, allowedFractialVariance)) {
					
					//A bad source value is considered a complete independent error, reported individually
					this.recordRowError(modelId, id, r, txtIncForSourcValue, dbCalcedIncForSourceValue, 
							"text", "db", rowIsShoreReach, rowIsIfTranOn,
							"This incremental value for source " + s + " does not match.");
					
					thisSourceIncMatched = false;
					rowMatches = false;
				}
				
				if (! comp(txtTotalForSourceValue, dbCalcedTotalForSourceValue, allowedFractialVariance)) {

					if (thisSourceIncMatched) {
						//The total is off, but the source was OK.  Its probably due to
						//an error upstream, but mark it as a full error here
						this.recordRowError(modelId, id, r, txtTotalForSourceValue, dbCalcedTotalForSourceValue, 
								"text", "db", rowIsShoreReach, rowIsIfTranOn,
								"This total value for source " + s + " does not match.");
					} else {
						//The incremental value was off as well, so this is already reported.
						this.recordRowDebug(modelId, id, r, txtTotalForSourceValue, dbCalcedTotalForSourceValue, 
								"text", "db", rowIsShoreReach, rowIsIfTranOn,
								"This total value for source " + s + " does not match.  Likely due to other errors in this row.");
					}

					rowMatches = false;
				}
			}
			
			
			//Check the total (all sources together) values
			double txtIncValue =
					txt.getDouble(txtRow, getIncAllCol(txt));
			double txtTotalValue =
					txt.getDouble(txtRow, getTotalAllCol(txt));
			
			double dbCalcedIncValue =
					pred.getDouble(r, pred.getIncrementalCol()) * instreamDecay;
			double dbCalcedTotalValue =
					pred.getDouble(r, pred.getTotalCol());
			
			
			if (! comp(txtIncValue, dbCalcedIncValue, allowedFractialVariance)) {

				if (rowMatches) {
					//The total is off, but individual src values were OK, so treat this as a full error
					this.recordRowError(modelId, id, r, txtIncValue, dbCalcedIncValue, 
							"text", "db", rowIsShoreReach, rowIsIfTranOn,
							"The total incremental value (for all sources) does not match.");
				} else {
					//The incremental value was off as well, so this is already reported.
					this.recordRowDebug(modelId, id, r, txtIncValue, dbCalcedIncValue, 
							"text", "db", rowIsShoreReach, rowIsIfTranOn,
							"The total incremental value (for all sources) does not match.  Likely due to other errors in this row.");
				}

				rowMatches = false;
			}
			
			if (! comp(txtTotalValue, dbCalcedTotalValue, allowedFractialVariance)) {

				if (rowMatches) {
					//The total is off, but individual src values were OK, so treat this as a full error
					this.recordRowError(modelId, id, r, txtTotalValue, dbCalcedTotalValue, 
							"text", "db", rowIsShoreReach, rowIsIfTranOn,
							"The total value (for all sources) does not match.");
				} else {
					//The incremental value was off as well, so this is already reported.
					this.recordRowDebug(modelId, id, r, txtTotalValue, dbCalcedTotalValue, 
							"text", "db", rowIsShoreReach, rowIsIfTranOn,
							"The total value (for all sources) does not match.  Likely due to other errors in this row.");
				}

				rowMatches = false;
			}

		}
	}
	
	
}

