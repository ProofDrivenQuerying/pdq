/**
 * 
 */
package uk.ac.ox.cs.pdq.test.datasources.services.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.ResetableIterator;
import uk.ac.ox.cs.pdq.datasources.legacy.services.Service;
import uk.ac.ox.cs.pdq.datasources.legacy.services.ServiceReader;
import uk.ac.ox.cs.pdq.datasources.legacy.services.ServiceRepository;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;

/**
 * 
 * Tests the service reader class by parsing some of the xml files in the services folder. 
 * @author gabor
 *
 */
public class ServiceReaderTest {

	//@Test
	// The yahoo places / geo etc services are now maintained under the BOSS brand and they aren't free anymore.
	public void testParsingYahooWeather() throws FileNotFoundException {
		Attribute NAME = Attribute.create(String.class, "name");
		FileInputStream fis = new FileInputStream(new File("services/yahoo-services.xml"));
		ServiceReader reader = new ServiceReader();
		ServiceRepository repo = reader.read(fis);
		System.out.println(repo.getServices());
		Service sPlaces = repo.getService("YahooPlaces");
		TupleType tupleType = TupleType.DefaultFactory.createFromTyped(NAME);
		final Tuple oxford = tupleType.createTuple("Oxford");
		
		ResetableIterator<Tuple> inputTuples = new ResetableIterator<Tuple>() {
			int index = 0;
			Tuple tuples[] = new Tuple[] { oxford };
			
			@Override
			public boolean hasNext() {
				return index<tuples.length;
			}

			@Override
			public Tuple next() {
				return tuples[index++];
			}

			@Override
			public void open() {
			}

			@Override
			public void reset() {
				index=0;
			}
		};
		Table result = sPlaces.access(new Attribute[] {NAME}, inputTuples);
		System.out.println(result);
	}
	
	@Test
	public void testParsingReactomeService() throws FileNotFoundException {
		File input = new File("test/src/uk/ac/ox/cs/pdq/test/datasources/services/legacy/reactome-services.xml");
		FileInputStream fis = new FileInputStream(input);
		ServiceReader reader = new ServiceReader();
		ServiceRepository repo = reader.read(fis);
		System.out.println(repo.getServices());
		Service sOrganism = repo.getService("OrganismFree");
		Table t = sOrganism.access();
		System.out.println(t);
		System.out.println("size: " + t.size());
		//we got at least 10 organisms (last time it give 19 results on 2018 April 11.
		Assert.assertTrue(t.size() > 10);
		
	}
}
