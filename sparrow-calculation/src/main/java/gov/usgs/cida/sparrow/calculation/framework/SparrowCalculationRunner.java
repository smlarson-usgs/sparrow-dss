package gov.usgs.cida.sparrow.calculation.framework;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.action.LoadModelMetadata;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 *
 * @author eeverman
 */
public class SparrowCalculationRunner {

//	static {
//		// Set up a simple configuration that logs on the console.
//
//		URL log4jUrl = SparrowModelValidationRunner.class.getResource("/log4j_test.xml");
//		LogManager.resetConfiguration();
//		DOMConfigurator.configure(log4jUrl);
//	}

	/**
	 * The required comparison accuracy (expected - actual)/(max(expected, actual))
	 * This value is slightly relaxed for values less than 1.
	 */
	final double REQUIRED_COMPARISON_FRACTION = .001d;	//comp fraction

	protected static Logger log = null;
	private boolean resultHeaderWritten = false;


	public final static String ID_COL_KEY = "id_col";	//Table property of the key column

	//final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = 5;
	//final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = 6;
	//The 'print all non-matching values' option:
	final static int NUMBER_OF_BAD_INCREMENTALS_TO_PRINT = Integer.MAX_VALUE;
	final static int NUMBER_OF_BAD_TOTALS_TO_PRINT = Integer.MAX_VALUE;

	protected boolean attemptToLoadPublicModelsFromDb = false;

	final static String QUIT = "quit";

	String singleModelPath;
	String activeModelDirectory;
	String cacheDirectory;	//directory to cache db model data to, so re-run models are faster
	List<Long> modelIds;
	String dbPwd;
	boolean useTestDb = false;	//modifies the connection string

	int firstModelId = -1;
	int lastModelId = -1;


	/**
	 * Error: Print single line messages for each row error.  No - additional (multi-line) details.
	 * Warn:	In addition to errors, print single line warnings for rows that are suspicious.
	 * Debug:	If available, print multi-line detail for each error.
	 * Trace:	Used to debug the tests themselves, this option prints additional info about successful values as well.
	 */
	protected Level logLevel;

	private List<Calculator> validators = new ArrayList<Calculator>();

	//Application lifecycle listener handles startup / shutdown
	static LifecycleListener lifecycle = new LifecycleListener();

 /**
	* The SparrowModelValidationRunner subclass name must be passed as the first
	* arg.  That subclass must override the loadMOdelValidators method to add
	* its suite of validators.
	*
	* @param args
	* @throws Exception
	*/
	public static void main(String[] args) throws Exception {

		boolean continueRun = true;

		if (args.length == 0) {
			System.out.println("Sorry, you cannot run this class directly.  Use one of the runner subclass implementations.");
			return;
		}


		//No logger up to this point
		String runnerToRun = args[0];
		SparrowCalculationRunner runner = (SparrowCalculationRunner) SparrowCalculationRunner.class.forName(runnerToRun).newInstance();
		runner.loadModelValidators();

		if (runner.getValidators().isEmpty()) {
			System.err.println("No validators were found.  Subclass SparrowModelValidationRunner to override the loadModelValidators method to add some.");
			continueRun = false;
		} else {
			System.out.println("Found " + runner.getValidators().size() + " validation tests to run against the models.");
		}


		if (continueRun) {
			continueRun = runner.oneTimeUserInput();
			if (continueRun == false) {
				System.out.println("Run canceled.");
			}
		}
		if (continueRun) continueRun = runner.initSystemConfig();		//Logging system is now configured (which log4j file to load)
		if (continueRun) continueRun = runner.initLoggingConfig();		//Now do test configuraiton of logging

		//Build list of models to run
		if (continueRun && runner.attemptToLoadPublicModelsFromDb) {
			List<SparrowModel> models = SharedApplication.getInstance().getModelMetadata(new ModelRequestCacheKey(null, true, false, false));
			runner.modelIds = new ArrayList<Long>();
			for (SparrowModel m : models) {
				runner.modelIds.add(m.getId());
				log.info("Found public model: " + m.getId());
			}
		}

		if (continueRun && runner.initModelValidators()) {
			long startTime = System.currentTimeMillis();
			runner.run();
			long endTime = System.currentTimeMillis();

			System.out.println("Total test run time: " + ((endTime - startTime) / 1000) + " seconds.");
		} else {
			System.err.println("Unable to run any models or tests.");
		}
	}


