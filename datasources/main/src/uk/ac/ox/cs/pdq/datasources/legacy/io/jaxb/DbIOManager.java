package uk.ac.ox.cs.pdq.datasources.legacy.io.jaxb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.adapted.AdaptedDbSchema;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedAccessMethod;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedRelation;

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
	
	public static AdaptedDbSchema readAdaptedSchema(File schema) throws JAXBException, FileNotFoundException {
		try {
			if (!schema.exists() )
				throw new FileNotFoundException(schema.getAbsolutePath());
			JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			AdaptedDbSchema customer = (AdaptedDbSchema) jaxbUnmarshaller.unmarshal(schema);
			return customer;
		}catch(Throwable t) {
			throw new JAXBException("Error while parsing file: "+ schema.getAbsolutePath(),t);
		}
	}
	
	public static Map<AccessMethodDescriptor,String> createCatalog(File schema, File to) throws JAXBException, FileNotFoundException {
		try {
			if (!schema.exists() )
				throw new FileNotFoundException(schema.getAbsolutePath());
			FileWriter fw = new FileWriter(to);
			BufferedWriter bw = new BufferedWriter(fw);
			JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			AdaptedDbSchema customer = (AdaptedDbSchema) jaxbUnmarshaller.unmarshal(schema);
			customer.toSchema(null);
			Map<AccessMethodDescriptor, String> map = AdaptedAccessMethod.getMapOfCosts();
			for (AdaptedRelation r: customer.getAdaptedRelations()) {
				if (r.getSize()!=null) {
					//System.out.println("" + r.getName() + " size = " + r.getSize());
					//RE:AssayLimited										CA:1148942
					bw.write("RE:"+r.getName() + "\t\t\t\t\t\t\t\t\t" + "CA:"+r.getSize()+"\n");
				}
				if (r.getAccessMethods()!=null) {
					for (AccessMethodDescriptor am: r.getAccessMethods()) {
						//RE:relation_name  BI:access_method_name  			RT:cost_as_in_xml
						bw.write("RE:"+r.getName() + "\t\t\t\t" + "BI:" + am.getName() + "\t\tRT:"+map.get(am)+"\n");
						//System.out.println("\t" + r.getName() + "." + am.getName() + " cost = " + map.get(am));
					}
				}
				
			}
			bw.close();
			if (to.length()==0) {
				to.delete();
			}
			return map;
		}catch(Throwable t) {
			throw new JAXBException("Error while parsing file: "+ schema.getAbsolutePath(),t);
		}
	}

	public static void exportAdaptedSchemaToXml(AdaptedDbSchema s, File out) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(s, out);
	}
	
}
