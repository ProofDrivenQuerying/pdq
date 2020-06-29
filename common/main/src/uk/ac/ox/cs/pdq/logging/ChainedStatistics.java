// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.logging;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Statistics logger that works by appending logs from a sequence of delegate
 * loggers.
 *
 * @author Julien Leblay
 */
public class ChainedStatistics extends StatisticsLogger implements ProgressLogger {

	/** The filter separator. */
	public static Character FILTER_SEPARATOR = ':';

	protected PrintStream out;

	/** The print header. */
	protected boolean printHeader = true;

	/**  Map of static key to insert in the log line for grepping purpose. */
	private Map<String, Object> filters = new LinkedHashMap<>();

	/**  Map of static key to append in the log line for grepping purpose. */
	private Map<String, Object> suffixes = new LinkedHashMap<>();

	/**  Date formatter. */
	private DateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss.S");

	/** The list of delegated loggers. */
	private List<StatisticsLogger> stats = new LinkedList<>();

	/**
	 * Default constructor.
	 */
	public ChainedStatistics() {
		this.out = System.out;
	}

	/**
	 * Default constructor.
	 *
	 * @param out the stream where to print the logs
	 */
	public ChainedStatistics(PrintStream out) {
		this.out = out;
	}

	/**
	 * Adds a delegate logger to the chain.
	 *
	 * @param s the s
	 */
	public void addStatistics(StatisticsLogger s) {
		this.stats.add(s);
	}

	/**
	 * Adds a filter to the chain.
	 *
	 * @param s the s
	 * @param o the o
	 */
	public void addFilter(String s, Object o) {
		this.filters.put(s, o);
		this.printHeader = true;
	}

	/**
	 * Adds a suffix to the chain.
	 *
	 * @param s the s
	 * @param o the o
	 */
	public void addSuffix(String s, Object o) {
		this.suffixes.put(s, o);
		this.printHeader = true;
	}

	@Override
	public void log() {
		if (this.printHeader) {
			this.out.println("# " + this.makeHeader());
			this.printHeader = false;
		}
		this.out.println(this.makeLine());
		this.out.flush();
	}

	@Override
	public void log(String suffix) {
		if (this.printHeader) {
			this.out.println("# " + this.makeHeader());
			this.printHeader = false;
		}
		this.out.print(this.makeLine());
		this.out.println(suffix);
		this.out.flush();
	}

	@Override
	protected String makeLine() {
		StringBuilder result = new StringBuilder();
		result.append(this.df.format(System.currentTimeMillis())).append(FIELD_SEPARATOR);
		for (String k : this.filters.keySet()) {
			result.append(k).append(FILTER_SEPARATOR);
			result.append(this.filters.get(k)).append(FIELD_SEPARATOR);
		}
		for (StatisticsLogger s : this.stats) {
			result.append(s.makeLine());
		}
		for (String k : this.suffixes.keySet()) {
			result.append(k).append(FILTER_SEPARATOR);
			result.append(this.suffixes.get(k)).append(FIELD_SEPARATOR);
		}
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.logging.performance.StatisticsLogger#makeHeader()
	 */
	@Override
	protected String makeHeader() {
		StringBuilder result = new StringBuilder();
		result.append("TIMESTAMP").append(FIELD_SEPARATOR);
		for (String k : this.filters.keySet()) {
			result.append(k).append(FIELD_SEPARATOR);
		}
		for (StatisticsLogger s : this.stats) {
			result.append(s.makeHeader());
		}
		for (String k : this.suffixes.keySet()) {
			result.append(k).append(FIELD_SEPARATOR);
		}
		return result.toString();
	}

	@Override
	public void close() {
		this.printHeader = true;
		this.out.println(this.makeLine());
		if (this.printHeader) {
			this.out.println("# " + this.makeHeader());
			this.printHeader = false;
		}
		this.out.flush();
		if (this.out != System.out) {
			this.out.close();
		}
	}
}
