package uk.ac.ox.cs.pdq.datasources.services;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.ServiceGroup;

// AccessMethodManager calls JAXB and AccessMethodRoot to marshal or unmarshal a file
public class ServiceManager {

	public static ServiceGroup importServiceGroups(File schema) throws JAXBException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!schema.exists() )
			throw new FileNotFoundException(schema.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(ServiceGroup.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (ServiceGroup) jaxbUnmarshaller.unmarshal(schema);
	}

	public static Service importAccessMethod(File schema) throws JAXBException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!schema.exists() )
			throw new FileNotFoundException(schema.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(Service.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		return (Service) jaxbUnmarshaller.unmarshal(schema);
	}
	
	public static void exportAccessMethod(Service schema, File targetFile) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(Service.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(schema, targetFile);
	}
}
