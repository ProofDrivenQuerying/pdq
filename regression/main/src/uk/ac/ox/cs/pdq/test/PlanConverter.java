package uk.ac.ox.cs.pdq.test;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;

public class PlanConverter {
	public static void main(String[] args) {
		File to = new File("C:\\Work\\converted\\catalog.properties");
		
		File src = new File("c:\\Users\\Gabor\\git\\pdq\\regression\\test\\linear\\fast\\demo\\derby\\case_002\\schema.xml");
		try {
			System.out.println(DbIOManager.createCatalog(src,to));
		} catch (FileNotFoundException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
