package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Arrays;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import org.apache.log4j.Logger;

/**
 * This factory class creates a binning array based on a request from EHCache.
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
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 * 
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 * 
 * @author eeverman
 */
public class BinningFactory implements CacheEntryFactory {
	protected static Logger log =
		Logger.getLogger(BinningFactory.class); //logging for this class
	
	public Object createEntry(Object binningRequest) throws Exception {
		BinningRequest request = (BinningRequest)binningRequest;
		PredictionContext context = SharedApplication.getInstance().getPredictionContext(request.getContextID());
		
		if (context == null) {
			throw new Exception("No context found for context-id '" + request.getContextID() + "'");
		}

		PredictionContext.DataColumn dc = context.getDataColumn();
		
		double[] bins = null;
		
		// Determine type of binning to perform, calling the appropriate method
		if (request.getBinType() == BinningRequest.BIN_TYPE.EQUAL_COUNT) {
		    bins = getEqualCountBins(dc.getTable(), dc.getColumn(), request.getBinCount());
		} else if (request.getBinType() == BinningRequest.BIN_TYPE.EQUAL_RANGE) {
		    bins = getEqualRangeBins(dc.getTable(), dc.getColumn(), request.getBinCount());
		}
		
		return bins;
	}
	
	/**
	 * Placeholder for original getEqualCountBins() method. TODO remove
	 * @param data
	 * @param columnIndex
	 * @param binCount
	 * @return
	 */
	protected double[] getEqualCountBins(DataTable data, int columnIndex, int binCount) {
		return getEqualCountBins(data, columnIndex, binCount, true); // TODO set to true for now. Should take parameter from BinningRequest
	}
	
	public static final double ALLOWABLE_BIN_SIZE_VARIANCE_RATIO = 1/10;
	protected double[] getEqualCountBins(DataTable data, int columnIndex, int binCount, Boolean useRounding) {
		int totalRows = data.getRowCount();	//Total rows of data
		float[] values = extractSortedValues(data, columnIndex, totalRows); //sorted Array holding all values
		
		if (useRounding == null || !useRounding) {
			return getEqualCountBins(data, binCount, values);
		}
		// adjust the bin fence posts
		double[] bins = new double[binCount + 1];
		double binSize = (double)(totalRows) / (double)(binCount);
		double binVariance = ( binSize * ALLOWABLE_BIN_SIZE_VARIANCE_RATIO )/2;
		
		// lowest value edge bin needs special handling
		double value = values[0];
		int hiIndex = (int) binVariance;
		double hi = values[hiIndex];
		double lo = value - (hi - value);
		bins[0] = round(value, lo, value); // must not allow the lowest bin to be rounded up
		
		for (int i=1; i<binCount; i++) {
			double valueIndex = i * binSize;
			value = values[(int) valueIndex];
			hiIndex = (int) (valueIndex + binVariance);
			int loIndex = (int)(valueIndex - binVariance);
			// allow middle bins to be tweaked slightly higher or lower
			bins[i] = round(value, values[loIndex], values[hiIndex]);
		}
		
		// highest value edge bin needs special handling
		value = values[binCount];
		int loIndex = binCount - (int) binVariance;
		lo = values[loIndex];
		hi = value + (value - lo);
		bins[binCount] = round(value, value, hi); // must not allow the highest bin to be rounded down
		
		return bins;
	}
	
	/**
	 * Returns an equal count set of bins so that the bins define break-point
	 * boundaries with approximately an equal number of values in each bin.
	 * 
     * @param data Table of data containing the column to divide into bins.
     * @param columnIndex Index of the column to divide into bins.
     * @param binCount Number of bins to divide the column into.
     * @return Set of bins such that the bins define break-point boundaries
     *         with an approximately equal number of values contained within.
	 */
	protected double[] getEqualCountBins(DataTable data, int binCount, float[] sortedValues) {

		int totalRows = data.getRowCount();	//Total rows of data
		
		//Number of rows 'contained' in each bin.  This likely will not come out even,
		//so use a double to preserve the fractional rows.
		double binSize = (double)(totalRows) / (double)(binCount);	
		
		
		//The bins, where each value is a fence post w/ values between, thus, there is one more 'post' than bins.
		//The first value is the lowest value in values[], the last value is the largest value.
		double[] bins = new double[binCount + 1];	
		
		//Assign first and last values for the bins (min and max)
		bins[0] = sortedValues[0];
		bins[binCount] = sortedValues[totalRows - 1];
		
		//Assign the middle breaks so that equal numbers of values fall into each bin
		for (int i=1; i<(binCount); i++) {
			
			//Get the row containing the nearest integer split
			int split = (int) ((double)i * binSize);
			
			//The bin boundary is the value contained at that row.
			bins[i] = (double) sortedValues[split];
		}
		
		return bins;
	}

