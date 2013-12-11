package gov.usgs.cida.sparrow.calculation.framework;

/**
 *
 * @author cschroed
 */
public enum Database {
	
	EROS_PROD("EROS Prod (dbtrans)", "EP", "cida-eros-dbtrans.er.usgs.gov:1521:dbtrans"),
	EROS_QA("EROS QA (qatrans)", "EQA", "cida-eros-dbqa.er.usgs.gov:1521:qatrans"),
	EROS_DEV("EROS Dev (devtrans)", "EDEV", "cida-eros-dbdev.er.usgs.gov:1521:devtrans"),
	WI_TEST("Wisc Test", "WT", "130.11.165.137:1521:witest"),
	WI_PROD("Wisc Prod", "WP", "130.11.165.152:1521:widw"),
	WI_DEV("Wisc Dev", "WD", "130.11.165.154:1521:widev");

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
