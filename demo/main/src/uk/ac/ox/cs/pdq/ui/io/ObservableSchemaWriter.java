package uk.ac.ox.cs.pdq.ui.io;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.AbstractXMLWriter;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.QNames;
//import uk.ac.ox.cs.pdq.io.xml.SchemaWriter;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

// TODO: Auto-generated Javadoc
/**
 * Writes an observable schemas to XML.
 * 
 * @author Julien LEBLAY
 * 
 */
public class ObservableSchemaWriter extends AbstractXMLWriter<ObservableSchema> {

	/** Logger. */
	private static Logger log = Logger.getLogger(ObservableSchemaWriter.class);

	/**  Schema writer. */
// MR	private SchemaWriter schemaWriter = null;
	
	/**
	 * Default constructor.
	 */
	public ObservableSchemaWriter() {
// MR		this.schemaWriter = new SchemaWriter();
	}
	
	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.benchmark.io.AbstractWriter#save(java.io.PrintStream, java.lang.Object)
	 */
	@Override
	public void write(PrintStream out, ObservableSchema o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeSchema(out, o);
	}

	/**
	 * Writes the given schema to the given output.
	 *
	 * @param out the out
	 * @param s the s
	 */
	private void writeSchema(PrintStream out, ObservableSchema s) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, s.getName());
		att.put(QNames.DESCRIPTION, s.getDescription());
// MR		this.schemaWriter.write(out, s.getSchema(), att);
	}
}
