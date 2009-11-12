package gov.usgswim.sparrow.revised;

/**
 * Singleton to control the running modes of Sparrow, e.g. whether caching is used, etc.
 * @author ilinkuo
 *
 */
public class RunModeController {
	public static volatile boolean isCacheOn;
	public static volatile boolean useRevisedEngine;
	public static volatile boolean checkDeprecation;

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

	public static void setDeprecationCheck() {
		checkDeprecation = true;
	}

	public static void checkDeprecation() {
		if (checkDeprecation) {
			try {
				throw new Exception("DEPRECATION CHECK: Running possibly deprecated code. Print Stack trace and terminate");
			} catch (Exception e) {
				// Just a hack to get hold of the stack trace. There's probably a more elegant way to do this.
				e.printStackTrace(System.err);
			}
		}
	}
}
