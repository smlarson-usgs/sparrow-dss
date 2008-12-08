package gov.usgswim.sparrow.test;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.FilteredDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.datatable.DataTableCompareOld;
import gov.usgswim.sparrow.domain.Model;
import gov.usgswim.sparrow.domain.ModelBuilder;
import gov.usgswim.sparrow.domain.Source;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import junit.framework.TestCase;
import oracle.jdbc.OracleDriver;


/**
 * @author eeverman
 * @deprecated No longer tests the JDBCUtil class. Superseded by DataLoaderTest class. Should delete
 */
public class JDBCUtil_Test extends TestCase {
	private Connection conn;

	public JDBCUtil_Test(String sTestName) {
		super(sTestName);
	}

	public static void main(String args[]) {
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();


		String username = "SPARROW_DSS";
		String password = "***REMOVED***";
		String thinConn = "jdbc:oracle:thin:@130.11.165.152:1521:widw";
		DriverManager.registerDriver(new OracleDriver());
		conn = DriverManager.getConnection(thinConn,username,password);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		conn.close();
		conn = null;
	}


	public void testReadModelMetadata() throws Exception {
		List<ModelBuilder> models = DataLoader.loadModelMetaData(conn);

		Model m = models.get(0);
		Source s1 = m.getSource(1);	//get by identifier
		Source s2 = m.getSource(11);	//get by identifier

		//test that we get the first and last sources
		assertEquals(s1, m.getSources().get(0));	//get via list index
		assertEquals(s2, m.getSources().get(10));	//get via list index


		//model
		assertEquals(1, m.getId().intValue());
		assertEquals("Chesapeake bay", m.getName());
		assertEquals(11, m.getSources().size());

		//1st source
		assertEquals(1, s1.getId().intValue());
		assertEquals("pttn", s1.getName());
		assertEquals(1, s1.getSortOrder());
		assertEquals(1, s1.getModelId().intValue());
		assertEquals(1, s1.getIdentifier());

		//last source
		assertEquals(11, s2.getId().intValue());
		assertEquals("frst", s2.getName());
		assertEquals(11, s2.getSortOrder());
		assertEquals(1, s2.getModelId().intValue());
		assertEquals(11, s2.getIdentifier());

	}

	/**
	 * @see JDBCUtil#JDBCUtil.writePredictDataSet(PredictionDataSet data, Connection conn)
	 * 
	 * Note:  This method does not test this method directly, instead it assumes 
	 * that model 21 was loaded and compare the db values for model 21
	 * to the text files at classpath:  data.ch2007_04_24/
	 * 
	 * Not exactly fullproof.  A better test would be to have a subset.
	 */
	public void xtestDBWriteVsTextFilesDataSet() throws Exception {
		String rootDir = "/data/ch2007_04_24/";
		PredictDataBuilder textDs = new PredictDataBuilder();
		PredictData dbDs = null;


		//Load the text files
		textDs.setAncil( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "ancil.txt"), true, 0) );

		//1st 4 columns are iteration, inc delv, tot_del, and boot error (skip)
		DataTable baseTextCoef = TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "coef.txt"), true, -1);
		//int firstRowBeyondZeroIteration = baseTextCoef.findFirst(0, 1d);

		//For speed, you can choose which section to run below:
		/*
			//Uncomment this section to test only iteration 0.
			textDs.setCoef(
				new Data2DView(baseTextCoef, 0, firstRowBeyondZeroIteration, 4, 11)
			);
		 */

		//Uncomment this section to test all iterations.
		textDs.setCoef(
				new FilteredDataTable(baseTextCoef, 4, 11)
		);


		textDs.setSrc( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "src.txt"), true, -1) );
		textDs.setTopo( TabDelimFileUtil.readAsDouble(getClass().getResourceAsStream(rootDir + "topo.txt"), true, -1) );


		//Load the db version of the same model
		dbDs = DataLoader.loadFullModelDataSet(conn, 21);

		DataTableCompareOld comp = new DataTableCompareOld(textDs.getSrc(), dbDs.getSrc());
		DataTableCompareOld topo = new DataTableCompareOld(textDs.getTopo(), dbDs.getTopo());
		DataTableCompareOld coef = new DataTableCompareOld(textDs.getCoef(), dbDs.getCoef());


		for (int i = 0; i < comp.getColumnCount(); i++)  {
			System.out.println("comp col " + i + " error: " + comp.findMaxCompareValue(i));
		}

		for (int i = 0; i < topo.getColumnCount(); i++)  {
			System.out.println("topo col " + i + " error: " + topo.findMaxCompareValue(i));
		}

		for (int i = 0; i < coef.getColumnCount(); i++)  {
			System.out.println("coef col " + i + " error: " + coef.findMaxCompareValue(i));
			int maxRow = coef.findMaxCompareRow(i);
			System.out.println("--Row Data at max (row #" +  maxRow + ")");
			for (int j=0; j<coef.getColumnCount(); j++) {
				System.out.println("----Col " + j + ": " + coef.getValue(maxRow, j));
			}
		}

		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);
		assertEquals(0d, topo.findMaxCompareValue(), 0.004d);
		assertEquals(0d, coef.findMaxCompareValue(), 0.004d);

	}


	protected DataTableCompareOld buildTopoComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/topo.txt");
		DataTable data = TabDelimFileUtil.readAsInteger(fileStream, true, -1);

		DataTableCompareOld comp = new DataTableCompareOld(data, toBeCompared);

		return comp;
	}

	protected DataTableCompareOld buildSourceReachCoefComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.findFirst(0, Double.valueOf(1));
		DataTable view = new FilteredDataTable(data, 0, firstNonZeroRow, 4, 11);	//Crop to only iteration 0 and remove non-coef columns
		// System.out.println("DEBUG: " + view.getRowCount() + " - " + toBeCompared.getRowCount());
		DataTableCompareOld comp = new DataTableCompareOld(view, toBeCompared);

		return comp;
	}

	protected DataTableCompareOld buildDecayComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/coef.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int firstNonZeroRow = data.findFirst(0, Double.valueOf(1d));
		DataTable view = new FilteredDataTable(data, 0, firstNonZeroRow, 1, 2);	//Crop to only iteration 0 and only the two decay columns

		DataTableCompareOld comp = new DataTableCompareOld(view, toBeCompared);

		return comp;
	}

	protected DataTableCompareOld buildSourceValueComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/src.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);

		DataTableCompareOld comp = new DataTableCompareOld(data, toBeCompared);

		return comp;
	}

	protected DataTableCompareOld buildPredictionComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};

		DataTableCompareOld comp = new DataTableCompareOld(toBeCompared, data, DEFAULT_COMP_COLUMN_MAP);

		return comp;
	}
}

