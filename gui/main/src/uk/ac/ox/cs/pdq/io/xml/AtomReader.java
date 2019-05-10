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

import com.google.common.base.Preconditions;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.db.builder.SchemaBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Predicate;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.util.Types;

// TODO: Auto-generated Javadoc
/**
 * Reads atoms from XML.
 * 
 * @author Julien Leblay
 */
public class AtomReader extends AbstractXMLReader<Atom> {

	/** Logger. */
	private static Logger log = Logger.getLogger(AtomReader.class);

	/** The Constant UNBOUND_VARIABLE_PREFIX. */
	private static final String UNBOUND_VARIABLE_PREFIX = "_";
	
	/** The counter. */
	private static Integer counter = 0;
	
	/** List of atoms built so far. */
	private List<Atom> atoms = null;

	/** The atom to build and return. */
	private Atom atom = null;

	/** Temporary relation on which the atom is based being built. */
	private Relation relation = null;

	/** Temporary list of terms being built. */
	private List<Term> terms = new ArrayList<>();

	/** The schema of which the built atoms must belong . */
	private Schema schema = null;

	/** The schema builder of which the built atoms must belong . */
	private SchemaBuilder schemaBuilder = null;

	/** Current position in a predicate terms list. */
	private int position;
	
	/**
	 * Default constructor. 
	 * @param schema Schema.Builder
	 * @param atoms List<PredicateFormula>
	 */
	public AtomReader(SchemaBuilder schema, List<Atom> atoms) {
		this.schemaBuilder = schema;
		this.atoms = atoms;
	}
	
	/**
	 * Default constructor. 
	 * @param schema Schema
	 * @param atoms List<PredicateFormula>
	 */
	public AtomReader(Schema schema, List<Atom> atoms) {
		this.schema = schema;
		this.atoms = atoms;
	}

	/**
	 * Gets the atoms.
	 *
	 * @return List<PredicateFormula>
	 */
	public List<Atom> getAtoms() {
		return this.atoms;
	}

	/**
	 * Sets the atoms.
	 *
	 * @param atoms List<PredicateFormula>
	 */
	public void setAtoms(List<Atom> atoms) {
		this.atoms = atoms;
	}
	
	/**
	 * Read.
	 *
	 * @param in InputStream
	 * @return PredicateFormula
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(InputStream)
	 */
	@Override
	public Atom read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.atom;
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
		switch(QNames.parse(qName)) {
		case ATOM:
			this.relation = this.getRelation(this.getValue(atts, QNames.NAME));			
			Preconditions.checkNotNull(this.relation, "Referring to undefined relation '" + this.getValue(atts, QNames.NAME) + "'");
			this.terms = new ArrayList<>();
			this.position = 0;
			break;

		case CONSTANT:
			Attribute attribute = this.relation.getAttribute(this.position);
			String value = this.getValue(atts, QNames.VALUE);
			if (value == null) {
				throw new ReaderException("Syntax error. Constant requires a value attribute");
			}
			Object o = Types.cast(attribute.getType(), value);
			this.terms.add(TypedConstant.create(o));
			this.position++;
			break;

		case VARIABLE:
			String name = this.getValue(atts, QNames.NAME);
			if (name != null) {
				this.terms.add(new Variable(name));
			} else {
				this.terms.add(generateVariable());
			}
			this.position++;
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
		case ATOM:
			Term[] terms = new Term[this.terms.size()];
			int i = 0;
			for(Term term : this.terms) terms[i++] = term;
			Predicate p = Predicate.create(relation.getName(), this.terms.size());
			this.atom = Atom.create(p, terms);
			this.atoms.add(this.atom);
			this.terms = new ArrayList<>();
			this.relation = null;
			break;

		default:
			return;
		}
	}
	
	/**
	 * Gets the relation.
	 *
	 * @param name the name
	 * @return the relation the atom being built is backed.
	 */
	public Relation getRelation(String name) {
		if (this.schemaBuilder != null) {
			return this.schemaBuilder.getRelation(name);
		} else if (this.schema != null) {
			return this.schema.getRelation(name);
		} else {
			throw new IllegalStateException("No schema defined for AtomReader.");
		}
	}
	
	/**
	 * Generate variable.
	 *
	 * @return Variable
	 */
	private static Variable generateVariable() {
		synchronized (counter) {
			return new Variable(UNBOUND_VARIABLE_PREFIX + (counter++));
		}
	}
}
