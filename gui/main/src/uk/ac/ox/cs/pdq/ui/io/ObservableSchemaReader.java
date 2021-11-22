// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.io;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.ServiceGroup;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.ReaderException;
import uk.ac.ox.cs.pdq.ui.UserInterfaceException;
import uk.ac.ox.cs.pdq.ui.model.ObservableSchema;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * Reads schemas from XML.
 * 
 * @author Julien LEBLAY
 * 
 */
public class ObservableSchemaReader {

	/** Logger. */
	private static Logger log = Logger.getLogger(ObservableSchemaReader.class);

	/** The name. */
	private String name;

	/** The description. */
	private String description;
	
	private Schema schema;

	private AccessRepository services;
	
	/** A conventional schema reader, service group. */
	private ServiceGroup sgr;
	
	/** A conventional schema reader, service. */
	private Service sr;

	/**
	 * Default constructor.
	 */
	public ObservableSchemaReader() {
	}

	/*
	 * (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.benchmark.io.AbstractReader#load(java.io.InputStream)
	 */
	public ObservableSchema read(File file) {
		try {
			File schemaDir = new File(".pdq/services");
			if (!schemaDir.exists())
				schemaDir.mkdirs();
			this.services = AccessRepository.getRepository(".pdq/services");
			ArrayList<Service> list = new ArrayList<>();
//			for(File serviceFile : listFiles(schemaDir, "", ".xml"))
//			{
//				JAXBContext jaxbContext2 = JAXBContext.newInstance(Service.class);
//				Unmarshaller jaxbUnmarshaller2 = jaxbContext2.createUnmarshaller();
//				list.add((Service) jaxbUnmarshaller2.unmarshal(serviceFile));
//			}
			this.schema = DbIOManager.importSchema(file);
			this.name = homepath(file.getPath());
//			this.services = new Service[list.size()];

			return new ObservableSchema(this.name, this.description, this.schema, this.services);
		} catch (JAXBException | FileNotFoundException e) {
			throw new ReaderException("Exception thrown while reading schema ", e);
		}
	}
	
	private String homepath(String path)
	{
		String home = System.getProperty("user.dir");
		return path.replace(home, "");
	}

	private static File[] listFiles(File directory, final String prefix, final String suffix) {
		Preconditions.checkArgument(directory.isDirectory(), "Invalid internal schema directory " + directory.getAbsolutePath());
		return directory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(prefix) && name.endsWith(suffix);
			}
		});
	}
}
