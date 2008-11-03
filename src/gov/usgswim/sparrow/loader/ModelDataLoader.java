package gov.usgswim.sparrow.loader;

import static gov.usgswim.sparrow.loader.ModelDataAssumptions.CONTACT_ID;
import static gov.usgswim.sparrow.loader.ModelDataAssumptions.DEFAULT_CONSTITUENT;
import static gov.usgswim.sparrow.loader.ModelDataAssumptions.DEFAULT_UNITS;
import static gov.usgswim.sparrow.loader.ModelDataAssumptions.ENH_NETWORK_ID;
import static gov.usgswim.sparrow.loader.ModelDataAssumptions.IS_POINT_SOURCE_DEFAULT;
import static gov.usgswim.sparrow.loader.ModelDataAssumptions.useDefaultIfUnavailable;
import static gov.usgswim.sparrow.loader.ModelDataAssumptions.useDefaultPrecisionIfUnavailable;
import static gov.usgswim.sparrow.loader.ModelDataAssumptions.useNameForDisplayNameIfUnavailable;
import gov.usgswim.sparrow.util.JDBCUtil;
import gov.usgswim.sparrow.util.SparrowSchemaConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import oracle.jdbc.driver.OracleDriver;

import org.apache.commons.lang.StringUtils;

/**
 * @author ilinkuo
 *
 */
public class ModelDataLoader {
	
	
	// IMPORTANT COLUMN NAMES
	public static final String ANC_REACH_IDENTIFIER = "local_id";
	public static final String ANC_FULL_IDENTIFIER = "std_id";
	public static String ANC_REACH_TYPE = "RCHTYPE";
	public static String ANC_PNAME = "PNAME";
	public static final String MMD_NAME = "name";
	public static final String MMD_DESCRIPTION = "DESCRIPTION";
	public static final String COEF_BOOT_ERROR = "BOOT_ERROR";
	public static final String COEF_TOT_DELIVF = "TOT_DELIVF";
	public static final String COEF_INC_DELIVF = "INC_DELIVF";
	public static final String COEF_ITER = "ITER";
	public static final String TOPO_TNODE = "tnode";
	public static final String TOPO_FNODE = "fnode";
	public static final String TOPO_IFTRAN = "iftran";
	public static final String TOPO_HYDSEQ = "hydseq";
	public static final String SMD_IS_POINT_SOURCE = "is_point_source";
	public static final String SMD_UNITS = "units";
	public static final String SMD_CONSTITUENT = "constituent";
	public static final String SMD_SORT_ORDER = "sort_order";
	public static final String SMD_DESCRIPTION = "description";
	public static final String SMD_NAME = "name";
	public static final String SMD_PRECISION = "precision";
	// 
	public static final String[] BASIC_COEF_COLUMNS = new String[] {COEF_ITER, COEF_INC_DELIVF, COEF_TOT_DELIVF, COEF_BOOT_ERROR};

	// SQL FILE NAMES
	private static final String MODEL_REACHES_INSERT_SQL_FILE = "model_reaches_insert.sql";
	private static final String SRC_VALUE_INSERT_SQL_FILE = "src_value_insert.sql";
	private static final String SRC_REACH_COEF_INSERT_SQL_FILE = "src_reach_coef_insert.sql";
	private static final String REACH_DECAY_COEF_INSERT_SQL_FILE = "reach_decay_coef_insert.sql";
	private static final String SRC_METADATA_INSERT_SQL_FILE = "src_metadata_insert.sql";
	private static final String MODEL_METADATA_SQL_FILE = "model_metadata_insert.sql";
	private static final String DELETE_SQL_FILE = "delete_all.sql";
	// CONNECTION INFO
	private static final String THIN_LOCAL_CONNECTION = "jdbc:oracle:thin:@localhost:1521:xe";
	private static final String THIN_WIDW_CONNECTION = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
	private static final String THIN_WIMAP_CONNECTION = "jdbc:oracle:thin:@130.11.165.76:1521:wimap";
	// SPECIAL DIRECTORIES
	private static final String REL_LOAD_DIR = "load";
	static File baseDir = new File("D:\\CRKData\\Sparrow\\raw_data\\mrb3_tp");
	static File verificationDir = new File(baseDir.getAbsolutePath() + "/vOutput" );

	
	

	// 
	private static String sparrow_dss_password; // populated by user on prompt
		
