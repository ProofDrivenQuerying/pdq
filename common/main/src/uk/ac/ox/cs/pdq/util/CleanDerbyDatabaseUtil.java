package uk.ac.ox.cs.pdq.util;

/*
 *
 * Derby - Class org.apache.derbyTesting.functionTests.util.CleanDatabase
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import uk.ac.ox.cs.pdq.db.Schema;

/**
 * Test decorator that cleans a database on setUp and
 * tearDown to provide a test with a consistent empty
 * database as a starting point. The obejcts are cleaned
 * (dropped) on tearDown to ensure any filures dropping
 * the objects can easily be associated with the test
 * fixtures that created them.
 * <P>
 * Tests can extend to provide a decorator that defines
 * some schema items and then have CleanDatabaseTestSetup
 * automatically clean them up by implementing the decorateSQL method.. 
 * As an example:
 * <code>
       return new CleanDatabaseTestSetup(suite) {
           protected void decorateSQL(Statement s) throws SQLException {

               s.execute("CREATE TABLE T (I INT)");
               s.execute("CREATE INDEX TI ON T(I)")

           }
       };
 * </code>
 * 
 */
public class CleanDerbyDatabaseUtil{

	//   /**
	//    * Constructor to use when running in a client / server 
	//    * with the server already started on a given host
	//    * and port.
	//    */
	//   /*
	//    * Currently only used in o.a.dT.ft.tests.replicationTests.StandardTests
	//    * for running existing JUnit tests on a client server configuration.
	//    * To avoid duplicating the code inside decorateSQL() methods
	//    * public static decorate() methods have been factored out
	//    * for reuse in test methods in StandardTests: e.g. as AnsiTrimTest.decorate(s);
	//    */
	//   public CleanDerbyDatabaseUtil(Test test, 
	//           boolean useNetworkClient,
	//           String hostName,
	//           int portNo) {
	//	   
	//       if ( useNetworkClient )
	//       {
	//           this.jdbcClient = JDBCClient.DERBYNETCLIENT;
	//       }
	//       this.hostName = hostName;
	//       this.portNo = portNo;
	//   }
	//   private JDBCClient jdbcClient = null;
	//   private String hostName = null;
	//   private int portNo = -1;



	/**
	 * Clean the default database using the default connection.
	 */
	protected static void destroy(Connection conn) throws Exception {
		// See DERBY-5686 - perhaps there's a test that leaves a 
		// connection in read-only state - let's check here and 
		// if there's a conn that's read-only, unset it, and make
		// the test fail so we find it.
		conn.setAutoCommit(false);
		boolean ok=true;
		if (conn.isReadOnly())
		{
			conn.setReadOnly(false);
			ok=false;
		}

		// Clean the database, ensures that any failure dropping
		// objects can easily be linked to test fixtures that
		// created them
		//
		// No need to compress, any test requiring such a clean
		// setup should not assume it is following another test
		// with this decorator, it should wrap itself in a CleanDatabaseTestSetup.
		// Compress is a somewhat expensive operation so avoid it if possible.
		CleanDerbyDatabaseUtil.cleanDatabase(conn);        
	}

	/**
	 * Clean a complete database
	 * @param conn Connection to be used, must not be in auto-commit mode.
	 * @param compress True if selected system tables are to be compressed
	 * to avoid potential ordering differences in test output.
	 * @throws SQLException database error
	 */
	public static void cleanDatabase(Connection conn) throws SQLException {
		clearProperties(conn);
		removeObjects(conn);
		//        if (compress)
		//            compressObjects(conn);
		removeRoles(conn);
		removeUsers( conn );
	}

	/**
	 * Set of database properties that will be set to NULL (unset)
	 * as part of cleaning a database.
	 */
	private static final String[] CLEAR_DB_PROPERTIES =
		{
				"derby.database.classpath",
		};

	/**
	 * Clear all database properties.
	 */
	private static void clearProperties(Connection conn) throws SQLException {

		PreparedStatement ps = conn.prepareCall(
				"CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(?, NULL)");

		for (int i = 0; i < CLEAR_DB_PROPERTIES.length; i++)
		{
			ps.setString(1, CLEAR_DB_PROPERTIES[i]);
			ps.executeUpdate();
		}
		ps.close();
		conn.commit();
	}


