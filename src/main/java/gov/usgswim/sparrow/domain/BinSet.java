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
	 * Returns true if the passed value is greater than the formatted bottom
	 * bound of the first bin and less than the formatted top bound of the last bin.
	 * 
	 * The bottom bin is inclusive and the top bin is exclusive.
	 * 
	 * This method does not ensure that a value does not fall in possible gaps
	 * between bins.
	 * 
	 * @param value
	 * @return
	 */
	public boolean formattedExtremesContain(double value) {
		Bin first = bins[0];
		Bin last = bins[bins.length - 1];
		
		return first.formattedBottomLessThan(value, true) &&
				last.formattedTopGreaterThan(value, false);
	}
	
	/**
	 * Returns true if the formatted bottom-most bound is unbounded or is less than
	 * the actual bottom-most post value.
	 * 
	 * @param inclusive If true, the comparison is less than or equal to.
	 * @return
	 */
	public boolean formattedBottomContainsActual(boolean inclusive) {
		Bin bin = bins[0];
		return bin.formattedBottomLessThan(bin.getBottom().getActual(), inclusive);
	}
	
	/**
	 * Returns true if the formatted top-most bound is unbounded or is greater
	 * than the actual top-most post value.
	 * 
	 * @param inclusive If true, the comparison is greater than or equal to.
	 * @return
	 */
	public boolean formattedTopContainsActual(boolean inclusive) {
		Bin bin = bins[bins.length - 1];
		return bin.formattedTopGreaterThan(bin.getTop().getActual(), inclusive);
	}
	
	
	/**
	 * Creates a new BinSet inferring some aspects based on the context.
	 * 
	 * @param posts	The actual values that make up the 'fence posts' of the bins.
	 * @param suggestedPosts  A set of fence post values that slightly cleaned
	 * 	up.  For instance, if an actual top is 50.00000000000001, the suggested
	 * 	post for that might be 50.001.
	 * @param formatter	The formatter to use to convert the actual values to
	 * 	formatted string values for use in a legend.
	 * @param functionalFormatter The formatter used for the functional values.
	 * @param unboundedBottomString A string to use as the formatted value for
	 * 	the bottom-most bound if the bottom is unbounded. (not used otherwise).
	 * @param unboundedTopString	A string to use as the formatted value for
	 * 	the top-most bound if the top is unbounded. (not used otherwise).
	 * @param bottomUnbounded	True if the bottom-most bound is unbounded.
	 * @param topUnbounded	True if the top-most bound is unbounded.
	 * @param binType	Equal count or Equal Range.
	 * @param contextId	The SPARROW context ID, used to determine other relevant
	 * 	information about how the bins are constructed, such as the data series.
	 * @return
	 */
	public static BinSet createBins(InProcessBinSet ipbs, 
			DecimalFormat formatter, DecimalFormat functionalFormatter,
			boolean bottomUnbounded, boolean topUnbounded, String formattedDetectionLimit,
			BIN_TYPE binType) {
		
		
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
						functionalFormatter.format(functional[i]), true);
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
			
			Bin b = null;
			if (i == 0 && ipbs.usesDetectionLimit) {
				b = new Bin(bottom, top, true);
			} else {
				b = new Bin(bottom, top);
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
