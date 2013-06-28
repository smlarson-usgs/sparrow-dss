package gov.usgswim.sparrow;

import gov.usgswim.ImmutableBuilder;

import java.io.Serializable;

/**
 * Contains data and some calculations needed to calculate a specific
 * UncertaintySeries for a model.  There is no meta data (i.e., what series or
 * model - see UncertaintyDataRequest.
 * 
 * This class is intended to be used in conjunction with a PredictData instance,
 * thus no row IDs are stored in instances of this class as rows here must
 * correspond exactly to reach rows in its associated PredictedData.
 * 
 * Instances may be be mutable or immutable, but will be forced
 * immutable (via the ImmutableBuilder interface) so that they can be cached.
 * 
 */
public interface UncertaintyData extends ImmutableBuilder<UncertaintyData>, Serializable {

	/**
	 * Returns the bias value (don't know why its termed mean) for the specified
	 * reach.
	 * 
	 * @param rowIndex The zero based row index matching the associated PredictData reach rows.
	 * @return A bias value.
	 */
	public double getMean(int rowIndex);
	
	/**
	 * Returns the standard error value for the specified reach.
	 * 
	 * @param rowIndex The zero based row index matching the associated PredictData reach rows.
	 * @return A standard error value.
	 */
	public double getStandardError(int rowIndex);
	
	/**
	 * Calculates the coefficient of variation for the specified reach.
	 * 
	 * This calculation specified in an email on Sept 22, 2009 by:
	 * Gregory E. Schwarz
	 * U.S. Geological Survey Phone: (703) 648-5718
	 * 12201 Sunrise Valley Dr. Fax: (703) 648-6693
	 * MS 413
	 * Reston, VA 20192
	 * 
	 * @param rowIndex
	 * @return
	 */
	public double calcCoeffOfVariation(int rowIndex);
	
	/**
	 * Returns the number of rows in the data.
	 * 
	 * This doesn't really have a functional use, but it is handy for testing.
	 * 
	 * @return The number of rows in the data.
	 */
	public int getRowCount();

}

