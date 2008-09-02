package gov.usgswim.sparrow.loader;

public class ModelDataAssumptions {

	// WTF? in sparrow_model
	public static final int CONTACT_ID = 42; // default
	public static final int ENH_NETWORK_ID = 22; // default
	public static final int PRECISION = 2; // default
	public static final String SEDIMENT_MODEL_SOURCE_HACK = "0"; // HACK: 0 here because sediment source data is irregular!
	public static Long MODEL_ID = 30L;
	
	
	public static String useDefaultPrecisionIfUnavailable(
			DataFileDescriptor md, String[] inputValues) {
		String precision = inputValues[md.indexOf("precision")];
		if (precision == null || precision.length() == 0) {
			precision = Integer.toString(PRECISION);
		}
		return precision;
	}

	public static String useNameForDisplayNameIfUnavailable(
			DataFileDescriptor md, String[] inputValues) {
		String displayName = ModelDataLoader.quoteForSQL(inputValues[md.indexOf("display_name")]);
		if (displayName == null) {
			displayName = ModelDataLoader.quoteForSQL(inputValues[md.indexOf("name")]);
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
		} else {
			return line + md.delimiter + "_";
		}
	}


}
