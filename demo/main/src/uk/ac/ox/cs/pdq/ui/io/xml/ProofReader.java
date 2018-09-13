package uk.ac.ox.cs.pdq.ui.io.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Constant;
//import uk.ac.ox.cs.pdq.fol.Skolem;
import uk.ac.ox.cs.pdq.fol.Variable;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.datasources.legacy.io.xml.AbstractXMLReader;
//import uk.ac.ox.cs.pdq.io.xml.QNames;
import uk.ac.ox.cs.pdq.planner.accessibleschema.AccessibleSchema;
import uk.ac.ox.cs.pdq.ui.proof.Proof;

// TODO: Auto-generated Javadoc
/**
 * Reads proofs from XML.
 * 
 * @author Julien Leblay
 */
public class ProofReader extends AbstractXMLReader<Proof> {

	/** Logger. */
	private static Logger log = Logger.getLogger(ProofReader.class);

	/**  Proof builder. */
	private Proof.Builder builder = null;
	
	/**  The match being built. */
	private Map<Variable, Constant> match = null;
	
	/** The schema. */
	private final Schema schema;

	/** The acc schema. */
	private final AccessibleSchema accSchema;
	
	/**
	 * Default constructor.
	 *
	 * @param schema Schema
	 */
	public ProofReader(Schema schema) {
		this.schema = schema;
		this.accSchema = new AccessibleSchema(schema);
	}
	
	/**
	 * Read.
	 *
	 * @param in the in
	 * @return Proof
	 * @see uk.ac.ox.cs.pdq.io.Reader#read(InputStream)
	 */
	@Override
	public Proof read(InputStream in) {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(in, this);
			return this.builder.build();
		} catch (ParserConfigurationException | SAXException | IOException e) {
			log.error(e);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
/* MR	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) {
		switch(QNames.parse(qName)) {
		case PROOF:
			this.builder = Proof.builder();
			break;

		case STATE:
			break;

		case CANDIDATE:
		case MATCH:
			this.match = new LinkedHashMap<>();
			break;

		case ENTRY:
			this.match.put(
					new Variable(this.getValue(atts, QNames.KEY)),
					new Skolem(this.getValue(atts, QNames.VALUE)));
			break;

		case AXIOM:
			Relation r = this.schema.getRelation(this.getValue(atts, QNames.RELATION));
			this.builder.addAxiom(
					this.accSchema.getAccessibilityAxiom(r,
							r.getAccessMethod(
									this.getValue(atts, QNames.ACCESS_METHOD))));
			break;

		default:
			throw new ReaderException("Illegal element " + qName);
		}
	}*/

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
/* MR	@Override
	public void endElement(String uri, String localName, String qName) {
		switch(QNames.parse(qName)) {
		case CANDIDATE:
			this.builder.addMatch(this.match);
			break;

		case MATCH:
			this.builder.setQueryMatch(this.match);
			break;

		default:
			return;
		}
	}*/
}
