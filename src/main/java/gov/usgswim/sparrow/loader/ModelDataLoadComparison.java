package gov.usgswim.sparrow.loader;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.sparrow.action.LoadModelPredictData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;

public class ModelDataLoadComparison {

	public static void compare() {

	}



	public static void outputTopoAsTextFile(File baseOutputDirectory, Connection conn, Long modelID) throws SQLException, IOException {
		//int modelIdInt = modelID.intValue();

		{// write topo
			DataTable topo = LoadModelPredictData.loadTopo(conn, modelID);
			if (baseOutputDirectory.exists() && baseOutputDirectory.isDirectory()) {
				File topoOutFile = ModelDataLoadComparison.getOutputTopoFile(baseOutputDirectory);
				Writer writer = new BufferedWriter(new FileWriter(topoOutFile), 8192);
				// writer.write("fnode	tnode	iftran	hydseq\n");

				DataTablePrinter.printDataTable(topo, null, writer);
				writer.flush();
				writer.close();
			}
		}
	}

	public static void outputSourceValuesAsTextFile(File baseOutputDirectory, Connection conn, Long modelID) throws SQLException, IOException {
		//int modelIdInt = modelID.intValue();

		{// write src
			DataTableWritable sourceMetadata = LoadModelPredictData.loadSourceMetadata(conn, modelID);
			DataTable source = LoadModelPredictData.loadSourceValues(conn, modelID, sourceMetadata);
			if (baseOutputDirectory.exists() && baseOutputDirectory.isDirectory()) {
				File srcOutFile = ModelDataLoadComparison.getOutputSrcFile(baseOutputDirectory);
				Writer writer = new BufferedWriter(new FileWriter(srcOutFile), 8192);
				DataTablePrinter.printDataTable(source, null, writer);

				writer.flush();
				writer.close();
			}
		}
	}

	public static void outputCoefAsTextFiles(File baseOutputDirectory, Connection conn, Long modelID) throws SQLException, IOException {
		//int modelIdInt = modelID.intValue();

		{// write coef
			DataTableWritable sourceMetadata = LoadModelPredictData.loadSourceMetadata(conn, modelID);
			DataTable coef = LoadModelPredictData.loadSourceReachCoef(conn, modelID, sourceMetadata);
			if (baseOutputDirectory.exists() && baseOutputDirectory.isDirectory()) {
				File topoOutFile = ModelDataLoadComparison.getOutputCoefFile(baseOutputDirectory);
				Writer writer = new BufferedWriter(new FileWriter(topoOutFile), 8192);
				for (int col=0; col<coef.getColumnCount(); col++) {
					writer.write(coef.getName(col));
					writer.write("\t");
				}
				writer.write("\n");
				DataTablePrinter.printDataTable(coef, null, writer);

				writer.flush();
				writer.close();
			}
		}

	}

	// ================
	// UTILITY METHODS
	// =================
	public static File getOutputCoefFile(File baseOutputDirectory) {
		return new File(baseOutputDirectory.getAbsolutePath() + "/" + "coef.txt");
	}

	public static File getOutputDecayFile(File baseOutputDirectory) {
		return new File(baseOutputDirectory.getAbsolutePath() + "/" + "decay.txt");
	}

	public static File getOutputSrcFile(File baseOutputDirectory) {
		return new File(baseOutputDirectory.getAbsolutePath() + "/" + "src.txt");
	}

	public static File getOutputTopoFile(File baseOutputDirectory) {
		return new File(baseOutputDirectory.getAbsolutePath() + "/" + "topo.txt");
	}

}