	/**
	 * Remove all objects in all schemas from the database.
	 */
	private static void removeObjects(Connection conn) throws SQLException {

		DatabaseMetaData dmd = conn.getMetaData();

		SQLException sqle = null;
		// Loop a number of arbitary times to catch cases
		// where objects are dependent on objects in
		// different schemas.
		for (int count = 0; count < 5; count++) {
			// Fetch all the user schemas into a list
			List<String> schemas = new ArrayList<String>();
			ResultSet rs = dmd.getSchemas();
			while (rs.next()) {

				String schema = rs.getString("TABLE_SCHEM");
				if (schema.startsWith("SYS"))
					continue;
				if (schema.equals("SQLJ"))
					continue;
				if (schema.equals("NULLID"))
					continue;

				schemas.add(schema);
			}
			rs.close();

			// DROP all the user schemas.
			sqle = null;
			for (String schema : schemas) {
				try {
					Statement dropStm = conn.createStatement();
					dropStm.executeQuery("drop schema "+schema+" restrict");
					// JDBC.dropSchema(dmd, schema);
				} catch (SQLException e) {
					sqle = e;
					if(e.getMessage().contains("cannot be dropped"))
						sqle=null;
				}
			}
			// No errors means all the schemas we wanted to
			// drop were dropped, so nothing more to do.
			if (sqle == null)
				return;
		}
		throw sqle;
	}

	private static void removeRoles(Connection conn) throws SQLException {
		// No metadata for roles, so do a query against SYSROLES
		Statement stm = conn.createStatement();
		Statement dropStm = conn.createStatement();

		// cast to overcome territory differences in some cases:
		ResultSet rs = stm.executeQuery(
				"select roleid from sys.sysroles where " +
				"cast(isdef as char(1)) = 'Y'");

		while (rs.next()) {
			dropStm.executeUpdate("DROP ROLE " + rs.getString(1));
		}

		stm.close();
		dropStm.close();
		conn.commit();
	}

	/** Drop all credentials stored in SYSUSERS */
	private static void removeUsers(Connection conn) throws SQLException
	{
		// Get the users
		Statement stm = conn.createStatement();
		ResultSet rs = stm.executeQuery( "select username from sys.sysusers" );
		ArrayList<String> users = new ArrayList<String>();

		while ( rs.next() ) { users.add( rs.getString( 1 ) ); }
		rs.close();
		stm.close();

		// Now delete them
		PreparedStatement   ps = conn.prepareStatement( "call syscs_util.syscs_drop_user( ? )" );

		for ( int i = 0; i < users.size(); i++ )
		{
			ps.setString( 1, (String) users.get( i ) );

			// you can't drop the DBO's credentials. sorry.
			try {
				ps.executeUpdate();
			}
			catch (SQLException se)
			{
				if ( "4251F".equals( se.getSQLState() ) ) { continue; }
				else { throw se; }
			}
		}

		ps.close();
		conn.commit();
	}

	/**
	 * Set of objects that will be compressed as part of cleaning a database.
	 */
	private static final String[] COMPRESS_DB_OBJECTS =
		{
				"SYS.SYSDEPENDS",
		};

	//    /**
	//     * Compress the objects in the database.
	//     * 
	//     * @param conn the db connection
	//     * @throws SQLException database error
	//     */
	//    private static void compressObjects(Connection conn) throws SQLException {
	//   	 
	//   	 CallableStatement cs = conn.prepareCall
	//   	     ("CALL SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE(?, ?, 1, 1, 1)");
	//   	 
	//   	 for (int i = 0; i < COMPRESS_DB_OBJECTS.length; i++)
	//   	 {
	//   		 int delim = COMPRESS_DB_OBJECTS[i].indexOf(".");
	//            cs.setString(1, COMPRESS_DB_OBJECTS[i].substring(0, delim) );
	//            cs.setString(2, COMPRESS_DB_OBJECTS[i].substring(delim+1) );
	//            cs.execute();
	//   	 }
	//   	 
	//   	 cs.close();
	//   	 conn.commit();
	//    }

}