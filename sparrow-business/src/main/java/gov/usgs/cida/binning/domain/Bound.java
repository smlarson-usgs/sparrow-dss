package gov.usgs.cida.binning.domain;

import java.io.Serializable;
import java.math.BigDecimal;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * A upper or lower bound of a bin for theming a map or creating a legend.
 * 
 * @author eeverman
 */
@XStreamAlias("Bound")
public class Bound implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** The actual value needed for this bound (i.e. 50) */
	@XStreamAsAttribute
	private final BigDecimal actual;
	
	/** Formatted String representation of bound. May be sci notation of std. */
	@XStreamAsAttribute
	private final String formatted;
	
	/**
	 * In cases of a rounding overrun such as a value that *should* be 50, but
	 * precision errors cause it to be 50.0000000001, the functional
	 * gives a value that should be used for binning, even if the user
	 * is presented w/ a 'nice' rounded value in the legend.
	 * 
	 * This value should also be used as the suggested value when the user
	 * switches from auto binning to custom binning.
	 */
	@XStreamAsAttribute
	private final BigDecimal functional;
	
	/** Formatted string version of functional.  May be sci notation of std. */
	@XStreamAsAttribute
	private final String formattedFunctional;
	
	/** True if the bin is unbounded.  For instance, it may be '>' or '<'. */
	@XStreamAsAttribute
	private final boolean unbounded;
	
	
	public Bound(BigDecimal actual, BigDecimal functional,
			String formatted, String formattedFunctional, boolean isUnbounded) {

		this.actual = actual;
		this.functional = functional;
		this.formatted = formatted;
		this.unbounded = isUnbounded;
		this.formattedFunctional = formattedFunctional;
	}


	public String getFormatted() {
		return formatted;
	}


	public boolean isUnbounded() {
		return unbounded;
	}


	public BigDecimal getActual() {
		return actual;
	}


	public BigDecimal getFunctional() {
		return functional;
	}


	public String getFormattedFunctional() {
		return formattedFunctional;
	}
	

}
