package uk.ac.ox.cs.pdq.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.builder.QueryBuilder;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.util.QNames;

// TODO: Auto-generated Javadoc
/**
 * Reads queries from XML.
 * 
 * @author Julien Leblay
 */
public class QueryReader extends AbstractXMLReader<ConjunctiveQuery> {

	/**  Query builder. */
	private QueryBuilder builder = new QueryBuilder();
	
	/** True if the parse is in the body. */
	private Boolean inBody = null;

	/** Temporary list of atoms being built. */
	private List<Atom> atoms = null;

	/**  The atom reader. */
	private AtomReader atomReader = null;

	/**
	 * Default constructor.
	 *
	 * @param schema Schema
	 */
	public QueryReader(Schema schema) {
		this.atoms = new ArrayList<>();
		this.atomReader = new AtomReader(schema, this.atoms);
	}
	
	/**
	 * {@inheritDoc}
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(java.io.InputStream)
	 */
	@Override
	public ConjunctiveQuery read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.builder.build();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new ReaderException("Unable to read/parse query ", e);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		switch(QNames.parse(qName)) {
		case QUERY:
			this.builder = new QueryBuilder();
			this.builder.setType(this.getValue(atts, QNames.TYPE));
			this.inBody = null;
			break;

		case BODY:
			this.inBody = true;
			break;

		case HEAD:
			this.inBody = false;
			this.builder.setName(this.getValue(atts, QNames.NAME));
			break;

		case ATOM:
			if (this.inBody != null && this.inBody) {
				this.atomReader.startElement(uri, localName, qName, atts);
			} else {
				throw new ReaderException("Illegal element " + qName);
			}
			break;

		case VARIABLE:
			if (this.inBody) {
				this.atomReader.startElement(uri, localName, qName, atts);
			} else {
				this.builder.addHeadTerm(new Variable(this.getValue(atts, QNames.NAME)));
			}
			break;

		case CONSTANT:
			if (this.inBody) {
				this.atomReader.startElement(uri, localName, qName, atts);
			} else {
				throw new ReaderException("Constants currently not supported in query head.");
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
		case BODY:
			for (Atom a: this.atoms) {
				this.builder.addBodyAtom(a);
			}
			break;

		case ATOM:
			this.atomReader.endElement(uri, localName, qName);
			break;

		default:
			return;
		}
	}
	
	/**
	 * Gets the query.
	 *
	 * @return a instantiation of the query being in built.
	 */
	public ConjunctiveQuery getQuery() {
		return this.builder.build();
	}
}
