package gov.usgswim.sparrow.request;

import gov.usgswim.Immutable;
import gov.usgswim.sparrow.domain.HucLevel;

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A request for HUC outline geometry.
 * 
 * @author eeverman
 *
 */
@Immutable
public class HUCRequest implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final HucLevel hucType;	//derived
	private final String huc;
	
	public HUCRequest(String huc) {
		this.huc = huc;
		int len = huc.length();
		switch (len) {
		case 2:
			hucType = HucLevel.HUC2;
			break;
		case 4:
			hucType = HucLevel.HUC4;
			break;
		case 6:
			hucType = HucLevel.HUC6;
			break;
		case 8:
			hucType = HucLevel.HUC8;
			break;
		default:
			hucType = null;
			throw new IllegalArgumentException("The huc must be a 2, 4, 6, or 8 character string.");
		}
	}

	public HucLevel getHucType() {
		return hucType;
	}

	public String getHuc() {
		return huc;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof HUCRequest) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}
	
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(2457, 143).
		append(huc).toHashCode();
		
		return hash;
	}
}
