package uk.ac.ox.cs.pdq.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.builder.SchemaDiscoverer;
import uk.ac.ox.cs.pdq.db.Constraint;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.io.ReaderException;

// TODO: Auto-generated Javadoc
/**
 * Reads schemas from XML.
 * 
 * @author Julien Leblay
 * @author Efthymia Tsamoura
 */
public class SchemaReader extends AbstractXMLReader<Schema> {

	/**  The relation reader. */
	private RelationReader relationReader = null;

	/**  The dependency reader. */
	private DependencyReader dependencyReader;
	
	/** The builder to build and return. */
	private SchemaBuilder builder = null;

	/** Temporary list of relations being built. */
	private List<Relation> relations = new ArrayList<>();

	/** Temporary list of dependencies being built. */
	private List<Constraint> dependencies = new ArrayList<>();
	
	/** A map from the builder discovered' names to their instances. */
	private Map<String, SchemaDiscoverer> discovered;

	/**
	 * Default constructor.
	 */
	public SchemaReader() {
		this(new LinkedHashMap<String, SchemaDiscoverer>());
	}

	/**
	 * Default constructor.
	 * @param discovered Map<String,SchemaDiscoverer>
	 */
	public SchemaReader(Map<String, SchemaDiscoverer> discovered) {
		this.builder = Schema.builder();
		this.discovered = discovered;
		this.relationReader = new RelationReader(this.builder, this.discovered);
		this.dependencyReader = new DependencyReader(this.builder);
	}

	/**
	 * Read.
	 *
	 * @param in InputStream
	 * @return Schema
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(InputStream)
	 */
	@Override
	public Schema read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.builder.build();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ReaderException("Exception thrown while reading builder ", e);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		switch(QNames.parse(qName)) {
		case SCHEMA:
			break;
		case SOURCES:
			this.discovered.clear();
			break;
		case SOURCE:
			Properties props = new Properties();
			for (int i = 0, l = atts.getLength(); i < l; i++) {
				props.put(atts.getLocalName(i), atts.getValue(i));
			}
			String name = props.getProperty(QNames.NAME.format());
			String disco = props.getProperty(QNames.DISCOVERER.format());
			if (name != null && disco != null) {
				try {
					SchemaDiscoverer sd = (SchemaDiscoverer) Class.forName(disco).newInstance();
					sd.setProperties(props);
					this.discovered.put(name, sd);
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					throw new ReaderException("Unabled to load discoverer " + disco + ".");
				}
			} else {
				throw new ReaderException("Datasource definition missing a '" + QNames.NAME.format() + "' and/or '" + disco + "' parameter");
			}
			break;
		case RELATIONS:
		case VIEWS:
			this.relations.clear();
			break;
		case RELATION:
		case VIEW:
		case ATTRIBUTE:
		case ACCESS_METHOD:
			this.relationReader.startElement(uri, localName, qName, atts);
			break;
		case DEPENDENCIES:
			this.dependencies.clear();
			break;
		case DEPENDENCY:
		case BODY:
		case HEAD:
		case ATOM:
		case VARIABLE:
		case CONSTANT:
			this.dependencyReader.startElement(uri, localName, qName, atts);
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
		case DEPENDENCY:
		case BODY:
		case HEAD:
		case ATOM:
		case VARIABLE:
		case CONSTANT:
			this.dependencyReader.endElement(uri, localName, qName);
			break;
		case RELATION:
		case VIEW:
			this.relationReader.endElement(uri, localName, qName);
			this.relations.add(this.relationReader.getRelation());
			break;
		case ATTRIBUTE:
		case ACCESS_METHOD:
			this.relationReader.endElement(uri, localName, qName);
			break;
		default:
			return;
		}
	}
	
	/**
	 * Gets the schema.
	 *
	 * @return a instantiation of the builder being in built.
	 */
	public Schema getSchema() {
		return this.builder.build();
	}
	
}
