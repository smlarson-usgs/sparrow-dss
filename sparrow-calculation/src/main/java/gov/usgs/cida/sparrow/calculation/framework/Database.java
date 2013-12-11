package gov.usgs.cida.sparrow.calculation.framework;

/**
 *
 * @author cschroed
 */
public enum Database {
	WIWSC_PROD("WIWSC Production", "WP", "130.11.165.152:1521:widw"),
	EROS_PROD("EROS Production", "EP", "152.61.236.40:1521:dbdw"),
	EROS_DEV("EROS Development (trans)", "EDEV", "cida-eros-dbdev.er.usgs.gov:1521:devtrans"),
	EROS_QA("EROS QA (trans)", "EQA", "cida-eros-dbqa.er.usgs.gov:1521:qatrans"),
	TEST("Test", "T", "130.11.165.137:1521:witest"),
	DEVELOPMENT("Development", "D", "130.11.165.154:1521:widev");

	private String fullName;
	private String shortName;
	private String urlFragment;

	Database(String fullName, String shortName, String urlFragment) {
		this.shortName = shortName;
		this.fullName = fullName;
		this.urlFragment = urlFragment;
	}

	public Database getForShortName(String name) {
		for (Database d : Database.values()) {
			if (d.shortName.equalsIgnoreCase(name)) return d;
		}
		return null;
	}

	public String getFullName() {
		return fullName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getUrlFragment() {
		return urlFragment;
	}
}
