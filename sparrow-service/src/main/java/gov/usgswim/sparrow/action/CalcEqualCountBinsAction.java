package gov.usgswim.sparrow.action;

import gov.usgs.cida.binning.CalcEqualCountBins;
import gov.usgs.cida.binning.CalcEqualCountBins.PostsWrapper;
import gov.usgs.cida.binning.domain.BinSet;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.ReachRowValueMap;

import java.math.BigDecimal;

public class CalcEqualCountBinsAction extends Action<BinSet> {

	private CalcEqualCountBins delegate = null;
	
	
	public CalcEqualCountBinsAction() {
		delegate = new CalcEqualCountBins(); 
	}
	
	@Override
	public BinSet doAction() throws Exception {
		return delegate.doAction();
	}

	public void setDataColumn(SparrowColumnSpecifier dataColumn) {
		delegate.setDataColumn(dataColumn);
	}

	public void setValues(double[] values) {
		delegate.setValues(values);
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

	/**
	 * Max allowed number of iterations.  Directly tied to memory b/c each
	 * iteration records its state so it is not tried again.
	 */
	public int getMaxAllowedIterations() {
		return delegate.getMaxAllowedIterations();
	}

	/**
	 * Max allowed number of iterations.  Directly tied to memory b/c each
	 * iteration records its state so it is not tried again.
	 * @param maxAllowedIterations
	 */
	public void setMaxAllowedIterations(int maxAllowedIterations) {
		delegate.setMaxAllowedIterations(maxAllowedIterations);
	}

	/**
	 * The restartMultiplier times the number of bins is the number of times
	 * the main outer loop restarts the optimization search using the best
	 * post configuration found up to that point.  Restarting too many times
	 * means that a given pathway doesn't have time to settle to its best
	 * configuration.  Restarting too few times means that alternate pathways
	 * that may lead to better solutions are never tried.
	 * @return
	 */
	public int getRestartMultiplier() {
		return delegate.getRestartMultiplier();
	}

	/**
	 * The restartMultiplier times the number of bins is the number of times
	 * the main outer loop restarts the optimization search using the best
	 * post configuration found up to that point.  Restarting too many times
	 * means that a given pathway doesn't have time to settle to its best
	 * configuration.  Restarting too few times means that alternate pathways
	 * that may lead to better solutions are never tried.
	 * 
	 * @param restartMultiplier
	 */
	public void setRestartMultiplier(int restartMultiplier) {
		delegate.setRestartMultiplier(restartMultiplier);
	}

	public double getBestScore() {
		return delegate.getBestScore();
	}

	public double getTotalIterations() {
		return delegate.getTotalIterations();
	}

	public void setUseEqualCountStartPosts(boolean useEqualCountStartPosts) {
		delegate.setUseEqualCountStartPosts(useEqualCountStartPosts);
	}

	public boolean isUseEqualCountStartPosts() {
		return delegate.isUseEqualCountStartPosts();
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
