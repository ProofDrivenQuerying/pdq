// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

/**
 * 
 */
package uk.ac.ox.cs.pdq.cost.statistics;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;

/**
 * Frequency histograms.
 * @author Efthymia Tsamoura
 *
 */
public class SimpleFrequencyMap {

	/** The read relation column. */
	private static String READ_RELATION_COLUMN = "^(RE:(\\w+)(\\s+)AT:(\\w+)(\\s+)HH:)";
	
	/**  Reads constants *. */
	private static String READ_BINS = "\\(VA:([\\w|\\s|#|\\d|-]+) FR:(\\d+)\\)+";

	/** The relation. */
	private final Relation relation;
	
	/** The attibute. */
	private final Attribute attibute;
	
	/** The frequencies. */
	private final Map<String,Integer> frequencies;

	/**
	 * Instantiates a new simple frequency map.
	 *
	 * @param relation the relation
	 * @param attibute the attibute
	 * @param values the values
	 */
	private SimpleFrequencyMap(Relation relation, Attribute attibute, Map<String,Integer> values) {
		this.relation = relation;
		this.attibute = attibute;
		this.frequencies = values;
	}

	/**
	 * Builds the.
	 *
	 * @param schema the schema
	 * @param histogram the histogram
	 * @return the simple frequency map
	 */
	public static SimpleFrequencyMap build(Schema schema, String histogram) {			
		Triple<Relation, Attribute, Map<String, Integer>> h = parse(schema, histogram);
		return h == null ? null : new SimpleFrequencyMap(h.getLeft(), h.getMiddle(), h.getRight());			
	}

	/**
	 * Parses the.
	 *
	 * @param schema the schema
	 * @param histogram the histogram
	 * @return the triple
	 */
	private static Triple<Relation,Attribute,Map<String,Integer>> parse(Schema schema, String histogram) {
		Preconditions.checkNotNull(schema);
		Preconditions.checkNotNull(histogram);
		Pattern p = Pattern.compile(READ_RELATION_COLUMN);
		Matcher m = p.matcher(histogram);
		if (m.find()) {
			String relation = m.group(2);
			String attribute = m.group(4);
			if(schema.contains(relation)) {
				Relation r = schema.getRelation(relation);
				Attribute a = r.getAttribute(attribute);
				if(a == null) {
					throw new java.lang.IllegalStateException("RELATION " + relation + " DOES NOT CONTAINT ATTRIBUTE " + attribute);
				}

				Map<String,Integer> frequencies = new HashMap<>();
				Pattern p2 = Pattern.compile(READ_BINS);
				Matcher m2 = p2.matcher(histogram);
				while (m2.find()) {
					String value = m2.group(1);
					String freq = m2.group(2);
					frequencies.put(value, Integer.parseInt(freq));
				}
				if(!frequencies.isEmpty()) {
					return Triple.of(r, a, frequencies);
				}
				else {
					throw new java.lang.IllegalStateException("UNPARSABLE INPUT LINE " + histogram);
				}
			}
			else {
				throw new java.lang.IllegalStateException("SCHEMA DOES NOT CONTAINT RELATION " + relation);
			}
		}
		return null;
	}

	/**
	 * Gets the frequency.
	 *
	 * @param entry the entry
	 * @return the frequency
	 */
	public Integer getFrequency(String entry) {
		Preconditions.checkNotNull(this.frequencies.get(entry));
		return this.frequencies.get(entry);
	}

	/**
	 * Gets the frequencies.
	 *
	 * @return the frequencies
	 */
	public Map<String, Integer> getFrequencies() {
		return this.frequencies;
	}

	/**
	 * Gets the relation.
	 *
	 * @return the relation
	 */
	public Relation getRelation() {
		return this.relation;
	}

	/**
	 * Gets the attibute.
	 *
	 * @return the attibute
	 */
	public Attribute getAttibute() {
		return this.attibute;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return Joiner.on("\t").join(this.frequencies.entrySet());
	}
}