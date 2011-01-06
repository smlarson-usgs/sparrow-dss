package gov.usgswim.sparrow.parser;

public enum PredefinedSessionType {
	/** FEATURED are listed on the project home page */
	FEATURED,
	
	/** LISTED is publically listed, but not on the main page. */
	LISTED,
	
	/** UNLISTED are not listed anywhere in the SPARROW DSS application,
	 *  but links for these predefined themes can be placed on other websites
	 *  (like the modelers home page) so that they can create special purpose
	 *  pre-contexts w/o having them show up in the application.
	 */
	UNLISTED;
}
