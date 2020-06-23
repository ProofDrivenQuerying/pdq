// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

/**
 * 
 */
package uk.ac.ox.cs.pdq.test.datasources.services.legacy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.legacy.services.Service;
import uk.ac.ox.cs.pdq.datasources.legacy.services.ServiceReader;
import uk.ac.ox.cs.pdq.datasources.legacy.services.ServiceRepository;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;

/**
 * 
 * Tests the service reader class by parsing some of the xml files in the services folder. Also
 * generates an access and prints the results.
 * 
 * @author gabor
 *
 */
public class ServiceReaderTest {
	@Ignore // service is no longer available
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
