package gov.usgswim.sparrow;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a holder for non-String properties of the Sparrow model. For purely
 * String properties, the PropertyLoaderHelper class should be used to allow
 * dynamic configuration changes without an application restart
 * 
 * @author ilinkuo
 * 
 */
public abstract class SparrowModelProperties {
	
	/**
	 * Attributes which are specific to the Sparrow model, important to the
	 * Sparrow modeling structure. This is to be contrasted with general or
	 * basic attributes which are data and do not affect the dynamics of the
	 * modeling. This list of attributes is obtained from id_response.xml. A
	 * list is used rather than an array because of the availability of a
	 * List.contains() method and the lack of a corresponding method for arrays
	 * 
	 */
	public static final List<String> SPARROW_ATTRIBUTES = Collections.unmodifiableList(
			Arrays.asList(new String[]{"Hydrological Seq","Fraction","From Node","To Node"}));
	
	public static final Map<String, String> HARDCODED_ATTRIBUTE_UNITS = initializeHAU();
	
	// ======================
	// INITIALIZATION METHODS
	// ======================
	private static Map<String, String> initializeHAU() {
		// These values for attribute units are obtained from the comments in the table MODEL_REACH_ATTRIB
		Map<String, String> units = new HashMap<String, String>();
		units.put("Mean Q", "cu ft/s");
		units.put("Mean V", "ft/s");
		units.put("Catch Area", "sq km");
		units.put("Cumulative Catch Area", "sq km");
		units.put("Reach Length", "m");
		return Collections.unmodifiableMap(units);
	}
}
