
package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.io.WriterException;

// TODO: Auto-generated Javadoc
/**
 * Writes schemas to XML.
 * 
 * @author Julien Leblay
 */
public class SchemaWriter extends AbstractXMLWriter<Schema> {

	/**  Relations writer. */
	private RelationWriter relationWriter = null;

	/**  Dependency writer. */
	private DependencyWriter dependencyWriter = null;
	
	/**
	 * Default constructor.
	 */
	public SchemaWriter() {
		this.relationWriter = new RelationWriter();
		this.dependencyWriter = new DependencyWriter();
	}
	
	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param o Schema
	 */
	@Override
	public void write(PrintStream out, Schema o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeSchema(out, o);
	}


	/**
	 * Writes schema to the given stream with some additional attributes in the root
	 * element.
	 *
	 * @param out the out
	 * @param o the o
	 * @param atts the atts
	 */
	public void write(PrintStream out, Schema o, Map<QNames, String> atts) {
		this.writeSchema(out, o, atts);
	}

	/**
	 * Writes the given schema to the given output.
	 *
	 * @param out the out
	 * @param s the s
	 */
	private void writeSchema(PrintStream out, Schema s) {
		this.writeSchema(out, s, new LinkedHashMap<QNames, String>());
	}

	/**
	 * Writes the given schema to the given output.
	 *
	 * @param out the out
	 * @param s the s
	 * @param atts Map<QNames,String>
	 */
	private void writeSchema(PrintStream out, Schema s, Map<QNames, String> atts) {
		open(out, QNames.SCHEMA, atts);
		this.writeSources(out, s.getRelations());
		this.writeRelations(out, s.getRelations());
		this.writeDependencies(out, s.getDependencies());
		close(out, QNames.SCHEMA);
	}

	/**
	 * Writes the given collection of relations to the given output.
	 *
	 * @param out the out
	 * @param rs Collection<Relation>
	 */
	private void writeSources(PrintStream out, Collection<Relation> rs) {
		Set<Properties> properties = new LinkedHashSet<>();
		for (Relation r: rs) {
			properties.add(r.getProperties());
		}
		if (properties.size() > 0 && properties.size() == 1 ?
				!properties.iterator().next().isEmpty() : true) {
			open(out, QNames.SOURCES);
			for (Properties p: properties) {
				Properties p2 = new Properties(p);
				p2.remove(Relation.PropertyKeys.METADATA);
				if (!p2.isEmpty()) {
					openclose(out, QNames.SOURCE, p2);
				}
			}
			close(out, QNames.SOURCES);
		}
	}

	/**
	 * Writes the given collection of relations to the given output.
	 *
	 * @param out the out
	 * @param rs Collection<Relation>
	 */
	private void writeRelations(PrintStream out, Collection<Relation> rs) {
		open(out, QNames.RELATIONS);
		for (Relation r : rs) {
			this.relationWriter.writeRelation(out, r);
		}
		close(out, QNames.RELATIONS);
	}

	/**
	 * Write the schema's dependencies to the given output.
	 *
	 * @param out the out
	 * @param ds the ds
	 */
	private void writeDependencies(PrintStream out, Collection<Dependency> ds) {
		open(out, QNames.DEPENDENCIES);
		for (Dependency d : ds) {
			if (d instanceof TGD) {
				this.dependencyWriter.writeDependency(out, (TGD) d);
			} else {
				throw new WriterException("Only TGD (amonc integraty constraints) are currently supported.");
			}
		}
		close(out, QNames.DEPENDENCIES);
	}
}