	public static void main(String[] args) throws SQLException{
		try {
			File modelMetadata = new File(baseDir.getAbsolutePath() + "/model_metadata.txt");
			deleteModel(ModelDataAssumptions.MODEL_ID, modelMetadata);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		}
		System.out.println("====== " + DELETE_SQL_FILE + " generated ====");

		try {
			File modelMetadata = new File(baseDir.getAbsolutePath() + "/model_metadata.txt");
			insertModelMetadata(ModelDataAssumptions.MODEL_ID, modelMetadata);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		}
		System.out.println("====== " + MODEL_METADATA_SQL_FILE + " generated ====");

		try {
			File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
			ModelDataLoader.insertSources(ModelDataAssumptions.MODEL_ID, sourceMetadata);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		}
		System.out.println("====== " + SRC_METADATA_INSERT_SQL_FILE + " generated ====");

		try {
			File topoData = new File(baseDir.getAbsolutePath() + "/topo.txt");
			File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
			ModelDataLoader.insertReaches(ModelDataAssumptions.MODEL_ID, topoData, ancillaryData);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		}
		System.out.println("====== " + MODEL_REACHES_INSERT_SQL_FILE + " generated ====");
		
		// TODO insert reach attributes for ?demiarea & demtarea =? catchment_area?
		// meanq, ...
		
		Connection conn = null;
		try {
			File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
			File coefData = new File(baseDir.getAbsolutePath() + "/coef.txt");
			conn = ModelDataLoader.getWIDWConnection();
			ModelDataLoader.insertReachDecayCoefs(conn, ModelDataAssumptions.MODEL_ID, coefData, ancillaryData);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		} finally {
			conn.close();
		}
		System.out.println("====== " + REACH_DECAY_COEF_INSERT_SQL_FILE + " generated ====");

		try {
			File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
			File coefData = new File(baseDir.getAbsolutePath() + "/coef.txt");
			File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
			conn = ModelDataLoader.getWIDWConnection();
			ModelDataLoader.insertSourceReachCoefs(conn, ModelDataAssumptions.MODEL_ID, coefData, sourceMetadata, ancillaryData);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		} finally {
			conn.close();
		}
		System.out.println("====== " + SRC_REACH_COEF_INSERT_SQL_FILE + " generated ====");
		
		try {
			File ancillaryData = new File(baseDir.getAbsolutePath() + "/ancil.txt");
			File sourceValuesData = new File(baseDir.getAbsolutePath() + "/src.txt");
			File sourceMetadata = new File(baseDir.getAbsolutePath() + "/src_metadata.txt");
			conn = ModelDataLoader.getWIDWConnection();
			ModelDataLoader.insertSourceValues(conn, ModelDataAssumptions.MODEL_ID, sourceValuesData, sourceMetadata, ancillaryData);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			return;
		} finally {
			conn.close();
		}
		System.out.println("====== " + SRC_VALUE_INSERT_SQL_FILE + " generated ====");
		
		System.out.println("== DONE. All SQL files successfully generated ==");
		
	}
	
	
	public static void deleteModel(Long modelID, File modelMetadata) throws IOException {
		
		BufferedWriter writer = makeOutWriter(modelMetadata, DELETE_SQL_FILE);		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String removeModel = "DELETE FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".SPARROW_MODEL WHERE SPARROW_MODEL_ID = " + modelID + ";";
			String removeReaches = "DELETE FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".MODEL_REACH WHERE SPARROW_MODEL_ID = " + modelID + ";";
			String removeSource = "DELETE FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".SOURCE WHERE SPARROW_MODEL_ID = " + modelID + ";";

			writer.write(removeModel); writer.write("\n");
			writer.write(removeReaches); writer.write("\n");
			writer.write(removeSource); writer.write("\n");
		} finally {
			writer.write("commit;\n"); // this is added in case the .sql file is to be run by say, SQL+
			writer.flush();
			writer.close();
		}
	}
	
	public static void insertModelMetadata(Long modelID, File modelMetadata) throws IOException {
		DataFileDescriptor md = validateModelMetadata(modelMetadata);
		assert(md.lines == 1):"there should only be one line for the model metadata (excluding headers)";
		
		BufferedReader reader = new BufferedReader(new FileReader(modelMetadata));
		BufferedWriter writer = makeOutWriter(modelMetadata, appendDateTimeSuffixToFileName(MODEL_METADATA_SQL_FILE));
		advancePastHeader(md, reader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = reader.readLine();			
			String[] inputValues = line.split(md.delimiter);
			List<String> values = new ArrayList<String>();
			String[] allFields = {"SPARROW_MODEL_ID", "NAME","DESCRIPTION","DATE_ADDED","CONTACT_ID","ENH_NETWORK_ID", "IS_APPROVED", "IS_PUBLIC", "IS_ARCHIVED"};
			{
				values.add((modelID == null)? null: Long.toString(modelID));
				// if no modelID submitted, SPARROW_MODEL_ID has an insert trigger using sequence SPARROW_MODEL_SEQ
				values.add(quoteForSQL(inputValues[md.indexOf(MMD_NAME)]));
				values.add(quoteForSQL(inputValues[md.indexOf(MMD_DESCRIPTION)]));
				values.add("SYSDATE");
				values.add(Integer.toString(CONTACT_ID));
				values.add(Integer.toString(ENH_NETWORK_ID));
				values.add(quoteForSQL(ModelDataAssumptions.IS_APPROVED_DEFAULT));
				values.add(quoteForSQL(ModelDataAssumptions.IS_PUBLIC_DEFAULT));
				values.add(quoteForSQL(ModelDataAssumptions.IS_ARCHIVED_DEFAULT));
			}
			
			String sql = "INSERT into " + SparrowSchemaConstants.SPARROW_SCHEMA + ".SPARROW_MODEL ("
			+ joinInParallel(allFields, ",", values) + ")"
			+" VALUES (" + (join(values, ",") + ");\n");
			
			writer.write(sql.toString());
		} finally {
			writer.write("commit;\n"); // this is added in case the .sql file is to be run by say, SQL+
			writer.flush();
			writer.close();
			reader.close();
		}
	}

	public static void insertSources(Long modelID, File sourceMetadata) throws IOException {
		DataFileDescriptor md = validateSourceMetadata(sourceMetadata);

		BufferedReader reader = new BufferedReader(new FileReader(sourceMetadata));
		BufferedWriter writer = makeOutWriter(sourceMetadata, appendDateTimeSuffixToFileName(SRC_METADATA_INSERT_SQL_FILE));
		advancePastHeader(md, reader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			int identifier = 0; // FACT: identifier is 1-based and
								// autogenerated, forming a Unique id together
								// with SPARROW_MODEL_ID
			String[] allFields = { "SOURCE_ID", "NAME", "DESCRIPTION",
					"SORT_ORDER", "SPARROW_MODEL_ID", 
					"IDENTIFIER", "DISPLAY_NAME", "CONSTITUENT", "UNITS", "PRECISION", 
					"IS_POINT_SOURCE", "IS_APPROVED", "IS_PUBLIC"};
			
			while ( (line=reader.readLine()) != null) {
				if (line.trim().length() == 0) continue;
				identifier++;
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);
				assert(inputValues[md.indexOf("id")].equals(inputValues[md.indexOf("sort_order")]));
				
				List<String> values = new ArrayList<String>();
				{
					values.add(null); //SOURCE_ID has insert trigger using sequence SOURCE_SEQ
					values.add(quoteForSQL(inputValues[md.indexOf(SMD_NAME)]));
					values.add(quoteForSQL(inputValues[md.indexOf(SMD_DESCRIPTION)])); 
					values.add(inputValues[md.indexOf(SMD_SORT_ORDER)]);
					values.add(Long.toString(modelID)); 
					values.add(Integer.toString(identifier));
					String displayName = useNameForDisplayNameIfUnavailable(md, inputValues);
					values.add(displayName);
					
					String constituent = useDefaultIfUnavailable(md, inputValues, SMD_CONSTITUENT, DEFAULT_CONSTITUENT);
					values.add(quoteForSQL(constituent)); 
					
					String units = useDefaultIfUnavailable(md, inputValues, SMD_UNITS, DEFAULT_UNITS);
					values.add(quoteForSQL(units));
					
					String precision = useDefaultPrecisionIfUnavailable(md, inputValues);
					values.add(precision);
					

					String isPointSourceValue = useDefaultIfUnavailable(md, inputValues, SMD_IS_POINT_SOURCE, IS_POINT_SOURCE_DEFAULT);
					String isPointSource = ModelDataAssumptions.translatePointSource(isPointSourceValue);
					values.add(quoteForSQL(isPointSource));
				}
				
				String sql = "INSERT into " + SparrowSchemaConstants.SPARROW_SCHEMA 
				+ ".SOURCE (" + joinInParallel(allFields, ",", values) + ")" // 10
				+ " VALUES (" + join(values, ",") + ");\n";
				
				writer.write(sql);
			}
		} finally {
			writer.write("commit;\n"); // this is added in case the .sql file is to be run by say, SQL+
			writer.flush();
			writer.close();
			reader.close();
		}
	}

	/**
	 * Following JDBCUtil.writeModelReaches()
	 * 
	 * @param conn
	 * @param modelID
	 * @param topoData
	 * @param ancillaryData
	 * @throws IOException
	 * @throws SQLException 
	 */
	public static void insertReaches(long modelID, File topoData, File ancillaryData) throws IOException, SQLException {
		DataFileDescriptor md = validateTopologicalData(topoData);		
		DataFileDescriptor amd = validateAncillaryData(ancillaryData);
		assert(md.lines == amd.lines);
		
		BufferedReader tReader = new BufferedReader(new FileReader(topoData));
		BufferedReader aReader = new BufferedReader(new FileReader(ancillaryData));
		BufferedWriter writer = makeOutWriter(topoData, appendDateTimeSuffixToFileName(MODEL_REACHES_INSERT_SQL_FILE));
		advancePastHeader(md, tReader);
		
		int stdIdMatchCount = 0;	//Number of reaches where the STD_ID matched an enh reach
		int stdIdNullCount = 0;		//Number of reaches where the STD_ID is null (actually, counting zero as null)
		int stdIdNotMatched = 0;	//Number of reaches where the STD_ID is assigned, but not matched.
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			String aline = null;
			int identifier = 0; // FACT: identifier is 1-based and autogenerated
			advancePastHeader(amd, aReader);
			String[] allFields = {"IDENTIFIER","FULL_IDENTIFIER","HYDSEQ","IFTRAN","ENH_REACH_ID",
					"SPARROW_MODEL_ID","FNODE","TNODE"};
			Map<Integer, Integer> enhancedReachIDMap = readNetworkReaches();
			
			while ( (line=tReader.readLine()) != null) {
				aline = aReader.readLine();
				if (line.trim().length() == 0) {continue;}
				
				//identifier++;
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] topoInputValues = (line + md.delimiter + "_").split(md.delimiter);
				String[] ancilInputValues = (aline + amd.delimiter + "_").split(amd.delimiter);
				
				List<String> values = new ArrayList<String>();
				{
					values.add(ancilInputValues[amd.indexOf(ANC_REACH_IDENTIFIER)]);
					values.add(quoteForSQL(ancilInputValues[amd.indexOf(ANC_FULL_IDENTIFIER)]));
					values.add(topoInputValues[md.indexOf(TOPO_HYDSEQ)]);
					values.add(topoInputValues[md.indexOf(TOPO_IFTRAN)]);
					{
						String std_id = ancilInputValues[amd.indexOf(ANC_FULL_IDENTIFIER)];
						if (std_id == null || std_id.length() == 0 || std_id.equals("0")) {
							values.add(null);
							stdIdNullCount++; // no std_id or 0 std_id provided
						} else {
							Integer match = enhancedReachIDMap.get(Integer.valueOf(std_id));
							if (match != null) { 
								values.add(match.toString());
								stdIdMatchCount++; // successful match
							} else {
								values.add(null);
								stdIdNotMatched++; // no corresponding enh_id
							}
						}
					}
					values.add(Long.toString(modelID));
					values.add(topoInputValues[md.indexOf(TOPO_FNODE)]);
					values.add(topoInputValues[md.indexOf(TOPO_TNODE)]);
				}
				
				String sql = "INSERT INTO " + SparrowSchemaConstants.SPARROW_SCHEMA + ".MODEL_REACH "
				+ "(" + joinInParallel(allFields, ",", values) + ")"
				+ " VALUES (" + join(values, ",") + ");\n";
				
				writer.write(sql);
			}
		} finally {
			writer.write("commit;\n"); // this is added in case the .sql file is to be run by say, SQL+
			writer.flush();
			writer.close();
			tReader.close();
			aReader.close();
		}
	}
	
	public static void insertReachDecayCoefs(Connection conn, Long modelID, File reachCoefdata, File ancilData) throws IOException, SQLException {
		assert(conn != null) : "Connection required to do id lookups";
		DataFileDescriptor md = validateCoefData(reachCoefdata);
		DataFileDescriptor amd = validateAncillaryData(ancilData);
		
		// -----
		// SETUP
		// -----
		BufferedReader dcReader = new BufferedReader(new FileReader(reachCoefdata));
		BufferedReader aReader = new BufferedReader(new FileReader(ancilData));
		BufferedWriter writer = makeOutWriter(reachCoefdata, appendDateTimeSuffixToFileName(REACH_DECAY_COEF_INSERT_SQL_FILE));
		Map<Integer, Integer> reachIDLookup = retrieveModelReachIDLookup(conn, modelID);
		advancePastHeader(md, dcReader);
		advancePastHeader(amd, aReader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			String aLine = null;
			while ( (line=dcReader.readLine()) != null) {
				aLine = aReader.readLine();
				if (line.trim().length() == 0) continue;
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);
				String[] ancilValues = (aLine + amd.delimiter + "_").split(amd.delimiter);

				String[] values = new String[5];
				{
					values[0] = inputValues[md.indexOf(COEF_ITER)];
					values[1] = inputValues[md.indexOf(COEF_INC_DELIVF)]; 
					values[2] = inputValues[md.indexOf(COEF_TOT_DELIVF)]; 
					values[3] = inputValues[md.indexOf(COEF_BOOT_ERROR)];

					Integer identifier = Integer.valueOf(ancilValues[amd.indexOf(ANC_REACH_IDENTIFIER)]);
					values[4] = Integer.toString(reachIDLookup.get(identifier));
				}
				
				String sql = "INSERT INTO REACH_COEF (ITERATION, INC_DELIVERY, TOTAL_DELIVERY, BOOT_ERROR, MODEL_REACH_ID) "
				+ " VALUES (" + StringUtils.join(values, ",") + ");\n";
				
				writer.write(sql);
			}
		} finally {
			writer.write("commit;\n"); // this is added in case the .sql file is to be run by say, SQL+
			writer.flush();
			writer.close();
			dcReader.close();
			aReader.close();
		}
	}
	
	public static void insertSourceReachCoefs(Connection conn, Long modelID,
			File coefData, File sourceMetadata, File ancilData)
			throws IOException, SQLException {
		// ----------
		// VALIDATION
		// ----------
		assert(conn != null): "Connection required to do id lookups";
		DataFileDescriptor md = validateCoefData(coefData);
		validateSourceMetadata(sourceMetadata);
		DataFileDescriptor amd = validateAncillaryData(ancilData);
		
		String[] sources = readSourceNames(sourceMetadata);
		String[] cappedSources = ModelDataAssumptions.addPrefixAndCapitalize(sources);
		assert(md.hasColumns(cappedSources));

		// -----
		// SETUP
		// -----
		BufferedReader coefReader = new BufferedReader(new FileReader(coefData));
		BufferedReader ancilReader = new BufferedReader(new FileReader(ancilData));
		BufferedWriter writer = makeOutWriter(coefData, appendDateTimeSuffixToFileName(SRC_REACH_COEF_INSERT_SQL_FILE));
		Map<Integer, Integer> reachIDLookup = retrieveModelReachIDLookup(conn, modelID);
		Map<Integer, Integer> sourceIDLookup = retrieveSourceIDLookup(conn, modelID);
		advancePastHeader(md, coefReader);
		advancePastHeader(amd, ancilReader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			String aLine = null;

			int lineCount = 0;
			
			while ( (line=coefReader.readLine()) != null) {
				// complications to handle the possibility that ancil must be
				// looped once for each iteration
				aLine = ancilReader.readLine();
				if (aLine != null && aLine.trim().length() == 0) {
					aLine = ancilReader.readLine();
				}
				if (aLine == null) {
					ancilReader.close();
					// reopen the file
					ancilReader = new BufferedReader(new FileReader(ancilData));
					advancePastHeader(amd, ancilReader);
					aLine = ancilReader.readLine();
				}
				if (line.trim().length() == 0 && aLine.trim().length() == 0) continue;
				
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);
				String[] ancilValues = (aLine + amd.delimiter + "_").split(amd.delimiter);
				
				Integer identifier = Integer.valueOf(ancilValues[amd.indexOf(ANC_REACH_IDENTIFIER)]);
				Integer modelReachID = reachIDLookup.get(identifier);
				String modelReachIDString = Integer.toString(modelReachID);
				
				for (Integer i=1; i <=sourceIDLookup.size(); i++) {
					// source ids are 1-based.
					String[] values = new String[4];
					values[0] = inputValues[md.indexOf(COEF_ITER)];
					values[1] = inputValues[BASIC_COEF_COLUMNS.length -1 + i]; // the ith coef after the last basic coef
					values[2] = Integer.toString(sourceIDLookup.get(i));
					values[3] = modelReachIDString;
					
					// NOTE: SOURCE_REACH_COEF_SEQ autoinserts value of SOURCE_REACH_COEF_ID
					String sql = "INSERT into " + SparrowSchemaConstants.SPARROW_SCHEMA 
					+ ".SOURCE_REACH_COEF (ITERATION, VALUE, SOURCE_ID, MODEL_REACH_ID)"
					+ " VALUES (" + StringUtils.join(values, ",") + ");\n";
					
					writer.write(sql);
				}
			}
		} finally {
			writer.write("commit;\n"); // this is added in case the .sql file is to be run by say, SQL+
			writer.flush();
			writer.close();
			coefReader.close();
			ancilReader.close();
		}
	}
	
	public static void insertSourceValues(Connection conn, Long modelID, 
			File sourceValuesData, File sourceMetadata, File ancilData)
			throws IOException, SQLException {
		// ----------
		// VALIDATION
		// ----------
		assert(conn != null) : "Connection required to do id lookups";
		DataFileDescriptor md = validateSourceData(sourceValuesData);
		DataFileDescriptor smd = validateSourceMetadata(sourceMetadata);
		DataFileDescriptor amd = validateAncillaryData(ancilData);
		
		String[] sources = readSourceNames(sourceMetadata);
		assert(Arrays.equals(md.getHeaders(), sources)) : "the order should be the same";

		// -----
		// SETUP
		// -----
		BufferedReader sourceReader = new BufferedReader(new FileReader(sourceValuesData));
		BufferedReader ancilReader = new BufferedReader(new FileReader(ancilData));
		BufferedWriter writer = makeOutWriter(sourceValuesData, appendDateTimeSuffixToFileName(SRC_VALUE_INSERT_SQL_FILE));
		
		Map<Integer, Integer> reachIDLookup = retrieveModelReachIDLookup(conn, modelID);
		Map<Integer, Integer> sourceIDLookup = retrieveSourceIDLookup(conn, modelID);
		advancePastHeader(md, sourceReader);
		advancePastHeader(amd, ancilReader);
		
		// ----------------
		// GENERATE THE SQL
		// ----------------
		try {
			String line = null;
			String aLine = null;

			int lineCount = 0;
			
			
			while ( (line=sourceReader.readLine()) != null) {
				aLine = ancilReader.readLine();
				if (line.trim().length() == 0 && aLine.trim().length() == 0) continue;
				
				// HACK: trailing "_" added because split() will drop trailing empty spaces
				String[] inputValues = ModelDataAssumptions.sedimentIrregularHack(md, line).split(md.delimiter); // 
				String[] ancilValues = (aLine + amd.delimiter + "_").split(amd.delimiter);
				
				Integer identifier = Integer.valueOf(ancilValues[amd.indexOf(ANC_REACH_IDENTIFIER)]);
				Integer modelReachID = reachIDLookup.get(identifier);
				String modelReachIDString = Integer.toString(modelReachID);
				
				for (Integer i=1; i <=sourceIDLookup.size(); i++) {
					// source ids are 1-based.
					String[] values = new String[3];
					values[0] = inputValues[i-1];
					values[1] = Integer.toString(sourceIDLookup.get(i));
					values[2] = modelReachIDString;
					
					// NOTE: SOURCE_REACH_COEF_SEQ autoinserts value of SOURCE_REACH_COEF_ID
					String sql = "INSERT INTO source_value (VALUE, SOURCE_ID, MODEL_REACH_ID) "
					+ " VALUES (" + StringUtils.join(values, ",") + ");\n";
					
					writer.write(sql);
				}
			}
		} finally {
			writer.write("commit;\n"); // this is added in case the .sql file is to be run by say, SQL+
			writer.flush();
			writer.close();
			sourceReader.close();
			ancilReader.close();
		}
	}

	public static DataFileDescriptor validateSourceData(File srcValData)
	throws IOException {
		assert (srcValData !=  null);
		assert (srcValData.exists()) : srcValData.getAbsolutePath() + " does not exist";
		assert(srcValData.getName().equals("src.txt"));
		DataFileDescriptor md = Analyzer.analyzeFile(srcValData);
		assert(md.hasColumnHeaders());
		return md;
	}
	
	public static DataFileDescriptor validateCoefData(File reachCoefdata)
			throws IOException {
		assert (reachCoefdata !=  null);
		assert (reachCoefdata.exists()) : reachCoefdata.getAbsolutePath() + " does not exist";
		assert(reachCoefdata.getName().equals("coef.txt"));
		DataFileDescriptor md = Analyzer.analyzeFile(reachCoefdata);
		assert(md.hasColumnHeaders());
		assert(md.hasColumns(BASIC_COEF_COLUMNS));
		return md;
	}
	public static DataFileDescriptor validateModelMetadata(File modelMetadata)
			throws IOException {
		assert (modelMetadata !=  null);
		assert (modelMetadata.exists()) : modelMetadata.getAbsolutePath() + " does not exist";
		assert (modelMetadata.getName().equals("model_metadata.txt"));
		DataFileDescriptor fileMetaData = Analyzer.analyzeFile(modelMetadata);
		assert (fileMetaData.hasColumnHeaders());
		assert (fileMetaData.hasColumns("network", "name", "description",
				"constituent", "units", "precision"));
		return fileMetaData;
	}
	
	public static DataFileDescriptor validateSourceMetadata(File sourceMetadata)
			throws IOException {
		assert (sourceMetadata !=  null);
		assert (sourceMetadata.exists()) : sourceMetadata.getAbsolutePath() + " does not exist";
		assert(sourceMetadata.getName().equals("src_metadata.txt"));
		DataFileDescriptor md = Analyzer.analyzeFile(sourceMetadata);
		assert(md.hasColumnHeaders());
		assert(md.hasColumns("id", "sort_order", "name", "display_name", "description", 
				"constituent", "units", "precision", "is_point_source"));
		return md;
	}
	
	public static DataFileDescriptor validateTopologicalData(File topoData)
			throws IOException {
		assert (topoData !=  null);
		assert (topoData.exists()) : topoData.getAbsolutePath() + " does not exist";
		assert(topoData.getName().equals("topo.txt"));
		DataFileDescriptor tmd = Analyzer.analyzeFile(topoData);
		assert(tmd.hasColumnHeaders());
		assert(tmd.hasColumns(TOPO_FNODE, TOPO_TNODE, TOPO_IFTRAN, "hydseq"));
		return tmd;
	}
	public static DataFileDescriptor validateAncillaryData(File ancillaryData)
			throws IOException {
		assert (ancillaryData !=  null);
		assert (ancillaryData.exists()) : ancillaryData.getAbsolutePath() + " does not exist";
		assert(ancillaryData.getName().equals("ancil.txt"));
		DataFileDescriptor amd = Analyzer.analyzeFile(ancillaryData);
		assert(amd.hasColumnHeaders());
		assert(amd.hasColumns(ANC_REACH_IDENTIFIER, ANC_FULL_IDENTIFIER, "new_or_modified",
				"hydseq", "demiarea", "demtarea", "meanq", "station_id", "del_frac" ));
		if (!amd.hasColumns(ANC_REACH_TYPE)) {
			ANC_REACH_TYPE = "rchtype";
		}
		if (!amd.hasColumns(ANC_PNAME)) {
			ANC_PNAME = "pname";
		}
		assert(amd.hasColumns(ANC_REACH_TYPE, ANC_PNAME));
		
		String[] optionalColumns = {"waterid", 
				 "delivery_target", "RR", 
				"CONTFLAG",  "HEADFLAG", "TERMFLAG", "RESCODE", 
				 "statid"};
		// mrb_id rather than waterid
		// target rather than deliver_target
		//?? RR
		// headflag rather than CONTFLAG
		// staid =? statid
		
		return amd;
	}
	
	public static Map<Integer, Integer> retrieveModelReachIDLookup(
			Connection conn, long modelID) throws SQLException {
		// NOTE: unlike the other functions, this one MUST hit the database, as
		// the model_reach_ids are generally generated via a sequence in an
		// insert trigger
		String selectAllReachesQuery = "SELECT IDENTIFIER, MODEL_REACH_ID FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".MODEL_REACH WHERE SPARROW_MODEL_ID = " 
		+ modelID;
		Map<Integer, Integer> modelIdMap = null;
		try {
			modelIdMap = JDBCUtil.buildIntegerMap(conn, selectAllReachesQuery);
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RuntimeException("Array index out of bounds because reaches need to be inserted "
					+ "first into the database. Please insert reaches and rerun " 
					+ ModelDataLoader.class.getSimpleName(), e);
		}
		return modelIdMap;
	}
	
	public static Map<Integer, Integer> retrieveSourceIDLookup(
			Connection conn, long modelID) throws SQLException {
		// NOTE: unlike the other functions, this one MUST hit the database, as
		// the source_ids are generally generated via a sequence in an
		// insert trigger
		String sourceMapQuery = "SELECT IDENTIFIER, SOURCE_ID FROM " + SparrowSchemaConstants.SPARROW_SCHEMA + ".SOURCE WHERE SPARROW_MODEL_ID = " + modelID + " ORDER BY SORT_ORDER";
		Map<Integer, Integer> sourceIdMap = JDBCUtil.buildIntegerMap(conn, sourceMapQuery);
		// ordered map necessary here (but probably can be avoided.
		TreeMap<Integer, Integer> result = new TreeMap<Integer, Integer>();
		result.putAll(sourceIdMap);
		return result;
	}
	
	// ==============
	// LOOKUP METHODS
	// ==============
	private static String[] readSourceNames(File sourceMetadata) throws IOException {
		if (sourceMetadata ==  null || !sourceMetadata.exists()) return null;
		assert(sourceMetadata.getName().equals("src_metadata.txt"));
		DataFileDescriptor md = Analyzer.analyzeFile(sourceMetadata);
		assert(md.hasColumnHeaders());
		assert(md.hasColumns("id", "sort_order", "name", "display_name", "description", 
				"constituent", "units", "precision", "is_point_source"));
		
		BufferedReader reader = new BufferedReader(new FileReader(sourceMetadata));
		List<String> headers = new ArrayList<String>();
		try {
			String line = null;
			int identifier = 0; // FACT: identifier is 1-based and autogenerated
								advancePastHeader(md, reader);
			while ( (line=reader.readLine()) != null) {
				if (line.trim().length() == 0) continue;
				String[] inputValues = (line + md.delimiter + "_").split(md.delimiter);

				headers.add(inputValues[md.indexOf("name")]);
			}

		} finally {
			reader.close();
		}
		return headers.toArray(new String[headers.size()]);
	}
	
	public static Map<Integer, Integer> readNetworkReaches() throws SQLException {
		String enhReachQuery = "SELECT IDENTIFIER, ENH_REACH_ID FROM " + SparrowSchemaConstants.NETWORK_SCHEMA + ".ENH_REACH WHERE ENH_NETWORK_ID = " 
		+ ModelDataAssumptions.ENH_NETWORK_ID;
		return JDBCUtil.buildIntegerMap(getWIDWConnection(), enhReachQuery);
	}
	
	// ===============
	// UTILITY METHODS
	// ===============
	public static String appendDateTimeSuffixToFileName(String fileName) {
		if (fileName == null) return null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String dateSuffix = formatter.format(new Date());
		return appendSuffixToFileName(fileName, "_" + dateSuffix);

	}
	
	public static String appendSuffixToFileName(String fileName, String suffix) {
		if (fileName == null) return null;
		if (suffix == null) return fileName;
		int pos = fileName.lastIndexOf(".");
		if (pos > 0) {
			fileName = fileName.substring(0, pos) + suffix + fileName.substring(pos);
		} else {
			fileName += suffix;
		}
		return fileName;
	}
	
	private static BufferedWriter makeOutWriter(File sourceValuesData, String fileName)
	throws IOException {
		String loadScriptsDirPath = sourceValuesData.getParent() + "/" + REL_LOAD_DIR;
		File loadScriptsDir = new File(loadScriptsDirPath);
		if (!loadScriptsDir.exists()) throw new IOException("Please create a load scripts directory first at " + loadScriptsDirPath);
		String sqlOutputFilePath =  loadScriptsDir + "/" + fileName;
		File outputFile = new File(sqlOutputFilePath);
		return new BufferedWriter(new FileWriter(outputFile), 8192);
	}
	
	private static void advancePastHeader(DataFileDescriptor md,
			BufferedReader coefReader) throws IOException {
		String line;
		if (md.hasColumnHeaders()) {
			line = coefReader.readLine();
		}
	}
	
	public static String quoteForSQL(String value) {
		if (value == null || value.length() == 0) return null;
		StringBuffer quotedString = new StringBuffer("'");
		// escape all single quotes for SQL
		quotedString.append(value.replaceAll("'", "''")).append("'");
		return quotedString.toString();
	}
	
	public static StringBuffer join(List<String> values, String delimiter) {
		delimiter = (delimiter == null)? ",": delimiter;
		StringBuffer result = new StringBuffer();
		for (int i=0; i<values.size(); i++) {
			String value = values.get(i);
			if (value != null && value.length() > 0) {
				if (result.length() > 0) {
					result.append(delimiter);
				}
				result.append(value);
			}
		}
		return result;
	}
	
	public static StringBuffer joinInParallel(String[] values, String delimiter, List<String> governors) {
		delimiter = (delimiter == null)? ",": delimiter;
		StringBuffer result = new StringBuffer();
		for (int i=0; i<governors.size(); i++) {
			String governor = governors.get(i);
			if (governor != null && governor.length() > 0) {
				if (result.length() > 0) {
					result.append(delimiter);
				}
				result.append(values[i]);
			}
		}
		return result;
	}
	
	public static String prompt(String message) throws IOException {
		System.out.println(message);
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String response = in.readLine();
		in.close();
		return response;
	}
	// ==================
	// CONNECTION METHODS
	// ==================

	public static Connection getWIDWConnection(){
		return getSparrowDSSConnection(THIN_WIDW_CONNECTION, sparrow_dss_password);
	}

	public static Connection getWIMAPConnection(){
		return getSparrowDSSConnection(THIN_WIMAP_CONNECTION, sparrow_dss_password);
	}
	
	public static Connection getDevelopmentConnection() {
		return getSparrowDSSConnection(THIN_WIMAP_CONNECTION, "admin");
	}
	
	public static Connection getSparrowDSSConnection(String connectionString, String password) {
		try {
			DriverManager.registerDriver(new OracleDriver());		

			String username = "SPARROW_DSS";
			if (password == null) {
				password = promptSparrowPassword();
			}
			String thinConn = connectionString;

			Connection result = DriverManager.getConnection(thinConn,username,password);
			System.out.println("password accepted");
			return result;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(System.err);
		} catch (IOException e) {
			System.err.println("incorrect SPARROW_DSS password entered?");
			e.printStackTrace();
		}
		return null;
	}


	private static String promptSparrowPassword() throws IOException {
		if (sparrow_dss_password == null) {
			// WARNING: don't ever hardcode this password in.
			sparrow_dss_password = prompt("Please type the A password for user SPARROW_DSS");
		}
		return sparrow_dss_password;
	}




	

	

}
