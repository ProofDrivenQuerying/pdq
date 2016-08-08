package uk.ac.ox.cs.pdq.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.db.Dependency;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TGD;
import uk.ac.ox.cs.pdq.db.builder.DependencyBuilder;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.io.ReaderException;

import com.google.common.base.Preconditions;

/**
 * Reads dependencies from XML.
 * 
 * @author Julien Leblay
 */
public class DependencyReader extends AbstractXMLReader<Dependency> {

	/** Logger. */
	private static Logger log = Logger.getLogger(DependencyReader.class);

	/** The dependency builder  */
	private DependencyBuilder builder = null;

	/** The dependency being built. */
	private TGD dependency = null;

	/** Temporary list of left atoms being built. */
	private List<Atom> leftAtoms = null;
	
	/** Temporary list of right atoms being built. */
	private List<Atom> rightAtoms = null;
	
	/**  The atom reader for the left-hand side of the dependency. */
	private AtomReader leftAtomsReader = null;
	
	/**  The atom reader for the right-hand side of the dependency. */
	private AtomReader rightAtomsReader = null;
	
	/** Number of times the parser reached a body element (should not exceed 1). */
	private int inBody = 0;
	
	/** Number of times the parser reached a head element (should not exceed 1). */
	private int inHead = 0;
	
	/** The schema. */
	protected Schema schema = null;
	
	/** The schema builder. */
	protected SchemaBuilder schemaBuilder = null;

	/**
	 * Default constructor.
	 *
	 * @param schema Schema
	 */
	public DependencyReader(Schema schema) {
		this.schema = schema;
		this.leftAtoms = new ArrayList<>();
		this.rightAtoms = new ArrayList<>();
		this.leftAtomsReader = new AtomReader(schema, this.leftAtoms);
		this.rightAtomsReader = new AtomReader(schema, this.rightAtoms);
	}

	/**
	 * Default constructor.
	 *
	 * @param sb Schema.Builder
	 */
	public DependencyReader(SchemaBuilder sb) {
		this.schemaBuilder = sb;
		this.leftAtoms = new ArrayList<>();
		this.rightAtoms = new ArrayList<>();
		this.leftAtomsReader = new AtomReader(sb, this.leftAtoms);
		this.rightAtomsReader = new AtomReader(sb, this.rightAtoms);
	}

	@Override
	public Dependency read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.dependency;
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
	public void startElement(String uri, String localName, String qName,
			Attributes atts) {
		switch(QNames.parse(qName)) {
		case DEPENDENCY:
		case AXIOM:
			this.builder = new DependencyBuilder();
			this.dependency = null;
			this.inBody = 0;
			this.inHead = 0;
			break;

		case BODY:
			this.inBody++;
			break;

		case HEAD:
			Preconditions.checkState(this.inBody == 1, "Head element encountered before a body was defined");
			this.inHead++;
			break;

		case ATOM:
		case VARIABLE:
		case CONSTANT:
			if (this.inBody == 1) {
				if (this.inHead == 0) {
					this.leftAtomsReader.startElement(uri, localName, qName, atts);
				} else if (this.inHead == 1) {
					this.rightAtomsReader.startElement(uri, localName, qName, atts);
				}
			} else {
				throw new ReaderException("Illegal element " + qName + ". A dependency has exactly one head and one body elements." + this.inBody + ", " + this.inHead);
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
		case DEPENDENCY:
			this.dependency = this.builder.build();
			if (this.schemaBuilder != null) {
				this.schemaBuilder.addDependency(this.dependency);
			}
			this.inBody--;
			this.inHead--;
			break;

		case BODY:
			Preconditions.checkState(this.inBody == 1);
			for (Atom a: this.leftAtoms) {
				this.builder.addLeftAtom(a);
			}
			this.leftAtoms = new ArrayList<>();
			this.leftAtomsReader.setAtoms(this.leftAtoms);
			break;

		case HEAD:
			Preconditions.checkState(this.inHead == 1);
			for (Atom a: this.rightAtoms) {
				this.builder.addRightAtom(a);
			}
			this.rightAtoms = new ArrayList<>();
			this.rightAtomsReader.setAtoms(this.rightAtoms);
			break;

		case ATOM:
			if (this.inBody == 1) {
				if (this.inHead == 0) {
					this.leftAtomsReader.endElement(uri, localName, qName);
				} else if (this.inHead == 1) {
					this.rightAtomsReader.endElement(uri, localName, qName);
				}
			} else {
				throw new ReaderException("Illegal element " + qName + ". A dependency has exactly one head and one body elements." + this.inBody + ", " + this.inHead);
			}
			break;

		default:
			return;
		}
	}

	/**
	 *
	 * @return the TGD read
	 */
	public TGD getDependency() {
		return this.builder.build();
	}
}
