package uk.ac.ox.cs.pdq.io.jaxb;

import java.io.File;
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
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedSchema;

public class IOManager {

	public static Schema importSchema(File schema) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedSchema.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AdaptedSchema customer = (AdaptedSchema) jaxbUnmarshaller.unmarshal(schema);
		return customer.toSchema();
	}

	public static ConjunctiveQuery importQuery(File query) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedQuery.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AdaptedQuery customer = (AdaptedQuery) jaxbUnmarshaller.unmarshal(query);
		return customer.toQuery();
	}

	public static Pair<Schema, ConjunctiveQuery> importSchemaAndQuery(File folder) throws JAXBException {
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

	public static void exportQueryToXml(ConjunctiveQuery q, File targetFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedQuery.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		AdaptedQuery aq = new AdaptedQuery(q);
		jaxbMarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		jaxbMarshaller.marshal(aq, targetFile);
	}

	public static void exportQueryToXml(ConjunctiveQuery q, OutputStream out) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedQuery.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		AdaptedQuery aq = new AdaptedQuery(q);
		jaxbMarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		jaxbMarshaller.marshal(aq, out);
	}

	public static void exportSchemaToXml(Schema schema, File targetFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedSchema.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(new AdaptedSchema(schema), targetFile);
	}

	public static void exportSchemaToXml(Schema schema, OutputStream out) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedSchema.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(new AdaptedSchema(schema), out);
	}
	
	/**
	 * @deprecated - unfinished function
	 */
	public static RelationalTerm readRelationalTerm(File file, Schema schema) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Schema.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(schema, file);
		return null;
	}
}
