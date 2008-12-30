package gov.usgswim.sparrow.loader;

public class ModelDataAssumptions {

	public static final int CONTACT_ID = 42; // default
	public static final int ENH_NETWORK_ID = 22; // default
	public static final int PRECISION = 2; // default
	public static final String SEDIMENT_MODEL_SOURCE_HACK = "0"; // HACK: 0 here because sediment source data is irregular!
	public static Long MODEL_ID = 34L;
	
	public static final String DEFAULT_CONSTITUENT = "Phosphorus";
	public static final String DEFAULT_UNITS = "kg/yr";
	public static final String IS_ARCHIVED_DEFAULT = "F";
	public static final String IS_PUBLIC_DEFAULT = "T";
	public static final String IS_APPROVED_DEFAULT = "T";
	public static final String IS_POINT_SOURCE_DEFAULT = "F";
	
	public static String useDefaultPrecisionIfUnavailable(
			DataFileDescriptor md, String[] inputValues) {
		return useDefaultIfUnavailable(md, inputValues, ModelDataLoader.SMD_PRECISION, Integer.toString(PRECISION));
	}
	
	public static String useDefaultIfUnavailable(
			DataFileDescriptor md, String[] inputValues, String key, String defaultValue) {
		int index = md.indexOf(key);
		String result = null;
		if (index >= 0) result = inputValues[index];
		return (result == null || result.length() == 0)? defaultValue: result;
	}

	public static String useNameForDisplayNameIfUnavailable(
			DataFileDescriptor md, String[] inputValues) {
		String displayName = ModelDataLoader.quoteForSQL(inputValues[md.indexOf("display_name")]);
		if (displayName == null) {
			displayName = ModelDataLoader.quoteForSQL(inputValues[md.indexOf(ModelDataLoader.SMD_NAME)]);
		}
		return displayName;
	}


	public static String translatePointSource(String value) {
		if (value == null || value.length() ==0) return null;
		char val = value.toUpperCase().charAt(0);
		switch (val) {
			case '0':
			case 'F':
			case 'N':
				return "F";
			case '1':
			case 'T':
			case 'Y':
				return "T";
			default:
				return null;
		}
	}

	/**
	 * Assumes that the headings for the coefficients are the names of the
	 * sources capitalized and prefixed with "C_"
	 * 
	 * @param sources
	 * @return
	 */
	public static String[] addPrefixAndCapitalize(String[] sources) {
		String[] result = new String[sources.length];
		for (int i=0; i<sources.length; i++) {
			result[i] = "C_" + sources[i].toUpperCase();
		}
		return result;
	}

	public static String sedimentIrregularHack(DataFileDescriptor md,
			String line) {
		if (line.charAt(line.length()-1) == '\t') {
			return line + "0" + md.delimiter + "_";
		}
		return line + md.delimiter + "_";
	}




}
