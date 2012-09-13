package gov.usgswim.sparrow.service.idbypoint;

public class TruncateDecimal implements DisplayRule {

	private final int precision;

	/**
	 *
	 * @param precision = max number of digits allowed to the right of decimal point
	 */
	public TruncateDecimal(int precision) {
		this.precision = precision;
	}

	@Override
	public String apply(String value) {
		if (value != null && value.indexOf('.')> -1) {
			int decimalPointPos = value.indexOf('.');
			if (value.length() > decimalPointPos + precision + 1) {
				// too many digits to the right of the decimal, truncate
				value = value.substring(0, decimalPointPos + precision + 1);
			}
		}
		return value;
	}

}
