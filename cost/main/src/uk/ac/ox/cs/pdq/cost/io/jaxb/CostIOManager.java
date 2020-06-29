// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.cost.io.jaxb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.cost.Cost;
import uk.ac.ox.cs.pdq.cost.io.jaxb.adapted.AdaptedRelationalTermWithCost;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.jaxb.IOManager;

/**
 * Can read and write a relationalTerm object that contains a cost. This cost is
 * the full cost of the plan represented in the given RelationalTerm.
 * 
 * @author Gabor
 *
 */
public class CostIOManager extends IOManager {

	/**
	 * Reads the cost from a relationalTerm descriptor xml if the file contains a
	 * cost.
	 * 
	 * @throws FileNotFoundException
	 * @throws JAXBException
	 */
	public static Cost readRelationalTermCost(File planFile, Schema schema) throws FileNotFoundException, JAXBException {
		if (!planFile.exists())
			throw new FileNotFoundException(planFile.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedRelationalTermWithCost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AdaptedRelationalTermWithCost customer = (AdaptedRelationalTermWithCost) jaxbUnmarshaller.unmarshal(planFile);
		return customer.getCost();
	}
	public static RelationalTerm readRelationalTermFromRelationaltermWithCost(File planFile, Schema schema) throws FileNotFoundException, JAXBException {
		if (!planFile.exists())
			throw new FileNotFoundException(planFile.getAbsolutePath());
		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedRelationalTermWithCost.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AdaptedRelationalTermWithCost customer = (AdaptedRelationalTermWithCost) jaxbUnmarshaller.unmarshal(planFile);
		return customer.toRelationalTerm();
	}

	public static void writeRelationalTermAndCost(File targetPlanFile, RelationalTerm plan, Cost cost) throws JAXBException {
		AdaptedRelationalTermWithCost rt = new AdaptedRelationalTermWithCost(plan);
		rt.setCost(cost);

		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedRelationalTermWithCost.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		jaxbMarshaller.marshal(rt, targetPlanFile);
	}
	
	public static void writeRelationalTermAndCost(OutputStream output, RelationalTerm plan, Cost cost) throws JAXBException {
		AdaptedRelationalTermWithCost rt = new AdaptedRelationalTermWithCost(plan);
		rt.setCost(cost);

		JAXBContext jaxbContext = JAXBContext.newInstance(AdaptedRelationalTermWithCost.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		jaxbMarshaller.marshal(rt, output);
	}
}
