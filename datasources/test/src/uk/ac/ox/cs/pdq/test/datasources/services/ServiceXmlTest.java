package uk.ac.ox.cs.pdq.test.datasources.services;

import java.io.File;

import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.services.RESTExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.services.ServiceManager;
import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodSpecification;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.ServiceGroup;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

public class ServiceXmlTest {
	
	@Test
	public void testServiceXml() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "reactome-speciesList.xml");
			File outputFile = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "yahoo-services2.xml");
			ServiceGroup sgr = ServiceManager.importServiceGroups(schemaFile1);
			Service sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				RESTExecutableAccessMethodSpecification am = sr.getAccessMethod()[i];
				TupleType tupleType = TupleType.DefaultFactory.createFromTyped(Attribute.create(String.class, "temp1"),
																			   Attribute.create(String.class, "temp2"),
																			   Attribute.create(String.class, "temp3"));
				Tuple input = tupleType.createTuple("1", "2", "3");
				RESTExecutableAccessMethod ream = new RESTExecutableAccessMethod(sgr, sr, am, input);
				Table t = ream.access();
				System.out.println(t);
			}
			ServiceManager.exportAccessMethod(sr, outputFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
