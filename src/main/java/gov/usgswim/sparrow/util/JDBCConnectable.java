package gov.usgswim.sparrow.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple interface to mark a class that has a getConnection() method to
 * allow a call-back to get a JDBC connection.
 */
public interface JDBCConnectable {

	public Connection getConnection() throws SQLException;
}
