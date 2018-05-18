package uk.ac.ox.cs.pdq.datasources.services;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.service.ServiceRoot;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.servicegroup.ServiceGroupsRoot;

// AccessMethodManager calls JAXB and AccessMethodRoot to marshal or unmarshal a file
public class ServiceManager {

	public static ServiceGroupsRoot importServiceGroups(File schema) throws JAXBException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!schema.exists() )
			throw new FileNotFoundException(schema.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(ServiceGroupsRoot.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (ServiceGroupsRoot) jaxbUnmarshaller.unmarshal(schema);
	}

	public static ServiceRoot importAccessMethod(File schema) throws JAXBException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!schema.exists() )
			throw new FileNotFoundException(schema.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(ServiceRoot.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (ServiceRoot) jaxbUnmarshaller.unmarshal(schema);
	}
	
	public static void exportAccessMethod(ServiceRoot schema, File targetFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(ServiceRoot.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(schema, targetFile);
	}
}
