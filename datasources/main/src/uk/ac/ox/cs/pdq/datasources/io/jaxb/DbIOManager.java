package uk.ac.ox.cs.pdq.datasources.io.jaxb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.adapted.AdaptedDbSchema;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

/**
 * Reads a Schema that contains external (database) sources, such as: 
 * <code>
 * <source name="tpch" discoverer="uk.ac.ox.cs.pdq.sql.PostgresqlSchemaDiscoverer" 
 * 		driver="org.postgresql.Driver" 
 * 		url="jdbc:postgresql://localhost/" 
 * 		database="tpch_0001" username="root" password="root" />
 * </code>
 * 
 * @author Gabor
 *
 */
public class DbIOManager extends IOManager {
	/**
	 * Imports a Schema object from file.
	 * 
	 * @param schema
	 *            File pointer to the xml file.
	 * @return parsed Schema object
	 * @throws JAXBException
	 *             In case importing fails.
	 * @throws FileNotFoundException 
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static Schema importSchema(File schema, Properties props) throws JAXBException, FileNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!schema.exists() )
			throw new FileNotFoundException(schema.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AdaptedDbSchema customer = (AdaptedDbSchema) jaxbUnmarshaller.unmarshal(schema);
		return customer.toSchema(props);
	}
	
	public static Schema importSchema(File schema) throws JAXBException, FileNotFoundException {
		try {
			if (!schema.exists() )
				throw new FileNotFoundException(schema.getAbsolutePath());
			JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			AdaptedDbSchema customer = (AdaptedDbSchema) jaxbUnmarshaller.unmarshal(schema);
			return customer.toSchema(null);
		}catch(Throwable t) {
			throw new JAXBException("Error while parsing file: "+ schema.getAbsolutePath(),t);
		}
	}
	
}
