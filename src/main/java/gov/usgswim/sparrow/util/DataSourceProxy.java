package gov.usgswim.sparrow.util;

import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Wrapper class to provide a way to supply connections either via JNDI or
 * via an application defined connection.  This class or a real JNDI DataSource
 * can be passed to a client.
 * 
 * This class will then call the JDBCConnectable class when/if the actual
 * connection is needed.  It is assumed that the JDBCConnectable knows how to
 * make the actual connection, so none of the other info is passed.
 * 
 * @deprecated
 * TODO delete this class
 */
public class DataSourceProxy implements DataSource {
	JDBCConnectable src;
	PrintWriter pw;
	
	public DataSourceProxy(JDBCConnectable connectable) {
		src = connectable;
	}

	/**
	 * Returns a connection from the JDBCConnectable instance.
	 * This method is synchronized to protect the underlying JDBCConnectable
	 * class, which may not expect multiple calls while creating a connection.
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		return src.getConnection();
	}

	/**
	 * Ignores params and simply returns a connection.
	 * This method is synchronized to protect the underlying JDBCConnectable
	 * class, which may not expect multiple calls while creating a connection.
	 * @param username
	 * @param password
	 * @return
	 * @throws SQLException
	 */
	public synchronized Connection getConnection(String username, String password)
			throws SQLException {
			
		return src.getConnection();
	}

	public synchronized PrintWriter getLogWriter() throws SQLException {
		if (pw == null) {
			pw = new PrintWriter( System.out );
		}
		return pw;
	}

	public void setLogWriter(PrintWriter printWriter) throws SQLException {
		this.pw = printWriter;
	}

	public void setLoginTimeout(int i) throws SQLException {
	}

	public int getLoginTimeout() throws SQLException {
		return 0;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		// TODO [IK] Not sure how this should behave as I'm not sure about the intent of the class. Must discuss with Eric
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		// TODO [IK] Not sure how this should behave as I'm not sure about the intent of the class. Must discuss with Eric
		return null;
	}
}
