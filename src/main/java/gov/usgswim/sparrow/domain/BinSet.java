package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.request.BinningRequest.BIN_TYPE;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;

/**
 * Contains a set of bins and some metadata used to define how data values
 * are divided into ranges for theming during mapping and for legend generation.
 * 
 * In general the bottom bin is inclusive and the top bin is exclusive.
 * 
 * Ideally this class should contain little/no logic, however, it is currently
 * serving as a wrapper around some of the shortcomings of the CalcBinning
 * action.  I wanted to preserve the tests and currently working aspects of
 * that action while patching some of the poor aspects - specifically that the
 * bins sometimes do not include extreme values that fall just outside a 'nice'
 * edge bin.  For instance, a max value of 50.0000000001 may result in a top
 * bin of 50, which excludes the top values.
 * 
 * @author eeverman
 *
 */
@XStreamAlias("BinSet")
@XStreamInclude({Bin.class})
public class BinSet {

	private final Bin[] bins;
	
	@XStreamAsAttribute
	private final BIN_TYPE binType;
	
	/** True if a detection limit is used for the lowest bin */
	@XStreamAsAttribute
	public final boolean usesDetectionLimit;
	
	/** Actual Max - mostly for debug */
	@XStreamAsAttribute
	public final BigDecimal actualMax;
	
	/** Actual Min - mostly for debug */
	@XStreamAsAttribute
	public final BigDecimal actualMin;
	
	/** 
	 * The actual CUV used in building the posts.  Used later for
	 * formatting.
	 */
	@XStreamAsAttribute
	public final BigDecimal characteristicUnitValue;
	
	/** The requested number of bins */
	@XStreamAsAttribute
	public final Integer requestedBinCount;
	
	/** The actual number of bins created.
	 *  (may be less than requested for equal count bins */
	@XStreamAsAttribute
	public final Integer actualBinCount;
	
	/** The String pattern of the decimal formatter used to format the posts. */
	@XStreamAsAttribute
	public final String formatPattern;
	
	public BinSet(Bin[] bins, BIN_TYPE binType, InProcessBinSet inProcessBinSet, String formatPattern) {
		this.bins = bins;
		this.binType = binType;
		usesDetectionLimit = inProcessBinSet.usesDetectionLimit;
		actualMax = inProcessBinSet.actualMax;
		actualMin = inProcessBinSet.actualMin;
		characteristicUnitValue = inProcessBinSet.characteristicUnitValue;
		requestedBinCount = inProcessBinSet.requestedBinCount;
		actualBinCount = bins.length;
		this.formatPattern = formatPattern;
	}

	public Bin[] getBins() {
		return bins;
	}

	public BIN_TYPE getBinType() {
		return binType;
	}
	
	/**
	 * Util method to get just the BigDecimal fence post values.
	 * @return
	 */
	public BigDecimal[] getActualPostValues() {
		BigDecimal[] vals = new BigDecimal[bins.length + 1];
		
		vals[0] = bins[0].getBottom().getActual();
		for (int i = 0; i < bins.length; i++) {
			vals[i + 1] = bins[i].getTop().getActual();
		}
		
		return vals;
	}
	
	/**
	 * Util method to get just the Double fence post values.
	 * 
	 * These values may contain rounding errors, since they are converted from
	 * the true BigDecimal values.
	 * @return
	 */
	public Double[] getActualPostDoubles() {
		BigDecimal[] vals = getActualPostValues();
		Double[] dVals = new Double[vals.length];
		
		for (int i = 0; i < vals.length; i++) {
			dVals[i] = vals[i].doubleValue();
		}
		
		return dVals;
	}
	
	/**
	 * Util method to get just the formatted fence post values.
	 * @return
	 */
	public String[] getFormattedPostValues() {
		String[] vals = new String[bins.length + 1];
		
		vals[0] = bins[0].getBottom().getFormatted();
		for (int i = 0; i < bins.length; i++) {
			vals[i + 1] = bins[i].getTop().getFormatted();
		}
		
		return vals;
	}
	
