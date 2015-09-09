package uk.ac.ox.cs.pdq.endpoint.format;

import java.io.PrintWriter;
import java.util.List;

import uk.ac.ox.cs.pdq.runtime.exec.iterator.TupleIterator;

/**
 * This class formats the result of a core query into an XHTML pages.
 * 
 * @author Julien LEBLAY
 */
public class HtmlTabularResultFormatter implements ResultFormatter {

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.endpoint.format.ResultFormatter#process(fr.inria.oak.xr.engine.execution.Operator, java.io.PrintWriter)
	 */
	@Override
	public <T> void process(TupleIterator op, PrintWriter out) {
		this.writeProlog(op, out);
		while (op.hasNext()) {
			this.writeRecord(op.next(), out);
		}
		this.writeEpilog(out);
	}
	
	/**
	 * Writes a single data unit (or tuple).
	 * @param o
	 * @param buffer
	 */
	private void writeRecord(Object o, PrintWriter out) {
		out.write("<tr>");
		this.writeValue(o, out);
		out.write("</tr>");
	}

	/**
	 * Writes a single value.
	 * @param o
	 * @param buffer
	 */
	private void writeValue(Object o, PrintWriter out) {
		out.write("<td>");
		out.write(String.valueOf(o));
		out.write("</td>");
	}
	
	/**
	 * Writes the beginning of the result table.
	 * @param o
	 * @param buffer
	 */
	private <T> void writeProlog(TupleIterator op, PrintWriter out) {
		out.write("<table>");
		List<String> header = op.getColumnsDisplay();
		if (!header.isEmpty()) {
			out.write("<tr>");
			for (String h: header) {
				out.write("<th>");
				out.write(h);
				out.write("</th>");
			}
			out.write("</tr>");
		}
	}
	
	/**
	 * Writes the end of the result table.
	 * @param o
	 * @param buffer
	 */
	private void writeEpilog(PrintWriter out) {
		out.write("</table>");
	}

}
