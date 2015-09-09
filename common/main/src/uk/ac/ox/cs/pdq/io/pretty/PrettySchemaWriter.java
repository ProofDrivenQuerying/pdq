package uk.ac.ox.cs.pdq.io.pretty;

import java.io.PrintStream;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.Writer;

/**
 * Writes a concise representation of a schema to the given output
 * 
 * @author Julien Leblay
 *
 */
public class PrettySchemaWriter extends PrettyWriter<Schema> implements Writer<Schema> {

	/**
	 * The default out to which queries should be written, if not 
	 * explicitly provided at write time.
	 */
	private PrintStream out;

	/**
	 * 
	 * @param out the default output
	 */
	PrettySchemaWriter(PrintStream out) {
		this.out = out;
	}

	/**
	 * 
	 */
	public PrettySchemaWriter() {
		this(System.out);
	}
		
	/**
	 * Fluent pretty writer provider.
	 * @param out
	 * @return a new PrettyQueryWriter with the given default output.
	 */
	public static PrettySchemaWriter to(PrintStream out) {
		return new PrettySchemaWriter(out);
	}
	
	/**
	 * @param out PrintStream
	 * @param s Schema
	 */
	@Override
	public void write(PrintStream out, Schema s) {
		out.print(String.valueOf(s));
	}
	
	/**
	 * @param q Schema
	 */
	@Override
	public void write(Schema q) {
		this.write(this.out, q);
	}
}
