package uk.ac.ox.cs.pdq.datasources.io.jaxb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.adapted.AdaptedDbSchema;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.TypedConstant;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Term;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedAccessMethod;
import uk.ac.ox.cs.pdq.io.jaxb.adapted.AdaptedRelation;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * Reads a Schema that contains external (database) sources, such as: <code>
 * <source name="tpch" discoverer=
"uk.ac.ox.cs.pdq.sql.PostgresqlSchemaDiscoverer" 
 * 		driver="org.postgresql.Driver" 
 * 		url="jdbc:postgresql://localhost/" 
 * 		database="tpch_0001" username="root" password="root" />
 * </code>
 * 
 * @author Gabor
 *
 */
public class DbIOManager extends IOManager {
	public static File CSV_FOLDER = new File("test/schemas/accesses/data/");
	static {
		// create the data folder for csv files if it does not exists.
		CSV_FOLDER.mkdirs();
	}

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
	public static Schema importSchema(File schema, Properties props) throws JAXBException, FileNotFoundException,
			ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (!schema.exists())
			throw new FileNotFoundException(schema.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AdaptedDbSchema customer = (AdaptedDbSchema) jaxbUnmarshaller.unmarshal(schema);
		return customer.toSchema(props);
	}

	public static Schema importSchema(File schema) throws JAXBException, FileNotFoundException {
		try {
			if (!schema.exists())
				throw new FileNotFoundException(schema.getAbsolutePath());
			JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			AdaptedDbSchema customer = (AdaptedDbSchema) jaxbUnmarshaller.unmarshal(schema);
			return customer.toSchema(null);
		} catch (Throwable t) {
			throw new JAXBException("Error while parsing file: " + schema.getAbsolutePath(), t);
		}
	}

	public static AdaptedDbSchema readAdaptedSchema(File schema) throws JAXBException, FileNotFoundException {
		try {
			if (!schema.exists())
				throw new FileNotFoundException(schema.getAbsolutePath());
			JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			AdaptedDbSchema customer = (AdaptedDbSchema) jaxbUnmarshaller.unmarshal(schema);
			return customer;
		} catch (Throwable t) {
			throw new JAXBException("Error while parsing file: " + schema.getAbsolutePath(), t);
		}
	}

	public static Map<AccessMethodDescriptor, String> createCatalog(File schema, File to)
			throws JAXBException, FileNotFoundException {
		try {
			if (!schema.exists())
				throw new FileNotFoundException(schema.getAbsolutePath());
			FileWriter fw = new FileWriter(to);
			BufferedWriter bw = new BufferedWriter(fw);
			JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			AdaptedDbSchema customer = (AdaptedDbSchema) jaxbUnmarshaller.unmarshal(schema);
			customer.toSchema(null);
			Map<AccessMethodDescriptor, String> map = AdaptedAccessMethod.getMapOfCosts();
			for (AdaptedRelation r : customer.getAdaptedRelations()) {
				if (r.getSize() != null) {
					// System.out.println("" + r.getName() + " size = " + r.getSize());
					// RE:AssayLimited CA:1148942
					bw.write("RE:" + r.getName() + "\t\t\t\t\t\t\t\t\t" + "CA:" + r.getSize() + "\n");
				}
				if (r.getAccessMethods() != null) {
					for (AccessMethodDescriptor am : r.getAccessMethods()) {
						// RE:relation_name BI:access_method_name RT:cost_as_in_xml
						bw.write("RE:" + r.getName() + "\t\t\t\t" + "BI:" + am.getName() + "\t\tRT:" + map.get(am)
								+ "\n");
						// System.out.println("\t" + r.getName() + "." + am.getName() + " cost = " +
						// map.get(am));
					}
				}

			}
			bw.close();
			if (to.length() == 0) {
				to.delete();
			}
			return map;
		} catch (Throwable t) {
			throw new JAXBException("Error while parsing file: " + schema.getAbsolutePath(), t);
		}
	}

