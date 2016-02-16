package uk.ac.ox.cs.pdq.io.xml;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.View;
import uk.ac.ox.cs.pdq.db.metadata.StaticMetadata;
import uk.ac.ox.cs.pdq.util.Types;

// TODO: Auto-generated Javadoc
/**
 * Writes relations to XML.
 * 
 * @author Julien Leblay
 */
public class RelationWriter extends AbstractXMLWriter<Relation> {

	/**
	 * Writes the given relation to the given output.
	 *
	 * @param out the out
	 * @param relation the relation
	 */
	public void writeRelation(PrintStream out, Relation relation) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, relation.getName());

		if (relation.isEquality()) {
			att.put(QNames.EQUALITY, Boolean.TRUE.toString());
		}

		// If the relation is described in an external descriptor (e.g. discoverer), say it now.
		boolean describedExternally = false;
		Properties properties = relation.getProperties();
		if (properties != null && properties.containsKey(QNames.NAME.format())) {
			att.put(QNames.SOURCE, properties.getProperty(QNames.NAME.format()));
			describedExternally = true;
		}

		StaticMetadata rs = (StaticMetadata) relation.getMetadata();
		if (rs != null && rs.getSize() != 1) {
			att.put(QNames.SIZE, String.valueOf(rs.getSize()));
		}
		if (relation instanceof View) {
			open(out, QNames.VIEW, att);
		} else {
			open(out, QNames.RELATION, att);
		}
		// If the relation described externally, no need to specified the attributes
		if (!describedExternally) {
			for (Attribute a : relation.getAttributes()) {
				this.writeAttribute(out, a);
			}
		}
		for (AccessMethod b : relation.getAccessMethods()) {
			this.writeAccessMethod(out, relation, b);
		}
		if (relation instanceof View) {
			close(out, QNames.VIEW);
		} else {
			close(out, QNames.RELATION);
		}
	}
	
	/**
	 * Writes the given relation to the given output.
	 *
	 * @param out the out
	 * @param attribute the attribute
	 */
	public void writeAttribute(PrintStream out, Attribute attribute) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, attribute.getName());
		att.put(QNames.TYPE, Types.canonicalName(attribute.getType()));
		openclose(out, QNames.ATTRIBUTE, att);
	}

	/**
	 * Writes the given binding pattern to the given output.
	 *
	 * @param out the out
	 * @param r Relation
	 * @param ar the ar
	 */
	public void writeAccessMethod(PrintStream out, Relation r, AccessMethod ar) {
		Map<QNames, String> att = new LinkedHashMap<>();
		att.put(QNames.NAME, ar.getName());
		att.put(QNames.TYPE, ar.getType().name());
		String ints = "";
		for (int i: ar.getInputs()) {
			ints += i + ",";
		}
		if (!ints.isEmpty()) {
			att.put(QNames.INPUTS, ints.substring(0, ints.length() - 1));
		}
		if (r.getMetadata() != null && r.getMetadata().getPerInputTupleCost(ar) != null) {
			att.put(QNames.COST, String.valueOf(r.getMetadata().getPerInputTupleCost(ar).getValue()));
		}
		openclose(out, QNames.ACCESS_METHOD, att);
	}

	/**
	 * Write.
	 *
	 * @param out PrintStream
	 * @param o Relation
	 */
	@Override
	public void write(PrintStream out, Relation o) {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.writeRelation(out, o);
	}
}
