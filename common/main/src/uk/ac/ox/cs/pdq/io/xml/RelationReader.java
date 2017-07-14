package uk.ac.ox.cs.pdq.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.builder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.io.ReaderException;

/**
 * Reads relations from XML.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class RelationReader extends AbstractXMLReader<Relation> {

	/** Logger. */
	private static Logger log = Logger.getLogger(RelationReader.class);

	/** Relation being built. */
	protected Relation relation = null;

	/** Name of temporary relation being built. */
	protected String relationName = null;

	/** TOCOMMENT */
	protected String isEquality = null;

	/** Temporary list of attributes being built. */
	protected List<Attribute> attributes = new ArrayList<>();

	/** Temporary list of binding patterns being built. */
	private List<AccessMethod> accessMethods = new ArrayList<>();
	
	/** Temporary list of keys being built. */
	private List<Attribute> key = new ArrayList<>();

	/** Temporary map of binding patterns to their per-tuple costs. */
	private Map<AccessMethod, Cost> accessCosts = new LinkedHashMap<>();

	/** The schema. */
	protected SchemaBuilder schema;
	
	/** Boolean tracing with the relation being read is a view. */
	protected boolean isView = false;
	
	/** TOCOMMENT: the size fo what? */
	protected Long size;
	
	/** TOCOMMENT: what?. */
	protected Map<String, SchemaDiscoverer> discovered = null;

	/**
	 * Default constructor. 
	 * @param schema Schema.Builder
	 * @param d Map<String,SchemaDiscoverer>
	 */
	public RelationReader(SchemaBuilder schema, Map<String, SchemaDiscoverer> d) {
		this.schema = schema;
		this.discovered = d;
	}

	/**
	 * Default constructor. 
	 */
	public RelationReader() {
		this(null, new LinkedHashMap<String, SchemaDiscoverer>());
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(java.io.InputStream)
	 */
	@Override
	public Relation read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.relation;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error(e);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		String source = null;
		switch(QNames.parse(qName)) {
		case RELATION:
		case VIEW:
			this.relation = null;
			this.relationName = this.getValue(atts, QNames.NAME);
			this.isEquality = this.getValue(atts, QNames.EQUALITY);

			source = this.getValue(atts, QNames.SOURCE);
			if (source != null && !source.trim().isEmpty()) {
				SchemaDiscoverer sd = this.discovered.get(source);
				Schema s = sd.discover();
				this.relation = s.getRelation(this.relationName);
				if (this.relation == null) {
					throw new ReaderException("Relation '" + this.relationName + "' is not part of the discovered schema.");
				}
				this.relation.getProperties().putAll(sd.getProperties());
			} else {
				this.attributes.clear();
			}
			String s = this.getValue(atts, QNames.SIZE);
			if (s != null) {
				this.size = Long.valueOf(s);
			} else {
				this.size = null;
			}
			this.isView = QNames.parse(qName) == QNames.VIEW;
			break;
		case ATTRIBUTE:
			try {
				this.attributes.add(
						Attribute.create(
								Class.forName(this.getValue(atts, QNames.TYPE)),
								this.getValue(atts, QNames.NAME)));
			} catch (ClassNotFoundException e) {
				throw new ReaderException("Class '" 
						+ this.getValue(atts, QNames.TYPE)
						+ "' not found for attribute '"
						+ this.getValue(atts, QNames.NAME));
			}
			break;
		case ACCESS_METHOD:
			String positions = this.getValue(atts, QNames.INPUTS);
			List<Integer> inputs = new ArrayList<>();
			if (positions != null && !positions.trim().isEmpty()) {
				inputs.addAll(toIntList(positions));
			}
			AccessMethod b = null;
			String name = this.getValue(atts, QNames.NAME);
			String type = this.getValue(atts, QNames.TYPE);
			if (type == null && name != null && !name.trim().isEmpty()) {
				b = this.relation.getAccessMethod(name);
				if (b == null) {
					throw new ReaderException("No such access method " + name);
				}
			} else if (name != null && !name.trim().isEmpty()) {
				b = new AccessMethod(name, Types.valueOf(type), inputs);
			} else {
				b = new AccessMethod(Types.valueOf(type), inputs);
			}
			String cost = this.getValue(atts, QNames.COST);
			if (cost != null && !cost.trim().isEmpty()) {
				this.accessCosts.put(b, new DoubleCost(Double.valueOf(cost)));
			}
			this.accessMethods.add(b);
			break;
		case KEY:
			String attributes = this.getValue(atts, QNames.ATTRIBUTES);
			for(String key:attributes.split(",")) {
				Attribute k = this.relation.getAttribute(key);
				Preconditions.checkNotNull(k, "Undentified input key");
				this.key.add(k);
			}
			break;
		default:
			throw new ReaderException("Illegal element " + qName);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) {
		switch(QNames.parse(qName)) {
		case RELATION:
			if (this.relation == null) {
				boolean isEq = "true".equalsIgnoreCase(this.isEquality);
				if (this.attributes != null && !this.attributes.isEmpty()) {
					this.relation = new InMemoryTableWrapper(this.relationName, this.attributes, isEq);
				} else {
					throw new IllegalStateException(this.relationName + " not properly loaded.");
				}
			}
			break;
		case VIEW:
			if (this.relation == null) {
				if (this.attributes != null && !this.attributes.isEmpty()) {
					this.relation = new InMemoryViewWrapper(this.relationName, this.attributes, this.accessMethods);
				} else {
					throw new IllegalStateException(this.relationName + " not properly loaded.");
				}
			}
			break;
		default:
			return;
		}
		RelationMetadata metadata = this.relation.getMetadata();
		if (metadata == null) {
			metadata = new StaticMetadata();
			this.relation.setMetadata(metadata);
		}
		if (this.size != null) {
			metadata.setSize(this.size);
		}
		if (!this.accessCosts.isEmpty()) {
			metadata.setPerInputTupleCosts(this.accessCosts);
		}
		this.relation.setAccessMethods(this.accessMethods, true);
		this.relation.setKey(this.key);
		if (this.schema != null) {
			this.schema.addRelation(this.relation);
		}
		this.attributes = new ArrayList<>();
		this.accessMethods = new ArrayList<>();
		this.key = new ArrayList<>();
	}

	/**	
	 * @return the relation under construction
	 */
	public Relation getRelation() {
		return this.relation;
	}
}