	private float[] extractSortedValues(DataTable data, int columnIndex,
			int totalRows) {
		float[] values = new float[totalRows];	
		//Export all values in the specified column to values[] so they can be sorted
		for (int r=0; r<totalRows; r++) {
			values[r] = data.getFloat(r, columnIndex);
		}
		
		Arrays.sort(values);
		return values;
	}
	
    /**
     * Returns an equal range set of bins such that the bins define break-point
     * boundaries whose values are approximately equally spaced apart.
     *
     * @param data Table of data containing the column to divide into bins.
     * @param columnIndex Index of the column to divide into bins.
     * @param binCount Number of bins to divide the column into.
     * @return Set of bins such that the bins define break-point boundaries
     *         whose values are approximately equally spaced apart.
     */
    protected double[] getEqualRangeBins(DataTable data, int columnIndex, int binCount) {
        int totalRows = data.getRowCount(); // Total rows of data

        // Grab the min and max values from the datatable
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        for (int r = 0; r < totalRows; r++) {
            float value = data.getFloat(r, columnIndex);
            minValue = Math.min(value, minValue);
            maxValue = Math.max(value, maxValue);
        }

        // Size of the range of values that will be defined by each bin
        double binRangeSize = (maxValue - minValue) / (double)(binCount);

        // The bins, where each value is a fence post with values between, thus
        // there is one more post than bins.  The first value is the minimum,
        // the last value is the maximum.
        double[] bins = new double[binCount + 1];
        bins[0] = minValue;
        bins[binCount] = maxValue;

        // Assign the breakpoints so that an equal range of values fall into each bin
        for (int i = 1; i < binCount; i++) {
            bins[i] = minValue + ((double)i * binRangeSize);
        }

        return bins;
    }
    
    public static final double[] digitAccuracyMultipliers= {1,2,10, 20, 100, 200}; // multipliers for 1, 1.5, ... 3.5 digit accuracy
    
    /**
     * Rounds to the fewest digits necessary (<= 3.5 digits) within the given lo-hi range
     * 
     * @param value
     * @param lo
     * @param hi
     * @return the original value if lo-hi range is too narrow
     */
    public static double round(double value, double lo, double hi) {
    	// round to zero as first option
    	if (value ==0 || (lo <=0 && hi>=0)) return 0;
    	// round to 1 digit
    	double baseNormalizer = Math.round(Math.pow(10, Math.floor(Math.log10(value))));
    	
    	for (double digitMultiplier: digitAccuracyMultipliers) {
    		Double result = findClosestInRange(value, lo, hi, baseNormalizer * digitMultiplier);
    		if (result != null) {
    			return result;
    		}
    	}
    	return value; // range is too narrow so just return original value
    }
    
    /**
	 * Finds the fraction with the denominator which is within the lo-hi range
	 * and closest to the value
	 * 
	 * @param value
	 * @param lo
	 * @param hi
	 * @param denominator
	 * @return null if no fraction within range
	 */
    public static Double findClosestInRange(double value, double lo, double hi, double denominator) {
    	assert(lo < hi);
    	value = value * denominator;
    	// expect Math.abs(value) >= 0, but is ok if it isn't
    	lo = lo * denominator;
    	hi = hi * denominator;
    	
    	double up = Math.ceil(value);
    	double down = Math.floor(value);
    	boolean isUpInRange = (lo <= up) && (hi >= up);
    	boolean isDownInRange = (lo <= down) && (hi >= down);
    	
    	if (isUpInRange && isDownInRange) {
    		// return the closest of two valid alternatives
    		return (Math.abs(up - value) < Math.abs(down - value))? up/denominator: down/denominator;
    	}
    	if (isUpInRange) {
    		return up/denominator;
    	} else if (isDownInRange) {
    		return down/denominator;
    	}
    	return null;
    }
}
