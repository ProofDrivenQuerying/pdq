// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.eventbus.EventBus;

/**
 * Simple statistics collector, It can measure how long certain functions take.
 * 
 * @author Gabor
 *
 */
public class SimpleStatisticsCollector extends StatisticsCollector {
	private FileWriterThread fwt = null;
	private Map<String, List<StatisticsRecord>> cache = new LinkedHashMap<>();
	private final Object LOCK = new Object();
	private File output;

	public SimpleStatisticsCollector() {
		super(false, new EventBus());
	}

	public SimpleStatisticsCollector(File output) {
		super(false, new EventBus());
		this.output = output;
		if (this.output != null) {
			fwt = new FileWriterThread();
			fwt.start();
		}
	}

	public StatisticsRecord addNewRecord(String taskName) {
		long startTime = System.currentTimeMillis();
		StatisticsRecord r = new StatisticsRecord(taskName, startTime);
		synchronized (LOCK) {
			List<StatisticsRecord> list = cache.get(taskName);
			if (list == null) {
				list = new ArrayList<>();
				list.add(r);
				cache.put(taskName, list);
			} else {
				list.add(r);
			}
		}
		return r;
	}

	@SuppressWarnings("unchecked")
	public List<StatisticsRecord> getRecordsForTask(String taskName) {
		synchronized (LOCK) {
			return (List<StatisticsRecord>) ((ArrayList<StatisticsRecord>) cache.get(taskName)).clone();
		}
	}

	public List<StatisticsRecord> getUnfinishedRecords() {
		List<StatisticsRecord> ret = new ArrayList<>();
		synchronized (LOCK) {
			for (List<StatisticsRecord> list : cache.values()) {
				for (StatisticsRecord r : list) {
					if (!r.isFinished())
						ret.add(r);
				}
			}
		}
		return ret;
	}

	public void printStatsToFile(File target) throws IOException {
		if (!target.getParentFile().exists())
			target.getParentFile().mkdirs();
		PrintWriter pw = null;
		try {
			FileWriter fw = new FileWriter(target, false);
			pw = new PrintWriter(fw);
			synchronized (LOCK) {
				for (String key : cache.keySet()) {
					List<StatisticsRecord> list = cache.get(key);
					int numberOfCalls = 0;
					double sumDuration = 0;
					for (StatisticsRecord r : list) {
						numberOfCalls++;
						sumDuration += r.getDurationSeconds();
						pw.println(r.toString());
						if (!r.isFinished()) {
							Exception e = new Exception(r.toString() + " not finished.");
							e.setStackTrace(r.trace);
							e.printStackTrace(pw);
						}
					}
					pw.println("=============================\n " + numberOfCalls + " calls to " +  key + "  summary duration: " + sumDuration + " seconds.\n");
					
				}
				cache.clear();
			}
		} finally {
			if (pw != null)
				pw.close();
		}
	}

	public class StatisticsRecord {
		private String taskName;
		private long startTime;
		private long endTime=0;
		private StackTraceElement[] trace;

		public StatisticsRecord(String taskName, long startTime) {
			this.taskName = taskName;
			this.startTime = startTime;
			Exception trace = new Exception();
			trace.fillInStackTrace();
			this.trace = trace.getStackTrace();
		}

		public String getTaskName() {
			return taskName;
		}

		protected long getStartTime() {
			return startTime;
		}

		protected void setStartTime(long startTime) {
			this.startTime = startTime;
		}

		protected long getEndTime() {
			return endTime;
		}

		public void setEndTimeInMilliseconds(long endTime) {
			this.endTime = endTime;
		}

		public void setEndTime() {
			this.endTime = System.currentTimeMillis();
		}

		public String toString() {
			return taskName + " " + new Date(startTime) + " took " + getDurationSeconds() + " seconds.";
		}

		protected double getDurationSeconds() {
			return (endTime - startTime) / 1000.0;
		}

		public StackTraceElement[] getTrace() {
			return trace;
		}
		
		public boolean isFinished() {
			return endTime!=0;
		}
	}

	private class FileWriterThread extends Thread {
		public FileWriterThread() {
			super("FileWriterThread");
		}

		public void run() {

		}
	}
}