	public CalculationResultsReport run() {

		CalculationResultsReport result = new CalculationResultsReport();

		log.info("*****************************************");
		if (singleModelPath != null) {
			log.info(" ************ Running one model from a text file ************");
			log.info("*************************************************************");

			Long id = getModelIdFromPath(singleModelPath);

			if (id != null) {
				result.add(runOneModel(id));
			} else {
				result.setConfigError();
			}
		} else if (modelIds != null && modelIds.size() > 0) {
			log.info(" ****** Running models from a list of ids (no text file) ********");
			log.info("*****************************************************************");

			result = runListOfModels(modelIds);

		} else {

			log.info("************ Running a directory of models ************");
			log.info("*****************************************");

			result =
					runOneModelDirectory(this.activeModelDirectory, this.firstModelId, this.lastModelId);

		}


		if (result.getModelCount() == 0) {
			log.error("- - - - - NO MODELS WERE FOUND.  PLEASE CHECK PATH AND CONFIG INFO. - - - - - ");
		} else if (result.isPerfect()) {
			log.info("+ + + + + EVERYTHING LOOKS PERFECT! + + + + +");
		} else if (result.isOk()) {
			log.info("+ + + + + EVERYTHING LOOKS OK, but there are some warnings. + + + + +");
		} else {
			log.error("- - - - - SOME MODELS AND TESTS WERE FAILED.  PLEASE CHECK THE FILE OUTPUT. - - - - - ");
		}

		String modelsRun = "";
		for (CalculationResultList modelResults : result) {
			modelsRun += modelResults.getModelId() + ", ";
		}
		modelsRun = modelsRun.substring(0, modelsRun.length() - 2);

		log.info("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
		log.info("Models Run: " + modelsRun);
		log.info("Total Models Run: " + result.getModelCount());
		log.info("Models with any type of Error: " + result.getModelsWithAnyAnyErrorCount());
		log.info("Models with any type of Warning: " + result.getModelsWithWarnCount() +
				" of which, " + result.getModelsWithErrorsAsWarnCount() +
				" would be considered errors, but they are temporarly disabled**");
		log.info("Total number of individual errors: " + result.getErrorCount());
		log.info("Total number of individual warnings: " + result.getWarnCount() +
				" of which, " + result.getErrorsAsWarnCount() +
				" would be considered errors, but they are temporarly disabled**");




		log.info("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
		log.info("**Some tests generate lots of errors, many of which may not be"
				+ " truely considered errors.  To prevent a fail message, they are counted"
				+ " as warnings, but tracked separately.");
		log.info("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");


		log.info("*****************************************");
		log.info("************ Run Complete ***************");
		log.info("*****************************************");

		return result;
	}

	/**
	 * Returns the number of failures
	 * @param file
	 * @return
	 */
	protected CalculationResultList runOneModel(Long id) {

		beforeEachModel();

		CalculationResultList results = new CalculationResultList(id);

		for (Calculator v : validators) {
			try {

				v.beforeEachTest(id);
				results.add(v.testModel(id));
				v.afterEachTest(id);

			} catch (Exception ex) {
				log.error("Failed while running the test " + v.getClass().getCanonicalName(), ex);
			}
		}

		afterEachModel();

		return results;
	}

	protected void beforeEachModel() {
		SharedApplication.getInstance().clearAllCaches();
	}

	protected void afterEachModel() {
		//Nothing to do
	}

	protected Long getModelIdFromPath(String path) {
		String idString = path.substring(0, path.lastIndexOf('.'));
		idString = idString.substring(idString.lastIndexOf(File.separatorChar) + 1);
		Long id = null;
		try {
			id = Long.parseLong(idString);
			return id;
		} catch (Exception e) {
			log.fatal("Couldn't figure out the model number from the model path: " + path);
			return null;
		}
	}

	protected CalculationResultsReport runOneModelDirectory(String modelDirectory, long firstModelIdNumber, long lastModelIdNumber) {
		CalculationResultsReport result = new CalculationResultsReport();

		for (long id = firstModelIdNumber; id <= lastModelIdNumber; id++) {
			result.add(runOneModel(id));
		}

		return result;
	}

	protected CalculationResultsReport runListOfModels(List<Long> ids) {
		CalculationResultsReport result = new CalculationResultsReport();

		for (long id : ids) {
			result.add(runOneModel(id));
		}

		return result;
	}


	public void addCalculation(Calculator validator) {
		validators.add(validator);
	}

	public boolean oneTimeUserInput() throws Exception {


		promptIntro();

		PromptResponse pr = null;

		boolean requiresDb = false;
		boolean requiresText = false;

		for (Calculator v : validators) {
			requiresDb = requiresDb || v.requiresDb();
			requiresText = requiresText || v.requiresTextFile();
		}

		System.out.println("");

		if (requiresText) {

			System.out.println("--> The selected tests require text files.");

			pr = promptPathOrDir();
			if (pr.quit) return false;

			if (singleModelPath != null) {
				File f = new File(singleModelPath);
				if (! f.exists()) {
					log.fatal("Oops, the model path '" + singleModelPath + "' does not seem to exist.");
					return false;
				}
			} else if (activeModelDirectory != null) {
				File f = new File(activeModelDirectory);
				if (! f.exists()) {
					log.fatal("Oops, the directory '" + activeModelDirectory + "' does not seem to exist.");
					return false;
				}
			} else {
				log.fatal("A model path or a directory must be specified.");
				return false;
			}
		} else {
			//If text files are not involved, we need a list of model ids
			pr = prompModelIds();
			if (pr.quit) return false;
		}

		if (requiresDb) {

			System.out.println("--> The selected tests require a DB connection.");

			pr = promptWhichDb();
			if (pr.quit) return false;

			pr = promptPwd();
			if (pr.quit) return false;

			intiDbConfig();

			try {
				Connection conn = SharedApplication.getInstance().getROConnection();
				conn.close();
			} catch (Exception e) {
				System.err.println("Oops, a bad pwd, or lack of network access to the db?");
				e.printStackTrace();
				return false;
			}

			pr = promptCacheDirectory();
			if (pr.quit) return false;
		}

		pr = promptLogDetail();
		if (pr.quit) return false;

		return true;

	}


	public void loadModelValidators() {
		//override to add validators
	}

	/**
	 * If true, each test writes the headings for the test results.
	 * If false, the runner will do that so there is no duplication
	 * of headers.
	 * @return
	 */
	public boolean isResultHeaderWritten() {
		return resultHeaderWritten;
	}

	public void setResultHeaderWritten(boolean isWritten) {
		resultHeaderWritten = isWritten;
	}

	public boolean initModelValidators() {

		boolean ok = true;

		for (Calculator mv : getValidators()) {

			try {

				ok = mv.initTest(this);
				if (! ok) return false;

			} catch (Exception e) {
				log.error("Unable to initiate the test: " + mv.getClass(), e);
				return false;
			}

		}

		return true;
	}

	protected List<Calculator> getValidators() {
		return validators;
	}




	public void intiDbConfig() {

		if (useTestDb) {
		//Edit these props to point to test
		System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.137:1521:witest");
		System.setProperty("dbuser", "sparrow_dss");
		System.setProperty("dbpass", dbPwd);
		} else {
		//Production Properties
		System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.152:1521:widw");
		System.setProperty("dbuser", "sparrow_dss");
		System.setProperty("dbpass", dbPwd);
		}
	}

	public boolean initLoggingConfig() {


		log = Logger.getLogger(SparrowCalculationRunner.class); //logging for this class

		//Turn off logging for the lifecycle
		Logger.getLogger(LifecycleListener.class).setLevel(Level.ERROR);

		//This class relies on getting messages out at at least the info level
		if (! log.isEnabledFor(Level.INFO)) {
			log.setLevel(Level.INFO);
		}

		return true;

	}

	protected boolean initSystemConfig() {

		System.setProperty(LifecycleListener.APP_ENV_KEY, "local");
		System.setProperty(LifecycleListener.APP_MODE_KEY, "validation");


		System.setProperty(
				"gov.usgswim.sparrow.cachefactory.PredictDataFactory.ACTION_IMPLEMENTATION_CLASS",
				"gov.usgswim.sparrow.action.LoadModelPredictDataFromSerializationFile");
		System.setProperty(
				"gov.usgswim.sparrow.action.LoadModelPredictDataFromSerializationFile.FETCH_FROM_DB_IF_NO_LOCAL_FILE",
				"true");
		System.setProperty(
				"gov.usgswim.sparrow.action.LoadModelPredictDataFromSerializationFile.DATA_DIRECTORY",
				cacheDirectory);
		System.setProperty(
				"gov.usgswim.sparrow.action.PredictionContextHandler.DISABLE_DB_ACCESS",
				"true");


		//Tell JNDI config to not expect JNDI props
		System.setProperty(
				"gov.usgs.cida.config.DynamicReadOnlyProperties.EXPECT_NON_JNDI_ENVIRONMENT",
				"true");

		System.setProperty(LoadModelMetadata.SKIP_LOADING_PREDEFINED_THEMES, "true");


		lifecycle.contextInitialized(null, true);

		if (logLevel != null) {
			Logger baseLogger = Logger.getLogger("gov.usgswim.sparrow.validation");
			baseLogger.setLevel(logLevel);
		}

		return true;
	}


	public void promptIntro() {
		System.out.println("");
		System.out.println(": : SPARROW DSS Model Validator : :");
		System.out.println("The validator works in three modes:");
		System.out.println("1) Test a single model by entering the complete path to a single model, or");
		System.out.println("2) Test several models from a directory by entering a directoy, or");
		System.out.println("3) If none of the specified tests use text files, only database connection information will be asked.");
		System.out.println("If you enter a directory, you will be prompted for a start and end model number.");
		System.out.println("Enter 'quit' for any response to stop.");
	}

	public PromptResponse promptPathOrDir() {
		System.out.println("");
		System.out.println(": : Model Text Files : :");
		PromptResponse pathOrDir  = prompt("Enter a direcotry containing models or the complete path to a single model: ");


		if (pathOrDir.isEmptyOrNull()) {
			System.out.println("What?? - please try again.  Enter 'quit' to quit.");
			return promptPathOrDir();
		} else {
			if (pathOrDir.quit) return pathOrDir;


			String pathOrDirStr = pathOrDir.getNullTrimmedStrResponse();
			File f = new File(pathOrDirStr);

			if (f.exists()) {
				if (f.isFile()) {
					singleModelPath = f.getAbsolutePath();
					activeModelDirectory = f.getParent();
				} else if (f.isDirectory()) {
					activeModelDirectory = f.getAbsolutePath();
					return promptFirstLastModel();
				}

			} else {
				System.out.println("That file or directory does not exist - please try again. ('quit' to quit)");
				return promptPathOrDir();
			}


			return pathOrDir;

		}
	}

	public PromptResponse promptLogDetail() {

		System.out.println("");
		System.out.println(": : Logging Level : :");
		System.out.println("Select Logging Level using the first letter of these options:");
		System.out.println("* Error: (Default) Print single line messages for each row error.  No - additional (multi-line) details.");
		System.out.println("* Warn:	In addition to errors, print single line warnings for rows that are suspicious.");
		System.out.println("* Debug:	If available, print multi-line detail for each error.");
		System.out.println("* Trace:	Used to debug the tests themselves, this option prints additional info about successful values as well.");

		PromptResponse level  = prompt("Logging Level (E/W/D/T) or [Enter] to use the default 'Error' Level: ");

		if (level.quit) return level;
		String lvlStr = level.getNullTrimmedStrResponse();


		if ("".equals(lvlStr) || "e".equalsIgnoreCase(lvlStr)) {
			logLevel = Level.ERROR;
		} else if ("w".equalsIgnoreCase(lvlStr)) {
			logLevel = Level.WARN;
		} else if ("d".equalsIgnoreCase(lvlStr)) {
			logLevel = Level.DEBUG;
		} else if ("t".equalsIgnoreCase(lvlStr)) {
			logLevel = Level.TRACE;
		} else {
			System.out.println("Hmm, I didn't exactly understand that, but I'll take it to be the ERROR log level.");
			logLevel = Level.ERROR;
		}

		return level;

	}

	public PromptResponse promptFirstLastModel() {
		try {

			System.out.println("");
			System.out.println(": : Models to Run (based on text files) : :");
			PromptResponse firstIdStr  = prompt("Enter the ID of the first model to test: ");
			if (firstIdStr.quit) return firstIdStr;
			firstModelId = Integer.parseInt(firstIdStr.getNullTrimmedStrResponse());

			PromptResponse lastIdStr  = prompt("Enter the ID of the last model to test: ");
			if (lastIdStr.quit) return lastIdStr;
			lastModelId = Integer.parseInt(lastIdStr.getNullTrimmedStrResponse());

			return lastIdStr;
		} catch (Exception e) {
			System.out.println("I really need numbers for this part.  Lets try that again.");
			return promptFirstLastModel();
		}
	}

	public PromptResponse prompModelIds() {
		try {

			System.out.println("");
			System.out.println(": : Models to Run (based on database models) : :");
			PromptResponse idStrsResp  = prompt("Enter a list of model IDs, separated by a comma and/or space.  Enter 'p' to run all the public models: ");
			if (! idStrsResp.quit) {

				if (idStrsResp.isEmptyOrNull()) {
					log.error("I really need a list of IDs.  Lets try that again.  You can enter 'quit' to quit.");
					prompModelIds();
				} else {

				}

				if (idStrsResp.getNullTrimmedStrResponse().equalsIgnoreCase("p")) {
					attemptToLoadPublicModelsFromDb = true;
				} else {

					String[] idStrArray = StringUtils.split(idStrsResp.getNullTrimmedStrResponse(), ", \t");
					modelIds = new ArrayList<Long>();
					for (String s : idStrArray) {
						modelIds.add(Long.parseLong(s));
					}
				}
			}

			return idStrsResp;

		} catch (Exception e) {
			System.out.println("I really need a list of IDs.  Lets try that again.  You can enter 'quit' to quit.");
			return prompModelIds();
		}
	}


	public PromptResponse promptWhichDb() {

		System.out.println("");
		System.out.println(": : Database Connection : :");
		PromptResponse response = prompt("Which database should the validation test be run against?  (T)est or (P)roduction: ");
		if (response.quit) return response;

		if (response.isEmptyOrNull()) {
			System.out.println("Sorry, I didn't get that.");
			return promptWhichDb();
		} else {
			String strVal = response.getNullTrimmedStrResponse();
			if ("t".equalsIgnoreCase(strVal)) {
				useTestDb = true;
			} else if ("p".equalsIgnoreCase(strVal)) {
				useTestDb = false;
			} else {
				System.out.println("Sorry, I didn't get that.");
				return promptWhichDb();
			}
		}

		return response;
	}

	//useTestDb
	public PromptResponse promptPwd() {
		PromptResponse pwdResp = prompt("Enter the db password: ");
		if (! pwdResp.quit) {
			dbPwd = pwdResp.getStrResponse();
		}

		return pwdResp;
	}

	public PromptResponse promptCacheDirectory() {

		File defaultBaseDir = getDefaultCacheDirectory();
		File defaultDir = new File(defaultBaseDir, this.useTestDb ? "test_db" : "prod_db");

		System.out.println("");
		System.out.println(": : Caching : :");
		System.out.println("Model data from the database is cached on your local disk to speed up repeated test runs.");
		System.out.println("If model data has changed in the db since the last run, the contents of the cache will need to be manually deleted.");
		System.out.println("Below enter one of the following:");
		System.out.println("* 'quit' to Quit.");
		System.out.println("* A complete local path to the directory to use for caching.");
		System.out.println("* [Enter] to accept the default cache location, which is '" + defaultDir.getAbsolutePath() );

		PromptResponse resp = prompt("Enter the path to your model cache directory, or accept the default location: ");

		if (!resp.quit && ! resp.isEmptyOrNull()) {
			File f = new File(resp.getNullTrimmedStrResponse());
			resp.setObjectResponse(f);

			cacheDirectory = resp.getNullTrimmedStrResponse();
			System.err.println("Using user specified cache directory '" + cacheDirectory + "'");
		} if (!resp.quit && resp.isEmptyOrNull()) {
			//accept default directory

			cacheDirectory = defaultDir.getAbsolutePath();
			System.err.println("Using default cache directory '" + cacheDirectory + "'");
		}

		return resp;
	}

	public Level getTestLogLevel() {
		return logLevel;
	}

	public static PromptResponse prompt(String prompt) {

		//  prompt the user to enter their name
		System.out.print(prompt);

		//  open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String val = null;

		//  read the username from the command-line; need to use try/catch with the
		//  readLine() method
		try {
				val = br.readLine();
		} catch (IOException ioe) {
				System.out.println("IO error trying to read input!");
				System.exit(1);
		} finally {
			//br.close();
		}

		return new PromptResponse(val);
	}

	public static File getDefaultCacheDirectory() {

		System.out.println("User Home Path: "+ System.getProperty("user.home"));

		File home = new File(System.getProperty("user.home"));
		File cacheDir = new File(home, "sparrow");
		cacheDir = new File(cacheDir, "data_cache");

		return cacheDir;
	}

	public static class PromptResponse {

		private boolean quit = false;

		String strResponse;
		Object response;

		PromptResponse(String strResponse) {

			if (strResponse != null && "quit".equalsIgnoreCase(strResponse)) {
				this.quit = true;
			}

			this.strResponse = strResponse;
		}

		public void setObjectResponse(Object response) {
			this.response = response;
		}

		public boolean isQuit() {
			return quit;
		}

		public Object getObjectResponse() {
			return response;
		}

		public String getStrResponse() {
			return strResponse;
		}

		public boolean isEmptyOrNull() {
			return (StringUtils.trimToNull(strResponse) == null);
		}

		public String getNullTrimmedStrResponse() {
			return StringUtils.trimToNull(strResponse);
		}

		public Long parseAsLong() {
			try {
				Long l = Long.parseLong(strResponse);
				return l;
			} catch (Exception e) {
				return null;
			}
		}

		public Boolean parseAsBoolean() {
			try {
				String s = StringUtils.trimToNull(strResponse).toLowerCase();

				return ("y".equals(s) || "yes".equals(s) || "t".equals(s) || "true".equals(s));

			} catch (Exception e) {
				return null;
			}
		}
	}
}

