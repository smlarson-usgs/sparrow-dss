package gov.usgswim.sparrow.action;

import gov.usgs.cida.binning.CalcEqualRangeBins;
import gov.usgs.cida.binning.domain.BinSet;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.ReachRowValueMap;

import java.math.BigDecimal;

public class CalcEqualRangeBinsAction extends Action<BinSet> {
	private CalcEqualRangeBins delegate = null;
	
	
	public CalcEqualRangeBinsAction() {
		delegate = new CalcEqualRangeBins(); 
	}
	
	@Override
	public BinSet doAction() throws Exception {
		return delegate.doAction();
	}

	public void setDataColumn(SparrowColumnSpecifier dataColumn) {
		delegate.setDataColumn(dataColumn);
	}

	public void setMinValue(BigDecimal minValue) {
		delegate.setMinValue(minValue);
	}

	public void setMaxValue(BigDecimal maxValue) {
		delegate.setMaxValue(maxValue);
	}

	public void setBinCount(int binCount) {
		delegate.setBinCount(binCount);
	}

	public void setDetectionLimit(BigDecimal detectionLimit) {
		delegate.setDetectionLimit(detectionLimit);
	}

	public void setMaxDecimalPlaces(Integer maxDecimalPlaces) {
		delegate.setMaxDecimalPlaces(maxDecimalPlaces);
	}

	public boolean isBottomUnbounded() {
		return delegate.isBottomUnbounded();
	}

	public void setBottomUnbounded(boolean bottomUnbounded) {
		delegate.setBottomUnbounded(bottomUnbounded);
	}

	public boolean isTopUnbounded() {
		return delegate.isTopUnbounded();
	}

	public void setTopUnbounded(boolean topUnbounded) {
		delegate.setTopUnbounded(topUnbounded);
	}
	
	public void setInclusionMap(ReachRowValueMap inclusionMap) {
		delegate.setInclusionMap(inclusionMap);
	}

	public ReachRowValueMap getInclusionMap() {
		return delegate.getInclusionMap();
	}
}
