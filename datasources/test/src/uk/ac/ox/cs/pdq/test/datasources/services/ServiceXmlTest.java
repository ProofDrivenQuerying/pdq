// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.datasources.services;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.ox.cs.pdq.datasources.services.RESTAccessMethodGenerator;
import uk.ac.ox.cs.pdq.datasources.services.ServiceManager;
import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodSpecification;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.datasources.services.servicegroup.ServiceGroup;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.db.tuple.TupleType;

// @author Mark Ridler

public class ServiceXmlTest {
	
	// chembl-activityFree test requires a null input tuple due to free status (no inputs)
 
	// This service has:
	//   name="ActivityFree"
	//   protocol="REST"
	//   url="https://www.ebi.ac.uk/chembl/api/data/activity.json" 
	//   media-type="application/json"
	//   documentation=""
	//   result-delimiter="activities"
	
	// The schema describes an Access Method:
	//   type="FREE"
	//   name="chembl_activity_free"
	// with 30 attributes such as "activity_comment" and "molecule_chembl_id"
	// There are 30 output attributes and no input attributes, hence the FREE marker

	@Test
	public void test1() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "chembl-activityFree.xml");
			ServiceGroup sgr = ServiceManager.importServiceGroups(schemaFile1);
			Service sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				RESTExecutableAccessMethodSpecification am = sr.getAccessMethod()[i];
				TupleType tupleType = TupleType.DefaultFactory.createFromTyped();
				Tuple input = tupleType.createTuple();
				RESTAccessMethodGenerator ream = new RESTAccessMethodGenerator(sgr, sr, am);
				Table t = ream.getRestAccessMethod().accessTable(input);
				System.out.println(t);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	// chembl-assayFree test requires a null input tuple due to free status (no inputs)
	
	// This service has:
	//   name="AssayFree"
	//   protocol="REST"
	//   url="https://www.ebi.ac.uk/chembl/api/data/assay.json" 
	//   media-type="application/json"
	//   documentation=""
	//   result-delimiter="assays"
	
	// The schema describes an Access Method:
	//   type="FREE"
	//   name="chembl_assay_free"
	// with 22 attributes such as "assay_category" and "document_chembl_id"
	// There are 22 output attributes and no input attributes, hence the FREE marker

	@Test
	public void test2() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "chembl-assayFree.xml");
			ServiceGroup sgr = ServiceManager.importServiceGroups(schemaFile1);
			Service sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				RESTExecutableAccessMethodSpecification am = sr.getAccessMethod()[i];
				TupleType tupleType = TupleType.DefaultFactory.createFromTyped();
				Tuple input = tupleType.createTuple();
				RESTAccessMethodGenerator ream = new RESTAccessMethodGenerator(sgr, sr, am);
				Table t = ream.getRestAccessMethod().accessTable(input);
				System.out.println(t);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	// ebeye-uniprot-protein test requires a 4-way input tuple due to defined input

	// This service has:
	//   name="EBEYEProtein"
	//   protocol="REST"
	//   url="http://www.ebi.ac.uk/ebisearch/ws/rest/uniprotkb" 
	//   media-type="application/json"
	//   documentation="http://www.uniprot.org/help/programmatic_access"
	//   result-delimiter="domains"
	
	// The schema describes an Access Method:
	//   type="LIMITED"
	//   name="ebeye_uniprot_protein_3"
	// with 4 attributes such as "id" and "status"
	// There are 4 output attributes and 4 input attributes, hence the LIMITED marker

	@Test
	public void test3() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "ebeye-uniprot-protein.xml");
			ServiceGroup sgr = ServiceManager.importServiceGroups(schemaFile1);
			Service sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				RESTExecutableAccessMethodSpecification am = sr.getAccessMethod()[i];
				TupleType tupleType = TupleType.DefaultFactory.createFromTyped(Attribute.create(String.class, "temp1"),
																			   Attribute.create(String.class, "temp2"),
																			   Attribute.create(String.class, "temp3"),
																			   Attribute.create(String.class, "temp4"));
				Tuple input = tupleType.createTuple("1", "2", "3", "4");
				RESTAccessMethodGenerator ream = new RESTAccessMethodGenerator(sgr, sr, am);
				Table t = ream.getRestAccessMethod().accessTable(input);
				System.out.println(t);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	// reactome-biopaxExporter test requires a 1-way input tuple due to defined input

	// This service has:
	//   url="http://reactomews.oicr.on.ca:8080/ReactomeRESTfulAPI/RESTfulWS/biopaxExporter" 
	//   media-type="text/plain"
	//   documentation="http://reactomews.oicr.on.ca:8080/ReactomeRESTfulAPI/ReactomeRESTFulAPI.html"
	
	// The schema describes an Access Method:
	//   type="FREE"
	//   name="reactome_species_1"
	// with 5 attributes such as "ORGANISM" and "COMMENT"
	// There are 5 output attributes and no input attributes, hence the FREE marker
	
	@Ignore //- 3rd party service stopped working 
	@Test
	public void test4() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "reactome-biopaxExporter.xml");
			ServiceGroup sgr = ServiceManager.importServiceGroups(schemaFile1);
			Service sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				RESTExecutableAccessMethodSpecification am = sr.getAccessMethod()[i];
				TupleType tupleType = TupleType.DefaultFactory.createFromTyped(Attribute.create(String.class, "temp1"));
				Tuple input = tupleType.createTuple("1");
				RESTAccessMethodGenerator ream = new RESTAccessMethodGenerator(sgr, sr, am);
				Table t = ream.getRestAccessMethod().accessTable(input);
				System.out.println(t);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}

	// reactome-speciesList test requires a 6-way input due to defined inputs
	
	// This service has:
	//   name="reactome-speciesList"
	//	 url="http://reactomews.oicr.on.ca:8080/ReactomeRESTfulAPI/RESTfulWS/speciesList" 
	//	 media-type="application/xml"
    //	 documentation="http://reactomews.oicr.on.ca:8080/ReactomeRESTfulAPI/ReactomeRESTFulAPI.html"
	//   result-delimiter="places/place"
	
	// The schema describes an Access Method:
	//   type="LIMITED"
	//   name="reactome_species_1"
	// with 2 attributes such as "displayName" and "schemaClass"
	// There are 2 output attributes and 1 input attributes, hence the LIMITED marker
	
	@Ignore //- 3rd party service stopped working 
	@Test
	public void test5() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "reactome-speciesList.xml");
			ServiceGroup sgr = ServiceManager.importServiceGroups(schemaFile1);
			Service sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				RESTExecutableAccessMethodSpecification am = sr.getAccessMethod()[i];
				TupleType tupleType = TupleType.DefaultFactory.createFromTyped(Attribute.create(String.class, "temp1"),
																			   Attribute.create(String.class, "temp2"),
																			   Attribute.create(String.class, "temp3"),
																			   Attribute.create(String.class, "temp4"),
																			   Attribute.create(String.class, "temp5"),
				   															   Attribute.create(String.class, "temp6"));
				Tuple input = tupleType.createTuple("1", "2", "3", "4", "5", "6");
				RESTAccessMethodGenerator ream = new RESTAccessMethodGenerator(sgr, sr, am);
				Table t = ream.getRestAccessMethod().accessTable(input);
				System.out.println(t);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}

	// pdq webapp nation input

	// This service has:
	//   name="pdgWebappNationInput"
    //   protocol="REST"
	//   url="http://localhost:8080/webapp/servlets/servlet/NationInput" 
	//   media-type="application/xml"
	//   documentation=""
	//   result-delimiter="places/place"
	
	// The schema describes an Access Method:
	//   name="pdqWebappNationInput"
	// with 4 attributes such as "n_nationkey" and "n_comment"
	// There are 4 output attributes and 1 input attribute
	
	@Test
	public void test6() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "pdq-webapp-nationInput.xml");
			ServiceGroup sgr = ServiceManager.importServiceGroups(schemaFile1);
			Service sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				RESTExecutableAccessMethodSpecification am = sr.getAccessMethod()[i];
				TupleType tupleType = TupleType.DefaultFactory.createFromTyped(Attribute.create(String.class, "temp1"));
				Tuple input = tupleType.createTuple("1");
				RESTAccessMethodGenerator ream = new RESTAccessMethodGenerator(sgr, sr, am);
				Table t = ream.getRestAccessMethod().accessTable(input);
				System.out.println(t);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}
	// pdq webapp country free

	// This service has:
	//   name="pdgWebappCountryFree"
	//   protocol="REST"
	//   url="http://localhost:8080/webapp/servlets/servlet/CountryFree" 
	//   media-type="application/xml"
	//   documentation=""
	//   result-delimiter="places/place"
	
	// The schema describes an Access Method:
	//   type="FREE"
	//   name="pdqWebappNationInput"
	// with 2 attributes such as "c_nationkey" and "c_area"
	// There are 2 output attributes and no input attributes, hence the FREE marker
	
	@Test
	public void test7() {
		
		try
		{
			File schemaFile1 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "service-groups.xml");
			File schemaFile2 = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "test" + File.separator + "datasources" + File.separator + "services" + File.separator + "pdq-webapp-countryFree.xml");
			ServiceGroup sgr = ServiceManager.importServiceGroups(schemaFile1);
			Service sr = ServiceManager.importAccessMethod(schemaFile2);
			System.out.println(sr.toString());
			for(int i = 0; i < sr.getAccessMethod().length; i++)
			{
				RESTExecutableAccessMethodSpecification am = sr.getAccessMethod()[i];
				TupleType tupleType = TupleType.DefaultFactory.createFromTyped();
				Tuple input = tupleType.createTuple();
				RESTAccessMethodGenerator ream = new RESTAccessMethodGenerator(sgr, sr, am);
				Table t = ream.getRestAccessMethod().accessTable(input);
				System.out.println(t);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Assert.fail();
		}
	}
}
