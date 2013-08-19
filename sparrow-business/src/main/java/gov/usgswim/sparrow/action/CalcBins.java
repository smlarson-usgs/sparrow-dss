package gov.usgswim.sparrow.action;

import gov.usgs.cida.binning.domain.BinSet;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.ComparisonType;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.request.BinningRequest;

import java.math.BigDecimal;

/**
 * This action creates a binning array based on a request from EHCache.
 *
 * Binning is the process of creating bins for a set of data.  For instance,
 * this data:<br>
 * <code>1, 2, 2, 9, 20, 29</code><br>
 * could be broken into two bins containing three values each based on Equal Count as:
 * <li>bin 1:  1 to 2 (inclusive)
 * <li>bin 2:  2 to 29
 * Equal Range binning for two bins would result in:
 * <li>bin 1:  1 to 15
 * <li>bin 2:  15 to 29
 *
 * @author eeverman
 */
public class CalcBins extends Action<BinSet> {
	
	/**
	 * The string to use to represent a bottom unbounded value.
	 * This is what the user would see in the legend for the bottom bound.
	 */
	public static final String BOTTOM_UNBOUNDED_STR = "<";
	
	/**
	 * The string to use to represent a top unbounded value.
	 * This is what the user would see in the legend for the top bound.
	 */
	public static final String TOP_UNBOUNDED_STR = ">";
	
	
	private BinningRequest request;
	private SparrowColumnSpecifier dataColumn;
	
	
	/** A hash of row numbers that are in the reaches to be mapped. **/
	private ReachRowValueMap inclusionMap;
	
    public CalcBins() {

    }
    
	@Override
	public BinSet doAction() throws Exception {
		
		
		boolean isComparison = ! ComparisonType.none.equals(request.getComparison());
		
		switch (request.getBinType()) {
		case EQUAL_RANGE: {
			
			CalcEqualRangeBinsAction action = new CalcEqualRangeBinsAction();
			action.setBinCount(request.getBinCount());
			action.setDataColumn(dataColumn);
			action.setDetectionLimit(request.getDetectionLimit());
			action.setMaxDecimalPlaces(request.getMaxDecimalPlaces());
			action.setBottomUnbounded(!isComparison);
			action.setTopUnbounded(!isComparison);
			action.setInclusionMap(inclusionMap);
			
			BinSet binSet = action.run();
			
			return binSet;
		}
			//break;
		case EQUAL_COUNT: {
			
			CalcEqualCountBinsAction action = new CalcEqualCountBinsAction();
			action.setBinCount(request.getBinCount());
			action.setDataColumn(dataColumn);
			action.setDetectionLimit(request.getDetectionLimit());
			action.setMaxDecimalPlaces(request.getMaxDecimalPlaces());
			action.setBottomUnbounded(!isComparison);
			action.setTopUnbounded(!isComparison);
			action.setInclusionMap(inclusionMap);
			
			BinSet binSet = action.run();
			
			return binSet;
		}
			//break;
		default:
		}
		
		return null;
	}
	
	
	/**
	 * Returns true if the bottom bound should be unbounded.
	 * 
	 * @return
	 */
	protected boolean isBottomUnbounded() {
		return request.getComparison().equals(ComparisonType.none);
	}
	
	/**
	 * Returns true if the top bound should be unbounded.
	 * @return
	 */
	protected boolean isTopUnbounded() {
		return request.getComparison().equals(ComparisonType.none);
	}
	
	/**
	 * Get the detection limit, considering the detection limit reported by 
	 * the model for this constituent and data series, as well as if we are
	 * doing a comparison.
	 * 
	 * @return
	 */
	protected BigDecimal getDetectionLimit() {
		if (request.getComparison().equals(ComparisonType.none)) {
			return request.getDetectionLimit();
		} else {
			return null;
		}
	}
	

	/**
	 * @return the request
	 */
	public BinningRequest getRequest() {
		return request;
	}

	/**
	 * @param request the request to set
	 */
	public void setRequest(BinningRequest request) {
		this.request = request;
	}

	/**
	 * @return the dataColumn
	 */
	public SparrowColumnSpecifier getDataColumn() {
		return dataColumn;
	}

	/**
	 * @param dataColumn the dataColumn to set
	 */
	public void setDataColumn(SparrowColumnSpecifier dataColumn) {
		this.dataColumn = dataColumn;
	}

	public void setInclusionMap(ReachRowValueMap inclusionMap) {
		this.inclusionMap = inclusionMap;
	}
	

	
}