	public static void exportAdaptedSchemaToXml(AdaptedDbSchema s, File out) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedDbSchema.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(s, out);
	}

	/**
	 * Reads and executable access method from an xml file.
	 * 
	 * @param xmlFile
	 * @return
	 * @throws JAXBException
	 */
	public static ExecutableAccessMethod importAccess(File xmlFile) throws JAXBException {

		try {
			if (!xmlFile.exists())
				throw new FileNotFoundException(xmlFile.getAbsolutePath());
			JAXBContext jaxbContext = JAXBContext.newInstance(XmlExecutableAccessMethod.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			XmlExecutableAccessMethod xmlAccessMethod = (XmlExecutableAccessMethod) jaxbUnmarshaller.unmarshal(xmlFile);
			return xmlAccessMethod.toExecutableAccessMethod(null);
		} catch (Throwable t) {
			throw new JAXBException("Error while parsing file: " + xmlFile.getAbsolutePath(), t);
		}
	}

	/**
	 * Prints an executable access method to Xml.
	 * 
	 * @param m
	 * @param out
	 * @throws JAXBException
	 */
	public static void exportAccessMethod(ExecutableAccessMethod m, File out) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(XmlExecutableAccessMethod.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(new XmlExecutableAccessMethod(m), out);
	}

	/**
	 * Imports comma separated file as a list of atoms. Inputs are the path to the
	 * file and the Relation which is needed to know how to convert the data.
	 * 
	 * @param r
	 * @param csvFile
	 * @return
	 */
	public static Collection<Atom> importFacts(Relation r, String csvFile) {
		return importFacts(r, new File(csvFile));
	}
	public static Collection<Atom> importFacts(Relation r, File csvFile) {
		Collection<Atom> facts = Sets.newHashSet();
		BufferedReader reader = null;
		try {
			// Open the csv file for reading
			reader = new BufferedReader(new FileReader(csvFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tuple = line.split(",");
				List<Term> constants = Lists.newArrayList();
				for (int i = 0; i < tuple.length; ++i) {
					constants.add(TypedConstant.create(tuple[i].replace("\"", "")));
				}
				facts.add(Atom.create(r, constants.toArray(new Term[constants.size()])));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return facts;
	}

	/**
	 * Creates a file called relationName.csv in the given folder containing all
	 * tuples line by line like: "alpha","beta","gamma"
	 * 
	 * @param relationName
	 * @param folder
	 * @param tuples
	 * @return the created csv file.
	 * @throws IOException
	 */
	public static File exportTuples(String relationName, File folder, Collection<Tuple> tuples) throws IOException {
		File target = new File(folder, relationName + ".csv");
		try (FileWriter fw = new FileWriter(target)) {
			for (Tuple t : tuples) {
				StringBuilder builder = null;
				for (Object value : t.getValues()) {
					if (builder == null) {
						builder = new StringBuilder();
					} else {
						builder.append(",");
					}
					builder.append("\"");
					builder.append(value);
					builder.append("\"");
				}
				builder.append("\r\n");
				fw.write(builder.toString());
			}
			fw.close();
		}
		return target;
	}

	public static Collection<Tuple> importTuples(Attribute[] attributes, String csvFile) {
		Collection<Tuple> facts = Sets.newHashSet();
		BufferedReader reader = null;
		TupleType tt = TupleType.DefaultFactory.createFromTyped(attributes);
		try {
			// Open the csv file for reading
			reader = new BufferedReader(new FileReader(csvFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tuple = line.split(",");
				Object[] constants = new Object[attributes.length];
				for (int i = 0; i < tuple.length; ++i) {
					constants[i] = (TypedConstant.convertStringToType(tuple[i].replace("\"", ""),
							attributes[i].getType()));
				}
				facts.add(tt.createTuple(constants));
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return facts;
	}

}
