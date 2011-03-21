package gov.usgswim.datatable.utils;

import gov.usgswim.datatable.ColumnDataWritable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.AcceptedStandardColumnTypes;
import gov.usgswim.datatable.impl.BuilderHelper;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A class with static methods to convert ResultSets, Files, and arrays to DataTable
 * @author ilinkuo
 *
 */
public abstract class DataTableConverter {

	/**
	 * Converts the ResultSet to a datatable, using the specified array of
	 * column types. The returned rset will be exactly the width of the array of
	 * specified types.
	 *
	 * @param rset
	 * @param specifiedColumnTypes
	 *            use nulls when type inference is desired.
	 * @param isFirstColumnID
	 * @return
	 * @throws SQLException
	 */
	public static DataTableWritable toDataTable(ResultSet rset, Class<?>[] specifiedColumnTypes, boolean isFirstColumnID) throws SQLException {
		Class<?>[] inferredColumnTypes = DataTableUtils.inferTypes(rset);
		int lengthAdjustment = (isFirstColumnID)? 1: 0;
		if (specifiedColumnTypes == null) specifiedColumnTypes = new Class[inferredColumnTypes.length - lengthAdjustment];

		// ------------------------------------------------
		// CONFIGURE THE DATATABLE ACCORDING TO columnTypes
		// ------------------------------------------------
		// Ignore the first column of inferredColumnTypes if it is an ID
		if (isFirstColumnID) {
			Class<?>[] dataColumns = new Class[inferredColumnTypes.length - 1];
			for (int i=1; i<inferredColumnTypes.length; i++) {
				dataColumns[i-1] = inferredColumnTypes[i];
			}
			assert(dataColumns.length == inferredColumnTypes.length - 1);
			inferredColumnTypes = dataColumns;
		}

		DataTableWritable result = new SimpleDataTableWritable();
		int offset = (isFirstColumnID)? 2: 1; // Note that sql column indexes begin with 1, not 0
		// Create all the columns.
		for (int i=0; i<specifiedColumnTypes.length; i++) {
			ColumnDataWritable column = null;
			// use the specified column type if available. Otherwise, use the inferred type.
			Class<?> columnClass = (specifiedColumnTypes[i] == null)? inferredColumnTypes[i]: specifiedColumnTypes[i];
			AcceptedStandardColumnTypes colType = AcceptedStandardColumnTypes.getColumnType(columnClass);
			colType = (colType == null)? AcceptedStandardColumnTypes.STRING: colType; // default is STRING type


			column = colType.makeNewColumn();
			String columnName = rset.getMetaData().getColumnName(i + offset);
			column.setName(columnName);
			result.addColumn(column);
		}
		// assert(columns is now completely filled with no nulls);
		return BuilderHelper.fill(result, rset, isFirstColumnID);
	}

	public static DataTableWritable toDataTable(ResultSet rset, boolean isFirstColumnID) throws SQLException {
		return toDataTable(rset, null, isFirstColumnID);
	}

	public static DataTableWritable toDataTable(ResultSet rset) throws SQLException {
		return toDataTable(rset, null, false);
	}

	// --------------------------
	// Convert Files to DataTable
	// --------------------------
	public static DataTableWritable toDataTable(File dataFile) throws IOException {
		return toDataTable(dataFile, null, false);
	}

	public static DataTableWritable toDataTable(File dataFile, Class<?>[] specifiedColumnTypes,
			boolean isFirstColumnID) throws IOException {
		DataFileDescriptor fileDescription = Analyzer.analyzeFile(dataFile);
		int lengthAdjustment = (isFirstColumnID)? 1: 0;
		if (specifiedColumnTypes == null) specifiedColumnTypes = new Class[fileDescription.getColumnCount() - lengthAdjustment];

		// ------------------------------------------------
		// CONFIGURE THE DATATABLE ACCORDING TO columnTypes
		// ------------------------------------------------
		// Ignore the first column of fileDescription if it is an ID
		Class<?>[] inferredColumnTypes = new Class[fileDescription.getColumnCount() - lengthAdjustment];
		for (int i=lengthAdjustment; i<fileDescription.getColumnCount(); i++) {
			inferredColumnTypes[i-lengthAdjustment] = fileDescription.dataTypes[i].clazz;
		}

		DataTableWritable result = new SimpleDataTableWritable();
		int offset = lengthAdjustment;
		// Create all the columns.
		for (int i=0; i<specifiedColumnTypes.length; i++) {
			ColumnDataWritable column = null;
			// use the specified column type if available. Otherwise, use the inferred type.
			Class<?> columnClass = (specifiedColumnTypes[i] == null)? inferredColumnTypes[i]: specifiedColumnTypes[i];
			AcceptedStandardColumnTypes colType = AcceptedStandardColumnTypes.getColumnType(columnClass);
			colType = (colType == null)? AcceptedStandardColumnTypes.STRING: colType; // default is STRING type

			column = colType.makeNewColumn();
			String columnName = "field_" + i;
			if (fileDescription.hasColumnHeaders()) {
				columnName = fileDescription.getHeaders()[i + offset];
			}

			column.setName(columnName);
			result.addColumn(column);
		}
		// assert(columns is now completely filled with no nulls);
		return DataTableUtils.fill(result, dataFile, isFirstColumnID, fileDescription.delimiter, fileDescription.hasColumnHeaders());
	}

	// ================================
	// Convert java arrays to DataTable
	// ================================

	/**
	 * Converts a 2 dimensional float array to a DataTable
	 * @param float2DArray
	 * @return
	 */
	public static DataTableWritable toDataTable(float[][] float2DArray) {
		DataTableWritable result = new SimpleDataTableWritable();
		BuilderHelper.fill(result, float2DArray, null );
		return result;
	}

	/**
	 * Converts a series of float values to a single column DataTable
	 * @param floatValues
	 * @return
	 */
	public static DataTableWritable toSingleColumnDataTable(float... floatValues) {
		float[][] float2DArray = new float[floatValues.length][1];
		for (int i=0; i<floatValues.length; i++) {
			float2DArray[i][0] = floatValues[i];
		}
		return toDataTable(float2DArray);
	}

}
