package uk.ac.ox.cs.pdq.test.datasources.services;

import java.io.File;

import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod.AccessMethodManager;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.accessmethod.AccessMethodRoot;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleImpl;
import uk.ac.ox.cs.pdq.util.TupleType;

public class ServiceXmlTest {
	
	@Test
	public void testServiceXml() {
		
		try
		{
			File schemaFile = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "reactome-biopaxExporter.xml");
			File outputFile = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "yahoo-services2.xml");
			AccessMethodRoot amr = AccessMethodManager.importAccessMethod(schemaFile);
			System.out.println(amr.toString());
			Tuple input = new TupleImpl(TupleType.EmptyTupleType);
			Table t = amr.restAccess(input);
			System.out.println(t);
			AccessMethodManager.exportAccessMethod(amr, outputFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
