// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.jaxb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.util.PdqTest;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

// @author Gabor
public class AccessTermIOTest {
	@Before
	public void setup() {
		PdqTest.assertsEnabled();
	}

	/** Creates and writes a new AccessTerm that contains inputConstants, then attempts to read it back from xml.
	 * @throws FileNotFoundException
	 */
	@Test
	public void testIO() throws FileNotFoundException {
		try {
			File schemaFile = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "io" + File.separator + "jaxb" + File.separator + "schema.xml");
			File out = new File("test" + File.separator + "src" + File.separator + "uk" + File.separator + "ac" + File.separator + "ox" + File.separator + "cs" + File.separator + "pdq" + File.separator + "io" + File.separator + "jaxb" + File.separator + "AccessTermOut.xml");
			Schema schema = IOManager.importSchema(schemaFile);
			Map<Integer, TypedConstant> inputConstants = new HashMap<>();
			inputConstants.put(0, TypedConstant.create(13));
			inputConstants.put(1, TypedConstant.create(2));
			AccessTerm atInputs = AccessTerm.create(schema.getRelation(0), schema.getRelation(0).getAccessMethod("m2"),inputConstants);
			IOManager.writeRelationalTerm(atInputs, out);
			Assert.assertTrue(out.exists());
			AccessTerm rtReadWithInputs = (AccessTerm)IOManager.readRelationalTerm(out, schema);
			Assert.assertTrue(rtReadWithInputs.getInputConstants()!=null);
			Assert.assertTrue(rtReadWithInputs.getInputConstants().get(0).getValue()==((Integer)13));
			out.delete();
		} catch (JAXBException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

}
