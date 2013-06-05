package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.*;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.navigation.PredictDataTestScenarios;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DLUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author eeverman
 */
public abstract class CalcFractionalAreaBaseTest extends SparrowTestBase {

	protected static Long	network1_model_id = 1000L;
	protected static TopoData network1_topo;
	protected static DataTable network1_inc_area;
	protected static ModelReachAreaRelations network1_reach_state_relation;
	protected static DataTable network1_region_detail;
	protected static TerminalReaches network1_term_to_11;



	protected static TopoData testTopo2;	//An example of a braided stream
	protected static TopoData testTopoCorrected;	//An example of Fracs not adding to one.


	static final double COMP_ERROR = .0000001d;


	public CalcFractionalAreaBaseTest() {
	}

	public void copyColumnAsRowId(DataTableWritable table, int columnIndex) {
		for (int i = 0; i < table.getRowCount(); i++) {
			long id = table.getLong(i, columnIndex);
			table.setRowId(id, i);
		}
	}

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//log.setLevel(Level.DEBUG);
		super.doOneTimeCustomSetup();

		loadNetwork1();

		//Topo file with a braided stream
		testTopo2 = loadTopo("topo2");

		//A topo files with 'errors' that we can detect and correct.
		//The result should be the same, even with the frac errors.
		//Reach 10:		The frac is .7 instead of 1 as it should be.
		//Reaches 11 & 12:  Insead of fracs of .9 and .1 respectively, the fracs are
		//		instead .09 and .01.
		testTopoCorrected = loadTopo("topo_corrected");

	}


	public void loadNetwork1() throws Exception {
		String subpackage = "network1";

		network1_topo = loadTopo(subpackage, "topo.tab");
		cachePredictData(network1_topo, network1_model_id);

		network1_inc_area = loadIncrementalArea(subpackage, "incarea.tab");
		network1_reach_state_relation = loadModelReachAreaRelations(subpackage, "reach_state_relation.tab", network1_topo);
		network1_region_detail = loadRegionDetail(subpackage, "state_detail.tab");

		ArrayList<String> to_11_targets = new ArrayList<String>();
		to_11_targets.add("11");
		
		network1_term_to_11 = new TerminalReaches(network1_model_id, to_11_targets);
	}

	public void  cachePredictData(TopoData topo, Long modelId) {
		PredictDataBuilder pd = new PredictDataBuilder();
		{
			DataTable coef = null;
			DataTable src = null;
			DataTable decay = null;

			pd.setSrcMetadata( null);
			pd.setTopo(topo);
			pd.setCoef( src );
			pd.setDelivery( decay );
			pd.setSrc( src);
		}

		ConfiguredCache.PredictData.put(modelId, pd.toImmutable());
	}

	public TopoData loadTopo(String subpackage, String fileName) throws IOException {

		String basePackage = this.getClass().getPackage().getName();
		InputStream fileInputStream = getResource(basePackage + "." + subpackage, fileName);

		BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileInputStream));
		String[] headings = {"MODEL_REACH", "FNODE", "TNODE", "IFTRAN", "HYDSEQ", "SHORE_REACH", "FRAC"};
		Class<?>[] types = {Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Double.class};
		DataTableWritable dtw = new SimpleDataTableWritable(headings, null, types);
		dtw.setName("topo");
		DataTableUtils.fill(dtw, fileReader, false, "\t", true);
		fileReader.close();
		//repopulate hydseq to be 1 and up, w/o gaps
		for (int r = 0; r < dtw.getRowCount(); r++) {
			dtw.setValue(new Integer(r + 1), r, 4);
		}
		//Normally the db version has the identifier as the row ID and the
		//model_reach (the db key) as column 0.  Here we are duplicating
		//the identifier (mrb_id) into both.
		copyColumnAsRowId(dtw, 0);

		dtw.buildIndex(PredictData.TOPO_TNODE_COL);
		TopoDataImm topoTable = new TopoDataImm(dtw.getColumns(), dtw.getName(), dtw.getDescription(), dtw.getProperties(), dtw.getIndex());

		assert(topoTable.hasRowIds()): "topo should have IDENTIFIER as row ids";
		assert(topoTable.isIndexed(PredictData.TOPO_TNODE_COL));


		return topoTable;
	}

	/**
	 * Returns an ordered DataTable of all topological data in the MODEL
	 * <h4>Data Columns</h4>
	 * One row per reach (i = reach index)
	 * <h5>Row ID: MRB_ID from txt, which is the same as IDENTIFIER from DB</h5>
	 * <ul>
	 * <li>[i][0]MODEL_REACH** - Copy of text MRB_ID, since no db id.
	 * <li>[i][1]FNODE - The from node
	 * <li>[i][2]TNODE - The to node
	 * <li>[i][3]IFTRAN - 1 if this reach transmits to its end node, 0 otherwise
	 * <li>[i][4]HYDSEQ - Hydrologic sequence order (starting at 1, no gaps)
	 * <li>[i][5]SHORE_REACH - 1 if a shore reach, 0 otherwise.
	 * <li>[i][6]FRAC - Fraction of the upstream load/flow entering this reach.  Non-one at a diversion.
	 * </ul>
	 *
	 * **Differs from db version of loading.
	 *
	 * <h4>Sorting</h4>
	 * As per the text file.  This should match a sorting of HYDSEQ then
	 * IDENTIFIER from the db.
	 *
	 * For complete data definitions, please see:
	 * @see gov.usgswim.sparrow.PredictData#getTopo()
	 *
	 * @param modelId	The ID of the Sparrow model
	 * @return Fetched data - see Data Columns above.
	 */
	public TopoData loadTopo(String fileNameSuffix) throws IOException {
		InputStream fileInputStream = getResource(CalcFractionalAreaBaseTest.class, fileNameSuffix, "txt");
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileInputStream));
		String[] headings = {"MODEL_REACH", "FNODE", "TNODE", "IFTRAN", "HYDSEQ", "SHORE_REACH", "FRAC"};
		Class<?>[] types = {Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Double.class};
		DataTableWritable dtw = new SimpleDataTableWritable(headings, null, types);
		dtw.setName("topo");
		DataTableUtils.fill(dtw, fileReader, false, "\t", true);
		fileReader.close();
		//repopulate hydseq to be 1 and up, w/o gaps
		for (int r = 0; r < dtw.getRowCount(); r++) {
			dtw.setValue(new Integer(r + 1), r, 4);
		}
		//Normally the db version has the identifier as the row ID and the
		//model_reach (the db key) as column 0.  Here we are duplicating
		//the identifier (mrb_id) into both.
		copyColumnAsRowId(dtw, 0);

		dtw.buildIndex(PredictData.TOPO_TNODE_COL);
		TopoDataImm topoTable = new TopoDataImm(dtw.getColumns(), dtw.getName(), dtw.getDescription(), dtw.getProperties(), dtw.getIndex());

		assert(topoTable.hasRowIds()): "topo should have IDENTIFIER as row ids";
		assert(topoTable.isIndexed(PredictData.TOPO_TNODE_COL));


		return topoTable;
	}

	public DataTable loadIncrementalArea(String subpackage, String fileName) throws IOException {

		String basePackage = this.getClass().getPackage().getName();
		InputStream fileInputStream = getResource(basePackage + "." + subpackage, fileName);
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileInputStream));

		String[] headings = {
				"MODEL_REACH",	//actually the MRB_ID in the file
				"INC_AREA"
				};

		Class<?>[] types= {
				Integer.class,	//[i][0]MODEL_REACH
				Double.class		//[i][1]Incremental area
				};
		DataTableWritable incArea = new SimpleDataTableWritable(headings, null, types);

		incArea.setName("incremental area");

		DataTableUtils.fill(incArea, fileReader, false, "\t", true);
		fileReader.close();

		//Normally the db version has the identifier as the row ID and the
		//model_reach (the db key) as column 0.  Here we are duplicating
		//the identifier (mrb_id) into both.
		copyColumnAsRowId(incArea, 0);

		return incArea.toImmutable();
	}

	public ModelReachAreaRelations loadModelReachAreaRelations(String subpackage, String fileName, TopoData topo) throws Exception {

		String basePackage = this.getClass().getPackage().getName();
		InputStream fileInputStream = getResource(basePackage + "." + subpackage, fileName);

		BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileInputStream));
		String[] headings = {"IDENTIFIER ID", "state_id", "fraction_in_state"};
		Class<?>[] types = {Long.class, Long.class, Double.class};
		DataTableWritable dtw = new SimpleDataTableWritable(headings, null, types);
		dtw.setName("topo");
		DataTableUtils.fill(dtw, fileReader, false, "\t", true);
		fileReader.close();

		LoadModelReachAreaRelations action = new LoadModelReachAreaRelations(topo, dtw);


		return action.run();
	}

	public DataTable loadRegionDetail(String subpackage, String fileName) throws IOException {

		String basePackage = this.getClass().getPackage().getName();
		InputStream fileInputStream = getResource(basePackage + "." + subpackage, fileName);
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileInputStream));

		String[] headings = {
				"State_Name",
				"FIPS_Code",
				"Country_Code",
				"Postal_Code"
				};

		Class<?>[] types= {
				String.class, String.class, String.class, String.class
				};
		DataTableWritable areaDetail = new SimpleDataTableWritable(headings, null, types);

		areaDetail.setName("Region Detail");

		DataTableUtils.fill(areaDetail, fileReader, true, "\t", true);
		fileReader.close();

		return areaDetail.toImmutable();
	}


	/**
	 * Opens the specified file as an input stream.
	 * @param packageName
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static InputStream getResource(String packageName, String fileName) throws IOException {
		Properties props = new Properties();

		String basePath = packageName.replace('.', '/');
		String fullPath = basePath + "/" + fileName;

		InputStream is = Thread.currentThread().getContextClassLoader().
				getResourceAsStream(fullPath);

		return is;
	}

}
