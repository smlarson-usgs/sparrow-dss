package gov.usgs.cida.sparrow.service.util;

/**
 * Enum of possible status values for a service.
 * 
 * @author eeverman
 *
 */
public enum ReturnStatus {
	/** Request succeeded */
	OK,
	/** Request succeeded, but the request results in no data to return.
	 *  This may be because a search resulted in no records found.
	 */
	OK_EMPTY,
	/** Request succeeded, but only some of the results are returned */
	OK_PARTIAL,
	/** A specific ID was requested, but this ID cannot be found */
	ID_NOT_FOUND,
	/** The request is incorrectly formatted or contains contradictory options */
	INVALID_REQUEST,
	/** An unexpected error occurred while processing the request */
	ERROR
}
