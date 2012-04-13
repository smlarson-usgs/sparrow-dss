package gov.usgswim.sparrow.request;

import gov.usgs.cida.binning.domain.BinType;
import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.ComparisonType;
import gov.usgswim.sparrow.domain.DataSeriesType;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for break-points in a set of data.
 *
 * @author eeverman
 */
@Immutable
public class BinningRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
     * The types of bins that may be requested.
     */
//	public enum BIN_TYPE {
//		EQUAL_COUNT,
//		EQUAL_RANGE
//	}

	/** The predict context ID the binning is based on */
	private final Integer contextID;
	
	/** The type of comparison the data is based on. (Derived from context, but useful) */
	private final ComparisonType comparison;
	
	/** The dataseries the binning is based on. (Derived from context, but useful) */
	private final DataSeriesType dataSeries;

	/** Number of bins. */
	private final int binCount;

	/** Type of bin. */
	private final BinType binType;
	
	/** What we are measuring */
	private final String constituent;
	
	/** For this data series / constituent combo, what is the lowest measurable value? */
	private final BigDecimal detectionLimit;
	
	/** Max number of decimal places allowed */
	private final Integer maxDecimalPlaces;

	/**
	 * Constructor that takes only the actual disctinct data of the request
	 * (other values are derived)
	 * 
	 * @param contextID
	 * @param binCount
	 * @param binType
	 */
	public BinningRequest(Integer contextID, int binCount, BinType binType) {
	    this(contextID, binCount, binType, null, null, null, null, null);
	}
	
	public BinningRequest(Integer contextID, int binCount, BinType binType,
			DataSeriesType dataSeries, ComparisonType comparison,
			String constituent, BigDecimal detectionLimit, Integer maxDecimalPlaces) {
		
	    if (binCount < 1) {
	        throw new IllegalArgumentException("Bin count must be greater than 0.");
	    }

		this.contextID = contextID;
		this.binCount = binCount;
		this.binType = binType;
		this.dataSeries = dataSeries;
		this.comparison = comparison;
		this.constituent = constituent;
		this.detectionLimit = detectionLimit;
		this.maxDecimalPlaces = maxDecimalPlaces;
	}

	public Integer getContextID() {
	    return contextID;
	}

	public Integer getBinCount() {
	    return binCount;
	}

	public BinType getBinType() {
        return binType;
    }
	
	public ComparisonType getComparison() {
		return comparison;
	}

	public DataSeriesType getDataSeries() {
		return dataSeries;
	}
	
	/**
	 * What we are measuring in the data we are binning.
	 * 
	 * @return
	 */
	public String getConstituent() {
		return constituent;
	}

	/**
	 * The detection limit for the current dataseries.
	 * 
	 * Note that in the case of a comparison, the detection limit is not relevant.
	 * @return
	 */
	public BigDecimal getDetectionLimit() {
		return detectionLimit;
	}

	/**
	 * The max number of decimal places that should be allowed.
	 * 
	 * If null, there is no special restriction.  Note that for comparison
	 * a different standard should be used.
	 * 
	 * @return
	 */
	public Integer getMaxDecimalPlaces() {
		return maxDecimalPlaces;
	}

	
	public BinningRequest clone(DataSeriesType dataSeries,
			ComparisonType comparison, String constituent,
			BigDecimal detectionLimit, Integer maxDecimalPlaces) {
		
		return new BinningRequest(this.contextID, this.binCount, this.binType,
				dataSeries, comparison, constituent,
				detectionLimit, maxDecimalPlaces);
	}


	/**
	 * Consider two instances the same if they have the same calculated hashcodes.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BinningRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {

		HashCodeBuilder hash = new HashCodeBuilder(197, 1343);
		hash.append(contextID);
		hash.append(binCount);
		hash.append(binType);
		hash.append(dataSeries);
		hash.append(comparison);
		hash.append(constituent);
		hash.append(detectionLimit);
		hash.append(maxDecimalPlaces);

		return hash.toHashCode();
	}



}
