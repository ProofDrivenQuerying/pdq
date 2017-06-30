package uk.ac.ox.cs.pdq.test.io;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethod;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.util.Utility;

// TODO: Auto-generated Javadoc
/**
 * The Class XMLSchemaIOTest.
 */
public class RelationIOTest {
	
	AccessMethod 
		free = AccessMethod.create("mt1", new Integer[]{}),
		limited = AccessMethod.create("mt2", new Integer[]{1}),
		bool = AccessMethod.create("mt2", new Integer[]{1});

	Attribute 
		a = Attribute.create(String.class, "a"),
		b = Attribute.create(Integer.class, "b"), 
		c = Attribute.create(Double.class, "c");
	
	/** The r3. */
	Relation 
		r1 = new Relation("r1", new Attribute[]{a}, new AccessMethod[]{free}) {
			private static final long serialVersionUID = 1L;}, 
		r2 = new Relation("r2", new Attribute[]{a,b}, new AccessMethod[]{limited,bool}) {
			private static final long serialVersionUID = 1L;},
		r3 = new Relation("r3", new Attribute[]{a,b,c}) {
			private static final long serialVersionUID = 1L;};

//	/** The bos. */
//	ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//	/**
//	 * Setup.
//	 */
//	@Before public void setup() {
//		Utility.assertsEnabled();
//		this.bos = new ByteArrayOutputStream();
//	}

	/**
	 * Test read write schema relations only.
	 * @throws JAXBException 
	 */
	@Test
	public void testWriteRelations() throws JAXBException {
		JAXBContext jc = JAXBContext.newInstance(Relation.class);
        JAXBElement<Relation> je2 = new JAXBElement<Relation>(new QName("Relation"), Relation.class, r1);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(je2, System.out);
	}
}
