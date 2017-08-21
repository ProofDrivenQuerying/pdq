package uk.ac.ox.cs.pdq.datasources.utility;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.db.Schema;

/**
 * @author Gabor
 *
 */
public class SchemaConverter {

	public static void convert(File from, File to) throws FileNotFoundException, JAXBException {
		Schema schema = DbIOManager.importSchema(from);
		if (to.isDirectory()) {
			DbIOManager.exportSchemaToXml(schema, new File(to,from.getName()));
		} else {
			DbIOManager.exportSchemaToXml(schema, to);
		}
	}
	
	public static void main(String[] args) {
		File to = new File("C:\\Work\\converted");
		
		File src = new File("c:\\Users\\Gabor\\git\\pdq\\regression\\test\\linear\\fast\\demo\\derby\\case_001\\schema.xml");
		try {
			convert(src,to);
		} catch (FileNotFoundException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
