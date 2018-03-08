package uk.ac.ox.cs.pdq.databasemanagement.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.ox.cs.pdq.databasemanagement.ExternalDatabaseManager;
import uk.ac.ox.cs.pdq.databasemanagement.exception.DatabaseException;

/**
 * All external connections are registered here. By calling shutdownAll it is
 * possible to clear any manager that somehow gets stuck, and allows unit tests
 * and similar long running applications to clear everything before stating the
 * next test.
 * 
 * @author gabor
 *
 */
public class DatabaseMonitor {
	private static List<DatabaseStats> stats = new ArrayList<>();
	private static final Object LOCK = new Object();
	
	/** Register this dbm to ensure it will be clered and closed later (in case test falure and such)
	 * @param dbm
	 */
	public static void register(ExternalDatabaseManager dbm) {
		synchronized (LOCK) {
			stats.add(new DatabaseStats(dbm));
		}
	}
	/** UnRegister this dbm, it was shut down already
	 * @param dbm
	 */
	public static void unRegister(ExternalDatabaseManager dbm) {
		DatabaseStats toDelete = null;
		for (DatabaseStats s:stats) {
			if (s.dbm.equals(dbm)) {
				toDelete = s;
				break;
			}
		}
		if (toDelete==null)
			return;
		synchronized (LOCK) {
			stats.remove(toDelete);
		}
	}
	/** Shuts down all existing database manager.
	 * @throws DatabaseException
	 */
	public static void forceStopAll() throws DatabaseException {
		synchronized (LOCK) {
			for (DatabaseStats s:stats) {
				System.out.println("DatabaseMonitor shuts down: " + s.toString());
				s.dbm.shutdown();
				System.out.println("shutdown of: " + s.toString() + " worked.");
			}
			stats.clear();
		}
	}
	public static String printStats() {
		StringBuffer sb = new StringBuffer();
		int counter = 1;
		for (DatabaseStats s:stats) {
			sb.append(counter);
			sb.append(".");
			sb.append(" ");
			sb.append(s.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
	protected static class DatabaseStats {
		private String databaseName;
		private ExternalDatabaseManager dbm;
		private Exception creatorThread;
		private Date createdAt;
		public DatabaseStats(ExternalDatabaseManager dbm) {
			this.dbm = dbm;
			creatorThread = new Exception();
			creatorThread.fillInStackTrace();
			createdAt = new Date(System.currentTimeMillis());
			databaseName = dbm.getDatabaseName() + "_" + dbm.getDatabaseInstanceID();
		}
		public String toString() {
			return databaseName + ": " +  createdAt + ", created by "  + creatorThread.getStackTrace()[3];
		}
	}
}