	/**
	 * Util method to get just the formatted functional fence post values.
	 * @return
	 */
	public String[] getFormattedFunctionalPostValues() {
		String[] vals = new String[bins.length + 1];
		
		vals[0] = bins[0].getBottom().getFormattedFunctional();
		for (int i = 0; i < bins.length; i++) {
			vals[i + 1] = bins[i].getTop().getFormattedFunctional();
		}
		
		return vals;
	}
	
	/**
	 * Returns the index numbers of any bins that contain duplicate formatted
	 * values, if any.
	 * 
	 * Bins indicated by the returned array have formatted top and bottom bins
	 * that are the same.
	 * 
	 * @return
	 */
	public Integer[] getDuplicateFormattedBins() {
		ArrayList<Integer> dups = new ArrayList<Integer>();
		
		for (int i = 0; i < bins.length; i++) {
			if (bins[i].hasDuplicateFormattedBounds()) {
				dups.add(i);
			}
		}
		
		return dups.toArray(new Integer[dups.size()]);
	}
	
	/**
	 * Returns true if there are any bounds with duplicate formatted bounds.
	 * @return
	 */
	public boolean hasDuplicateFormattedBins() {
		return getDuplicateFormattedBins().length > 0;
	}
	
	/**
	 * Returns the index numbers of any bins that contain duplicate actual
	 * values, if any.
	 * 
	 * Bins indicated by the returned array have actual top and bottom bounds
	 * that are the same.
	 * 
	 * @return
	 */
	public Integer[] getDuplicateActualBins() {
		ArrayList<Integer> dups = new ArrayList<Integer>();
		
		for (int i = 0; i < bins.length; i++) {
			if (bins[i].hasDuplicateActualBounds()) {
				dups.add(i);
			}
		}
		
		return dups.toArray(new Integer[dups.size()]);
	}
	
	/**
	 * Returns true if there are any bounds with duplicate actual bounds.
	 * @return
	 */
	public boolean hasDuplicateActualBins() {
		return getDuplicateActualBins().length > 0;
	}
	
	/**
	 * Returns the percentage difference between the number of values in the
	 * bin with the most values wrt the bin with the smallest number of values.
	 * 
	 * If no bin counts have been assigned, null is returned.
	 * If all bins are empty, zero is returned.
	 * If the smallest bin contains zero bins, 100% (100) is returned.
	 * 
	 * @return
	 */
	public Double getBinCountMaxVariancePercentage() {
		int maxCount = 0;
		int minCount = 0;
		boolean hasCount = false;
		int startBin = (this.usesDetectionLimit)?1:0;
		
		for (int i=startBin; i<bins.length; i++) {
			Bin b = bins[i];
			Integer cnt = b.getValueCount();
			
			if (cnt != null) {
				
				if (hasCount == false) {
					maxCount = cnt;
					minCount = cnt;
				} else {
					if (cnt > maxCount) maxCount = cnt;
					if (cnt < minCount) minCount = cnt;
				}
				hasCount = true;
			} else {
				hasCount = false;
				break;
			}
		}
		
		//Special cases
		if (!hasCount) return null;
		if (maxCount == 0) return 0d;
		if (minCount == 0) return 100d;
		
		int diff = maxCount - minCount;
		double percent = ((double)diff / (double)minCount) * 100d;
		return percent;
	}
	
	/**
	 * Creates a new BinSet inferring some aspects based on the context.
	 * 
	 * @param ipbs An InProcessBinSet that contains actual bins, functional bins and detection limit info 
	 * @param formatter Formatter for the actual bins
	 * @param functionalFormatter Formatter for the functional bins
	 * @param bottomUnbounded True if the bottom bound should be unlimited
	 * @param topUnbounded True if the top bound should be unlimitted
	 * @param formattedDetectionLimit Formatted detection limit.
	 * @param binType Type of bins being created.
	 * @return
	 */
	public static BinSet createBins(InProcessBinSet ipbs, 
			DecimalFormat formatter, DecimalFormat functionalFormatter,
			boolean bottomUnbounded, boolean topUnbounded, String formattedDetectionLimit,
			BIN_TYPE binType) {
		return createBins(ipbs, formatter, functionalFormatter,
				bottomUnbounded, topUnbounded,
				formattedDetectionLimit, null, binType);
	}
	
