package gov.usgswim.sparrow.debug;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.action.LoadModelPredictData;
import gov.usgswim.sparrow.datatable.PredictResultImm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;

/**
 * Run this as an Application inside of Eclipse with the same arguments as the
 * integration tests.
 *
 * @author ilinkuo
 *
 */
public class WriteDataToTSVFiles {
	static int modelID = 32;
	static String BASE_DIRECTORY = "D://eclipse-projects/sparrow_core/src/test/resources/integ";
	static String WORK_REL_PATH = "model-data";

	public static void main(String... args) throws Exception {
		File baseDirectory = new File(BASE_DIRECTORY);
		if (!baseDirectory.exists())
			throw new RuntimeException("The base directory specified ["
					+ BASE_DIRECTORY + "] does not exist!");

		PredictData predictData = new LoadModelPredictData((long) modelID).run();
		writePredictDataToFiles(predictData, modelID, baseDirectory, WORK_REL_PATH);

		PredictRunner predictRunner = new PredictRunner(predictData);
		PredictResultImm predictResult = predictRunner.doPredict();
		writeToFile(predictResult,  baseDirectory, WORK_REL_PATH, modelID + "_predict.txt", true);


		System.out.println("DONE!");
	}



	public static void writePredictDataToFiles( PredictData predictData, int id, File baseDir, String workRelPath) throws SQLException, IOException {

		DataTable dt = predictData.getSrcMetadata();
		String fileName = id + "_sourceMetadata.txt";
		writeToFile(dt,  baseDir, workRelPath, fileName, false);

		dt = predictData.getTopo();
		fileName = id + "_topo.txt";
		writeToFile(dt,  baseDir, workRelPath, fileName, true);

		dt = predictData.getSrc();
		fileName = id + "_sources.txt";
		writeToFile(dt,  baseDir, workRelPath, fileName, false);

		dt = predictData.getSrc();
		fileName = id + "_sources.txt";
		writeToFile(dt,  baseDir, workRelPath, fileName, false);

		dt = predictData.getCoef();
		fileName = id + "_coef.txt";
		writeToFile(dt,  baseDir, workRelPath, fileName, false);

		dt = predictData.getDelivery();
		fileName = id + "_delivery.txt";
		writeToFile(dt,  baseDir, workRelPath, fileName, false);
	}


	/**
	 * TODO move this method to DataTable
	 *
	 * @param dt
	 * @param writer
	 * @param writeRowID
	 * @throws IOException
	 */
	public static void DataTable2TabSeparated(DataTable dt, Writer writer, boolean writeRowID)
	throws IOException {
		int colCount = dt.getColumnCount();
		int lastCol = colCount - 1;
		boolean useID = writeRowID && dt.hasRowIds();
		{ // Output the headers
			StringBuilder sb = new StringBuilder();
			if ( useID ) sb.append("rowid").append("\t");
			for (int i = 0; i < colCount; i++) {
				sb.append(dt.getName(i));
				if (i < lastCol ) sb.append("\t");
			}
			sb.append("\n");
			writer.write(sb.toString());
		}
		{ // Output the data
			int rowCount = dt.getRowCount();
			for (int row = 0; row < rowCount; row++) {
				StringBuilder sb = new StringBuilder();
				if ( useID ) sb.append(dt.getIdForRow(row)).append("\t");
				for (int i = 0; i < colCount; i++) {
					if (i > 0)
					sb.append(dt.getValue(row, i));
					if (i < lastCol ) sb.append("\t");
				}
				sb.append("\n");
				writer.write(sb.toString());
			}
		}
	}

	private static File makeFileHandle(File baseDirectory, String relPath, String fileName) throws IOException {
		if (!baseDirectory.exists())
			throw new RuntimeException("The base directory specified ["
					+ baseDirectory.getAbsolutePath() + "] does not exist!");
		File workingDir = baseDirectory;
		if (relPath != null) {
			workingDir = new File(baseDirectory, relPath);
			if (!workingDir.exists()) {
				workingDir.mkdir();
			}
		}

		File targetFile = new File(workingDir, fileName);
		boolean isFileExists = (targetFile.exists())? true: targetFile.createNewFile();
		if (isFileExists) return targetFile;
		throw new RuntimeException("Unable to create new file " + targetFile.getAbsolutePath());
	}
	
	private static void writeToFile(DataTable dt, File baseDir, String workRelPath,
			String fileName, boolean writeRowID) throws IOException {
		File targetile = makeFileHandle(baseDir, workRelPath, fileName);
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(targetile));
			DataTable2TabSeparated(dt, writer, writeRowID);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (writer != null) writer.close();
		}
		System.out.println("Wrote to file " + targetile.getAbsolutePath());

	}



}
