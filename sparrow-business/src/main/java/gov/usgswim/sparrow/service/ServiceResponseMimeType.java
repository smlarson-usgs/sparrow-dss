package gov.usgswim.sparrow.service;

import org.apache.commons.lang.StringUtils;

/**
 * Standardized mime types for the ServiceResponseWrapper.
 * 
 * @author eeverman
 *
 */
public enum ServiceResponseMimeType {
	XML("application/xml"),
	JSON("application/json"),
	UNKNOWN("unknown");
	
	private String mimeStr;
	
	ServiceResponseMimeType(String mimeString) {
		this.mimeStr = mimeString;
	}


	public String toString() {
		return mimeStr;
	}
	
	/**
	 * Finds a matching ENUM based on name or the full mime string.
	 * 
	 * Case insensitive.
	 * 
	 * @param mimeString
	 * @return
	 */
	public static ServiceResponseMimeType parse(String mimeString) {
		
		mimeString = StringUtils.trimToNull(mimeString);
		if (mimeString == null) {
			return UNKNOWN;
		} else {
			ServiceResponseMimeType[] types = ServiceResponseMimeType.values();
			
			for (ServiceResponseMimeType t : types) {
				if (
						t.name().equalsIgnoreCase(mimeString) ||
						t.toString().equalsIgnoreCase(mimeString)) { 
					
					return t;
				}
				
			}
			
			return UNKNOWN;
		}
	}
}
