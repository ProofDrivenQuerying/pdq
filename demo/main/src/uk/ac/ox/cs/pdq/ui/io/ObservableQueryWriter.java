package uk.ac.ox.cs.pdq.ui.io;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.xml.AbstractXMLWriter;
import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.io.xml.QueryWriter;
import uk.ac.ox.cs.pdq.ui.model.ObservableQuery;

// TODO: Auto-generated Javadoc
/**
 * Writes queries to XML.
 * 
 * @author Julien LEBLAY
 * 
 */
public class ObservableQueryWriter extends AbstractXMLWriter<ObservableQuery> {

	/** Logger. */
	private static Logger log = Logger.getLogger(ObservableQueryWriter.class);

	/**  Query writer. */
	private QueryWriter queryWriter = null;
	
	/**
	 * Default constructor.
	 */
	public ObservableQueryWriter() {
		this.queryWriter = new QueryWriter();
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.benchmark.io.AbstractWriter#save(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, ObservableQuery o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeQuery(out, o);
	}

	/**
	 * Writes the given query to the given output.
	 *
	 * @param out the out
	 * @param s the s
	 */
	private void writeQuery(PrintStream out, ObservableQuery s) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, s.getName());
		att.put(QNames.DESCRIPTION, s.getDescription());
		this.queryWriter.write(out, (ConjunctiveQuery) s.getQuery(), att);
	}
}
