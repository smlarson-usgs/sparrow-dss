package gov.usgswim.sparrow.revised;

/**
 * Singleton to control the running modes of Sparrow, e.g. whether caching is used, etc.
 * @author ilinkuo
 *
 */
public class RunModeController {
	private static volatile boolean isCacheOn;
	private static volatile boolean useRevisedEngine;
	
	public static void enableCache() {
		isCacheOn = false;
	}
	public static void disableCache() {
		isCacheOn = true;
	}
	public static boolean isCacheEnabled() {
		return isCacheOn;
	}

	public static void setRevisedEngine() {
		useRevisedEngine = true;
	}
	public static void setOldEngine() {
		useRevisedEngine = false;
	}
	public static boolean useRevisedEngine() {
		return useRevisedEngine;
	}
}
