package gov.usgswim.sparrow.service.idbypoint;

public class ZeroOneAsTrueFalse implements DisplayRule {

	@Override
	public String apply(String value) {
		if (value != null) {
			if (value.equals("0.0") || value.equals("0") || value.equals(".0")) {
				value = "false";
			} else if (value.equals("1.0") || value.equals("1") || value.equals("1.")) {
				value = "true";
			}
		}
		return value;
	}

}
