package gov.usgswim.sparrow.action;

import java.util.List;

import gov.usgs.cida.datatable.ColumnAttribs;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.adjustment.ColumnCoefAdjustment;
import gov.usgs.cida.datatable.adjustment.SparseCoefficientAdjustment;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.datatable.PercentageColumnData;
import gov.usgswim.sparrow.datatable.SingleColumnOverrideDataTable;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.ReachClientId;
import gov.usgswim.sparrow.service.SharedApplication;

/**
 * Adjusts the passed predict data sources to reflect the new
 * source values specified in the AdjustmentGroups.
 * 
 * If there are no adjustments, the original sources will be returned.
 * 
 * @author eeverman
 *
 */
public class CalcAdjustedSources extends Action<DataTable> {


	protected PredictData predictData;
	
	protected AdjustmentGroups adjustments;
	
	protected String msg;
	
	@Override
	protected String getPostMessage() {
		return msg;
	}

	@Override
	public DataTable doAction() throws Exception {
		DataTable adjusted = predictData.getSrc();
		ReachGroup defaultGroup = adjustments.getDefaultGroup();
		List<ReachGroup> reachGroups = adjustments.getReachGroups();
		ReachGroup individualGroup = adjustments.getIndividualGroup();

		//Do model-wide adjustments first.  Any further adjustments will accumulate/override as appropriate
		if (defaultGroup != null && defaultGroup.getAdjustments().size() > 0 && defaultGroup.isEnabled()) {

			ColumnCoefAdjustment colAdj = new ColumnCoefAdjustment(adjusted);
			adjusted = colAdj;

			for (Adjustment adj: defaultGroup.getAdjustments()) {
				Double coef = adj.getCoefficient();
				Integer srcId = adj.getSource();

				//Logic check...
				if (coef == null || srcId == null) {
					throw new Exception("For a global adjustment, a source and coefficient must be specified");
				}

				colAdj.setColumnMultiplier(predictData.getSourceIndexForSourceID(srcId), coef);

			}
		}
		
		//TODO: This seems wasteful - shouldn't we only create these types of
		//adjustments if there actually are adjustments of the specific type?
		
        //Two places to adjust:  SparseCoeff allows coeff adjustments to individual
        //reaches, SparesOverride, which wraps coef, allows absolute value adjustments
        //to individual reaches.
        SparseCoefficientAdjustment coefAdj = new SparseCoefficientAdjustment(adjusted);
        SparseOverrideAdjustment overAdj = new SparseOverrideAdjustment(coefAdj);

		//Loop thru ReachGroups to do adjustments
		//Here we are assuming conflict accumulate
		if (reachGroups != null && reachGroups.size() > 0) {

			for (ReachGroup rg: reachGroups) {
				if (rg.isEnabled()) {

					List<Adjustment> adjustments = rg.getAdjustments();

					// Apply ReachGroup-wide adjustments to all reaches in the combined reaches.
					if (adjustments != null) {
						// Look up corresponding source indices for each adjustment to save some lookups
						int[] adjSourceColumn = new int[rg.getAdjustments().size()];
						for (int i=0; i<adjustments.size(); i++) {
							adjSourceColumn[i] = predictData.getSourceIndexForSourceID(rg.getAdjustments().get(i).getSource());
						}

						// Apply the adjustments to each reach in the combined logical and explicit reaches
						for (Long reachID: rg.getCombinedReachIDs()) {
							int row = predictData.getRowForReachID(reachID);
							for (int i=0; i<adjustments.size(); i++) {
								applyAdjustmentToReach(adjustments.get(i), row, adjSourceColumn[i], coefAdj, overAdj, false);
							}
						}
					}
				}
			}
		}

		// Do individual reach adjustments
		if (individualGroup != null
		        && individualGroup.getExplicitReaches().size() > 0
		        && individualGroup.isEnabled()) {

		    // Iterate over the explicit set of reaches and apply adjustments
				for (ReachElement r: individualGroup.getExplicitReaches()) {
					
					String clientReachId = r.getId();
					ReachFullId rfi = SharedApplication.getInstance().getReachFullId(
							new ReachClientId(predictData.getModel().getId(), clientReachId));
					
					int row = predictData.getRowForReachID(rfi.getReachId());
					// Apply the adjustments specified for just this reach (if any)
					// Note:  getAdjustments() never returns null
					for (Adjustment adj: r.getAdjustments()) {
							Integer srcId = adj.getSource();
							applyAdjustmentToReach(adj, row, predictData.getSourceIndexForSourceID(srcId), coefAdj, overAdj, true);
					}
				}
		}

		adjusted = overAdj; //resulting adjustment
		return adjusted;
	}
	
	/**
	 * Applies an adjustment to an individual reach.
	 * 
	 * Note that an override adjustments always replaces an existing value and
	 * will cause any existing coef adjustment for that reach/source to be ignored.
	 * 
	 * Coef adjustments can either replace existing coefs or be multiplied by
	 * existing coefs (see params).
	 * 
	 * @param adj The adjustment to apply
	 * @param row The row the adjustment should be applied to
	 * @param col The column index the adjustment should be applied to
	 * @param coefAdj The Table containing the current coefficient adjustments
	 * @param overAdj The table containing the current override (absolute) adjustments
	 * @param coefOverridesPrevious If true, the new coef value should replace any existing coefs.  If false, multiply w/ exising coefs.
	 * @throws Exception
	 */
	private void applyAdjustmentToReach(Adjustment adj, int row, int col,
			SparseCoefficientAdjustment coefAdj, SparseOverrideAdjustment overAdj,
			boolean coefOverridesPrevious) throws Exception {
		
		Double coef = adj.getCoefficient();

		if (coef != null) {

			//Coef's can override previously set coefs or can be multiplied in
			//to the existing coef value.
			if (! coefOverridesPrevious) {
				Number existingCoef = coefAdj.getCoef(row , col);
				if (existingCoef != null) {
					coef = coef.doubleValue() * existingCoef.doubleValue();
				}
			}

			coefAdj.setValue(coef, row, col);
		} else {
			Double abs = adj.getAbsolute();
			overAdj.setValue(abs, row, col);
		}
	}


	public void setPredictData(PredictData predictData) {
		this.predictData = predictData;
	}

	public void setAdjustments(AdjustmentGroups adjustments) {
		this.adjustments = adjustments;
	}



}
