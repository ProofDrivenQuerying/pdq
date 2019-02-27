package uk.ac.ox.cs.pdq.regression.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.xml.bind.JAXBException;

import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.accessrepository.AccessRepository;
import uk.ac.ox.cs.pdq.datasources.io.jaxb.DbIOManager;
import uk.ac.ox.cs.pdq.datasources.memory.InMemoryAccessMethod;
import uk.ac.ox.cs.pdq.datasources.sql.SqlAccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import uk.ac.ox.cs.pdq.exceptions.DatabaseException;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

/**
 * Contains utility methods to create a new test case from scratch or convert an existing regression test
 * to runtime-compatible test with auto generated access methods using templates.
 * 
 * @author gabor
 *
 */
public class CaseGenerator {

	public CaseGenerator() {
	}
	
	/**
	 * Main function. No parameter processing, input and output directory is hardcoded.
	 */
	public static void main(String[] args) {
		try {
			File root = new File("test/planner/linear/fast/tpch/simple/");
			CaseGenerator generator = new CaseGenerator();
			for (File f: root.listFiles()) {
				generator.convert(f, f);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	/**
	 *  Converts a test case from normal regression test to runtime test by adding executable access methods.
	 * @param in
	 * @param out
	 * @throws DatabaseException
	 * @throws JAXBException
	 * @throws IOException
	 */
	private boolean memory = true;
	private void convert(File in, File out) throws DatabaseException, JAXBException, IOException {
		System.out.println("Converting " + in.getAbsolutePath()); 
		Schema s = IOManager.importSchema(new File(in,"schema.xml"));
		if (!out.exists()) {
			out.mkdirs();
		}
		File sql = new File(out,"accesses");
		File mem = new File(out,"accessesMem");
		sql.mkdirs();
		mem.mkdirs();
		Entry<RelationalTerm, Cost> expectedPlan = PlannerTestUtilities.obtainPlan(in.getAbsolutePath() + '/' + "expected-plan.xml", s);
		if (expectedPlan == null)
			return;
		Set<AccessTerm> accesses = expectedPlan.getKey().getAccesses();
		
		AccessRepository repo = AccessRepository.getRepository(sql.getAbsolutePath());
		if (memory) {
			for (AccessTerm at:accesses) {
				System.out.println("Converting " + at.getAccessMethod().getName()); 
				ExecutableAccessMethod access = repo.getAccess(at.getAccessMethod().getName());
				try {
					List<Tuple> target = new ArrayList<>();
					access.access().forEach(target::add);
					InMemoryAccessMethod newAccess = new InMemoryAccessMethod(access.getName(), access.outputAttributes(),access.getInputs(), access.getRelation(), access.getAttributeMapping(false));
					newAccess.load(target);
					DbIOManager.exportAccessMethod(newAccess, new File(mem,at.getAccessMethod().getName() + ".xml"));
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			for (Relation r: s.getRelations()) {
				for (AccessMethodDescriptor amd:r.getAccessMethods()) {
					System.out.println("Converting " + r.getName() + "//" + amd.getName()); 
					createSqlAccess(sql,amd,r);
				}
			}
		}
	}


	private void createSqlAccess(File sqlOut, AccessMethodDescriptor amd, Relation r) throws JAXBException {
		Map<Attribute, Attribute> mapping = ExecutableAccessMethod.getDefaultMapping(r);
		SqlAccessMethod sam = new SqlAccessMethod(amd.getName(),r.getAttributes(), amd.getInputs(),r,mapping , getProperties());
		DbIOManager.exportAccessMethod(sam, new File(sqlOut,amd.getName()+".xml"));
	} 
	/** DB properties for the tpch database.
	 * @return
	 */
	public static Properties getProperties() {
		Properties properties = new Properties();
		properties.setProperty("url", "jdbc:postgresql://localhost:5432/");
		properties.setProperty("database", "tpch");
		properties.setProperty("username", "postgres");
		properties.setProperty("password", "root");
		return(properties);
	}
	
}
