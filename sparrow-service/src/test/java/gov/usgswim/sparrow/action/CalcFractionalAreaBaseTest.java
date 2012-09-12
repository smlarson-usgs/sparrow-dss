package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.SparrowTestBase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author eeverman
 */
public abstract class CalcFractionalAreaBaseTest extends SparrowTestBase {

	protected static DataTable testTopo;
	protected static DataTable testTopoCorrected;
	protected static DataTableWritable incrementalAreaTable;
	
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
		
		//A 'no error' topo file.
		testTopo = loadTopo("topo");
		
		//A topo files with 'errors' that we can detect and correct.
		//The result should be the same, even with the frac errors.
		//Reach 10:		The frac is .7 instead of 1 as it should be.
		//Reaches 11 & 12:  Insead of fracs of .9 and .1 respectively, the fracs are
		//		instead .09 and .01.
		testTopoCorrected = loadTopo("topo_corrected");
		incrementalAreaTable = loadIncrementalAreas();
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
	public DataTableWritable loadTopo(String fileNameSuffix) throws IOException {
		InputStream fileInputStream = getResource(CalcFractionalAreaBaseTest.class, "topo", "txt");
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileInputStream));
		String[] headings = {"MODEL_REACH", "FNODE", "TNODE", "IFTRAN", "HYDSEQ", "SHORE_REACH", "FRAC"};
		Class<?>[] types = {Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Double.class};
		DataTableWritable topo = new SimpleDataTableWritable(headings, null, types);
		topo.setName("topo");
		DataTableUtils.fill(topo, fileReader, false, "\t", true);
		fileReader.close();
		//repopulate hydseq to be 1 and up, w/o gaps
		for (int r = 0; r < topo.getRowCount(); r++) {
			topo.setValue(new Integer(r + 1), r, 4);
		}
		//Normally the db version has the identifier as the row ID and the
		//model_reach (the db key) as column 0.  Here we are duplicating
		//the identifier (mrb_id) into both.
		copyColumnAsRowId(topo, 0);
		return topo;
	}
	
	public DataTableWritable loadIncrementalAreas() throws IOException {
		InputStream fileInputStream = getResource(CalcFractionalAreaBaseTest.class, "incarea", "txt");
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
		
		return incArea;
	}

}
