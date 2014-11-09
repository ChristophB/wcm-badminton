package backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * this class connects to a mysql database for given user and password and
 * allows operations on it. mysql-connector-java-5.1.18.jar needs to be added to
 * the classpath in the project.
 * 
 * @author Marcel
 * 
 */
public class MySQL {

	/**
	 * the username for the login will be saved here.
	 */
	private String username;

	/**
	 * the password for the login will be saved here.
	 */
	private String password;

	/**
	 * this is the mysql driver.
	 */
	private String driver = "com.mysql.jdbc.Driver";

	/**
	 * this string holds the databasename.
	 */
	private String databaseName;

	/**
	 * this is the url, includes the dbms, port and databasename.
	 */
	private String URL;

	/**
	 * this is an instance of the connection-class.
	 */
	private Connection connection;

	/**
	 * constructor, calls the method, which connects to MySQL-database with
	 * default-values from the user.
	 * 
	 * @param username
	 * @param password
	 * @param databaseName
	 */
	public MySQL(String username, String password, String databaseName) {
		this.username = username;
		this.password = password;
		this.databaseName = databaseName;
		this.URL = "jdbc:mysql://localhost:3306/" + this.databaseName
				+ "?useUnicode=true&characterEncoding=UTF-8";
		this.connect();
	}

	/**
	 * close the connection to the MySQL-database.
	 */
	public void close() {
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * register the JDBC driver and connects to the MySQL-database if connection
	 * fails, a exception will be thrown.
	 */

	public void connect() {
		try {
			Class.forName(this.driver);
			this.connection = DriverManager.getConnection(this.URL,
					this.username, this.password);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error while connecting to database for user "
					+ username + ". Please check your login data.");
		}
	}

	/**
	 * this is a test, whether the connection to the MySQL-database persists, an
	 * easy query will be executed, which returns a boolean false stand for
	 * "no connection", true for "connected".
	 * 
	 * @return isConnected
	 */

	public boolean isConnected() {
		try {
			ResultSet rs = this.returnQuery("SELECT 1;");
			if (rs == null) {
				return false;
			}
			if (rs.next()) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * execute a query, a resultset will be returned.
	 * 
	 * @param query
	 * @return ResultSet
	 */

	public ResultSet returnQuery(String query) {
		try {
			Statement stmt = this.connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			return rs;
		} catch (SQLException e) {
			System.err.println(e.toString());
			return null;
		}
	}

	/**
	 * execute a query.
	 * 
	 * @param query
	 * @return querySuccessfulExecuted
	 */

	public boolean runQuery(String query) {
		try {
			Statement stmt = this.connection.createStatement();
			boolean ok = stmt.execute(query);
			stmt.close();
			return ok;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * returns the databasename.
	 * 
	 * @return String
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * returns the connection object.
	 * 
	 * @return Connection
	 */
	public Connection getConnection() {
		return connection;
	}
}