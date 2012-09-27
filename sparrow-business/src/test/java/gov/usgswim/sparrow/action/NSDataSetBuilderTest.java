package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.TopoDataComposit;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.SingleColumnOverrideDataTable;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.ArrayList;
import java.util.List;

import oracle.mapviewer.share.ext.NSDataSet;
import oracle.mapviewer.share.ext.NSRow;

import org.junit.Test;

/**
 * Compares calculated delivery fractions to hand calculated values stored in
 * a few text files.  The network is 'cut' at reach 9681 by turning off transport
 * at that reach so that the delivery below that point is a small set of reaches
 * that can be calculated by hand.
 * 
 * @author eeverman
 */
public class NSDataSetBuilderTest extends SparrowTestBase {
	
	static PredictData unmodifiedPredictData;
	static PredictData predictData;
	
	static DataTable stdData;
	static DataTable stdDelFracTo9682;
	static DataTable stdDelFracTo9674;
	static DataTable stdDelFracToBoth;
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//log.setLevel(Level.DEBUG);
		super.doOneTimeCustomSetup();
		
		
		//Lets hack the predictData to Turn off transport for the two
		//reaches above reach 9681
		unmodifiedPredictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = unmodifiedPredictData.getTopo();
		SparseOverrideAdjustment adjTopo = new SparseOverrideAdjustment(topo);
		adjTopo.setValue(0d, unmodifiedPredictData.getRowForReachID(9619), PredictData.TOPO_IFTRAN_COL);
		adjTopo.setValue(0d, unmodifiedPredictData.getRowForReachID(9100), PredictData.TOPO_IFTRAN_COL);
		
		predictData = new PredictDataImm(
				new TopoDataComposit(adjTopo), unmodifiedPredictData.getCoef(),
				unmodifiedPredictData.getSrc(),
				unmodifiedPredictData.getSrcMetadata(),
				unmodifiedPredictData.getDelivery(),
				unmodifiedPredictData.getModel());
		
		
	}
	
	@Test
	public void VerifyNonUpstreamReachesUseTheDefaultNAValue() throws Exception {

		//Use the default NAValue
		NSDataSet nsData = buildNSDataSetToTarget9682(null, false);
		
		int upstreamReachCount = 0;
		int nonUpstreamReachCount = 0;
		NSRow[] rows = nsData.getRows();
		
		for (NSRow row : rows) {

			long rowID = row.getKeyAttribute().getLong();
			
			
			if (rowID >= 9682L && rowID <= 9696) {
				//upstream reaches
				upstreamReachCount++;
				double value = row.get(1).getDouble();
				assertTrue(value > 0d);
			} else if (rowID >= 9674 && rowID <= 9680) {
				//non-upstream reaches
				nonUpstreamReachCount++;
				long value = row.get(1).getLong();
				assertTrue(value == NSDataSetBuilder.DEFAULT_NA_VALUE);
			}
			
		}
		
		//Insure we have found at least some reaches
		assertTrue(upstreamReachCount > 5);
		assertTrue(nonUpstreamReachCount > 5);
	}
	
	@Test
	public void VerifyNonUpstreamReachesUseTheNullNAValue() throws Exception {

		//Use the default NAValue
		NSDataSet nsData = buildNSDataSetToTarget9682(null, true);
		
		int upstreamReachCount = 0;
		int nonUpstreamReachCount = 0;
		
		NSRow[] rows = nsData.getRows();
		
		for (NSRow row : rows) {
			
			long rowID = row.getKeyAttribute().getLong();
			
			
			if (rowID >= 9682L && rowID <= 9696) {
				//upstream reaches
				upstreamReachCount++;
				double value = row.get(1).getDouble();
				assertTrue(value > 0d);
			} else if (rowID >= 9674 && rowID <= 9680) {
				//non-upstream reaches
				nonUpstreamReachCount++;
				assertTrue(row.get(1).isNull());
			}
			
		}
		
		//Insure we have found at least some reaches
		assertTrue(upstreamReachCount > 5);
		assertTrue(nonUpstreamReachCount > 5);
	}
	
	@Test
	public void VerifyNonUpstreamReachesUseACustomNAValue() throws Exception {

		Long CUSTOM_NA_VALUE = 99999L;
		
		//Use the default NAValue
		NSDataSet nsData = buildNSDataSetToTarget9682(CUSTOM_NA_VALUE, true);
		
		int upstreamReachCount = 0;
		int nonUpstreamReachCount = 0;
		NSRow[] rows = nsData.getRows();
		
		for (NSRow row : rows) {
			long rowID = row.getKeyAttribute().getLong();
			
			
			if (rowID >= 9682L && rowID <= 9696) {
				//upstream reaches
				upstreamReachCount++;
				double value = row.get(1).getDouble();
				assertTrue(value > 0d);
			} else if (rowID >= 9674 && rowID <= 9680) {
				//non-upstream reaches
				nonUpstreamReachCount++;
				assertTrue(CUSTOM_NA_VALUE.equals(row.get(1).getLong()));
			}
			
		}
		
		//Insure we have found at least some reaches
		assertTrue(upstreamReachCount > 5);
		assertTrue(nonUpstreamReachCount > 5);
	}
	
	private NSDataSet buildNSDataSetToTarget9682(Long naValue, boolean useNaValue) throws Exception {
		//Build the NSDataSet to a single target reach
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);
		TerminalReaches targets = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap();
		CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();
		
		hashAction.setPredictData(predictData);
		hashAction.setTargetReachIds(targets.asSet());
		ReachRowValueMap delHash = hashAction.run();
		
		delAction.setPredictData(predictData);
		delAction.setDeliveryFractionHash(delHash);
		ColumnData deliveryFrac = delAction.run();
		
		NSDataSetBuilder nsBuilder = new NSDataSetBuilder();
		
		SingleColumnOverrideDataTable dataTable = new SingleColumnOverrideDataTable(
				predictData.getTopo(),
				deliveryFrac, 4, null);
		SparrowColumnSpecifier dataColumn = new SparrowColumnSpecifier(dataTable, 4, 9999);
		
		
		if (useNaValue) {
			nsBuilder.setNAValue(naValue);
		}
		
		nsBuilder.setData(dataColumn);
		nsBuilder.setInclusionMap(delHash);
		NSDataSet nsData = nsBuilder.doAction();
		
		return nsData;
	}
	
}

