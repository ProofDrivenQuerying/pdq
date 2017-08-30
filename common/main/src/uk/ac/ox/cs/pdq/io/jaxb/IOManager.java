package uk.ac.ox.cs.pdq.io.jaxb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.ConjunctiveQuery;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedQuery;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedRelationalTerm;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedSchema;

/**
 * Main class for the jaxb xml parser. It can export RelationalTerms, Schemas
 * and ConjuntiveQueries to xml or import them from xml.
 * 
 * @see RelationalTerm
 * @see Schema
 * @see ConjunctiveQuery
 * 
 * @author Gabor
 *
 */
public class IOManager {

	/**
	 * Imports a Schema object from file.
	 * 
	 * @param schema
	 *            File pointer to the xml file.
	 * @return parsed Schema object
	 * @throws JAXBException
	 *             In case importing fails.
	 * @throws FileNotFoundException 
	 */
	public static Schema importSchema(File schema) throws JAXBException, FileNotFoundException {
		if (!schema.exists() )
			throw new FileNotFoundException(schema.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedSchema.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AdaptedSchema customer = (AdaptedSchema) jaxbUnmarshaller.unmarshal(schema);
		return customer.toSchema();
	}

	/**
	 * Imports a Query object from file.
	 * 
	 * @param schema
	 *            File pointer to the xml file.
	 * @return imported Query object
	 * @throws JAXBException
	 *             In case importing fails.
	 * @throws FileNotFoundException 
	 */
	public static ConjunctiveQuery importQuery(File query) throws JAXBException, FileNotFoundException {
		if (!query.exists() )
			throw new FileNotFoundException(query.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedQuery.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AdaptedSchema s = new AdaptedSchema(); // create an empty schema as current for query reading.
		AdaptedQuery customer = (AdaptedQuery) jaxbUnmarshaller.unmarshal(query);
		return customer.toQuery();
	}

	/**
	 * Helper function that expects a folder with "query.xml" and "schema.xml" files
	 * and parses them both.
	 * 
	 * @param folder
	 *            File pointer to the folder containing "query.xml" and "schema.xml"
	 *            files.
	 * @return Pair of ConjunctiveQuery and Schema objects.
	 * @throws JAXBException
	 *             in case there is a parsing error.
	 * @throws FileNotFoundException 
	 */
	public static Pair<Schema, ConjunctiveQuery> importSchemaAndQuery(File folder) throws JAXBException, FileNotFoundException {
		Schema left = null;
		ConjunctiveQuery right = null;
		File schema = new File(folder, "schema.xml");
		File query = new File(folder, "query.xml");
		if (schema.exists()) {
			left = importSchema(schema);
		}
		if (query.exists()) {
			right = importQuery(query);
		}
		return new ImmutablePair<Schema, ConjunctiveQuery>(left, right);
	}

	/**
	 * Creates a new file describing the query object.
	 * 
	 * @param q
	 *            the object to export to the file.
	 * @param targetFile
	 *            File pointer to a non-existing query.xml file.
	 * @throws JAXBException
	 *             in case exporting fails.
	 */
	public static void exportQueryToXml(ConjunctiveQuery q, File targetFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedQuery.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		AdaptedQuery aq = new AdaptedQuery(q);
		jaxbMarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		jaxbMarshaller.marshal(aq, targetFile);
	}

	/**
	 * Same as above but the output is not a file but an output stream. Can be used
	 * to print out the xml to the console:
	 * 
	 * <example>IOManager.exportQueryToXml(q, System.out);</example>
	 * 
	 * @see IOManager.exportQueryToXml(ConjunctiveQuery q, File targetFile)
	 * @param q the query to export
	 * @param out OutputStream where the print should go.
	 * @throws JAXBException in case parsing fails.
	 */
	public static void exportQueryToXml(ConjunctiveQuery q, OutputStream out) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedQuery.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		AdaptedQuery aq = new AdaptedQuery(q);
		jaxbMarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		jaxbMarshaller.marshal(aq, out);
	}

	/**
	 * Creates a new file describing the Schema object.
	 * 
	 * @param schema Schema to export
	 * @param targetFile target file pointer
	 * @throws JAXBException in case parsing error happens.
	 */
	public static void exportSchemaToXml(Schema schema, File targetFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedSchema.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(new AdaptedSchema(schema), targetFile);
	}

	/**
	 * Same as above but the output is not a file but an output stream. Can be used
	 * to print out the xml to the console:
	 * 
	 * <example>IOManager.exportSchemaToXml(schema, System.out);</example>
	 * 
	 * @see IOManager.exportSchemaToXml(Schema schema, File targetFile)
	 * @param schema the Schema to export
	 * @param out OutputStream where the print should go.
	 * @throws JAXBException in case parsing fails.
	 */
	public static void exportSchemaToXml(Schema schema, OutputStream out) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedSchema.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(new AdaptedSchema(schema), out);
	}

	/** 
	 * Creates a RelationalTerm object with all it's nested relationalTerms, described by the xml file.
	 * See examples in the IOManagerTest class.
	 * @param file File pointer to the xml
	 * @param schema This is not used currently, in the future it will be used for sanity checks on the generated schemas.
	 * @return the imported RelationalTerm object.
	 * @throws JAXBException in case parsing fails.
	 */
	public static RelationalTerm readRelationalTerm(File file, Schema schema) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedRelationalTerm.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return ((AdaptedRelationalTerm) jaxbUnmarshaller.unmarshal(file)).toRelationalTerm();
	}

	/** 
	 * Creates an xml file representing the full hierarchy of a RelationalTerm object.
	 * @param t The relationalterm to export.
	 * @param targetFile Target file.
	 * @throws JAXBException in case parsing fails.
	 */
	public static void writeRelationalTerm(RelationalTerm t, File targetFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedRelationalTerm.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(new AdaptedRelationalTerm(t), targetFile);
	}

}