	/**
	 * Creates a new BinSet inferring some aspects based on the context.
	 * 
	 * @param ipbs An InProcessBinSet that contains actual bins, functional bins and detection limit info 
	 * @param formatter Formatter for the actual bins
	 * @param functionalFormatter Formatter for the functional bins
	 * @param bottomUnbounded True if the bottom bound should be unlimited
	 * @param topUnbounded True if the top bound should be unlimitted
	 * @param formattedDetectionLimit Formatted detection limit.
	 * @param binCounts The number of values in each bin.  Optionally null.
	 * @param binType Type of bins being created.
	 * @return
	 */
	public static BinSet createBins(InProcessBinSet ipbs, 
			DecimalFormat formatter, DecimalFormat functionalFormatter,
			boolean bottomUnbounded, boolean topUnbounded, String formattedDetectionLimit,
			Integer[] binCounts, BIN_TYPE binType) {
		
		
		BigDecimal[] posts = ipbs.posts;
		BigDecimal[] functional = ipbs.functional;
		
		
		Bin[] bin = new Bin[posts.length - 1];
		for (int i = 0; i < bin.length; i++) {
			Bound bottom = null;
			Bound top = null;
			
			//
			//Bottom values
			if (i == 0 && (bottomUnbounded || ipbs.usesDetectionLimit)) {
				//If the bottom is unbounded due to model requirements or this
				//is a detection limit bottom, display with '<' and mark it
				//as unbounded.
				bottom = new Bound(posts[i], functional[i],
						"<", functionalFormatter.format(functional[i]), true);
			} else if (i == 1 && ipbs.usesDetectionLimit) {
				//The second bin bottom bound will have a bottom bound
				//that is the non-detect value (if using detect limits).
				//Use special formatted value for this case.
				bottom = new Bound(
						posts[i], functional[i], formattedDetectionLimit,
						functionalFormatter.format(functional[i]), false);
			} else {
				bottom = new Bound(posts[i], functional[i],
						formatter.format(posts[i]), functionalFormatter.format(functional[i]), false);
			}
			
			
			//
			//Top values
			if (i == 0 && ipbs.usesDetectionLimit) {
				//The first bin top bound will is the non-detect value (if using detect limits).
				//Use special formatted value for this case.
				top = new Bound(
						posts[i + 1], functional[i + 1], formattedDetectionLimit,
						functionalFormatter.format(functional[i + 1]), false);
			} else if (i == (bin.length - 1) && topUnbounded) {
				//Unbounded top due to model requirement
				top = new Bound(posts[i + 1], functional[i + 1],
						">", functionalFormatter.format(functional[i + 1]), true);
			} else {
				top = new Bound(posts[i + 1], functional[i + 1],
						formatter.format(posts[i + 1]), functionalFormatter.format(functional[i + 1]), false);
			}
			
			Integer valueCount = null;
			if (binCounts != null) valueCount = binCounts[i];
			
			Bin b = null;
			if (i == 0 && ipbs.usesDetectionLimit) {
				b = new Bin(bottom, top, true, valueCount);
			} else {
				b = new Bin(bottom, top, valueCount);
			}
			
			bin[i] = b;
		}
		
		BinSet bins = new BinSet(bin, binType, ipbs, formatter.toPattern());
		return bins;
	}
	
	public InProcessBinSet createInProcessBinSet() {
		InProcessBinSet ipbs = new InProcessBinSet();
		ipbs.actualMax = this.actualMax;
		ipbs.actualMin = this.actualMin;
		ipbs.characteristicUnitValue = this.characteristicUnitValue;
		ipbs.posts = this.getActualPostValues();
		ipbs.requestedBinCount = this.requestedBinCount;
		ipbs.usesDetectionLimit = this.usesDetectionLimit;
		return ipbs;
	}

	public boolean isUsesDetectionLimit() {
		return usesDetectionLimit;
	}

	public BigDecimal getActualMax() {
		return actualMax;
	}

	public BigDecimal getActualMin() {
		return actualMin;
	}

	public BigDecimal getCharacteristicUnitValue() {
		return characteristicUnitValue;
	}

	public Integer getRequestedBinCount() {
		return requestedBinCount;
	}

	public Integer getActualBinCount() {
		return actualBinCount;
	}

	public String getFormatPattern() {
		return formatPattern;
	}
	
}
