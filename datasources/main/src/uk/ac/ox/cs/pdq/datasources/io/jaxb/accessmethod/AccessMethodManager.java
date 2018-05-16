package uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedSchema;

// AccessMethodManager calls JAXB and AccessMethodRoot to marshal or unmarshal a file
public class AccessMethodManager {

	public static AccessMethodRoot importAccessMethod(File schema) throws JAXBException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!schema.exists() )
			throw new FileNotFoundException(schema.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(AccessMethodRoot.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AccessMethodRoot customer = (AccessMethodRoot) jaxbUnmarshaller.unmarshal(schema);
		return customer;
	}
	
	public static void exportAccessMethod(AccessMethodRoot schema, File targetFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AccessMethodRoot.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(schema, targetFile);
	}
}
