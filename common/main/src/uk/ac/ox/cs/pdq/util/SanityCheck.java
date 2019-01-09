package uk.ac.ox.cs.pdq.util;

import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Atom;
import uk.ac.ox.cs.pdq.fol.Dependency;
import uk.ac.ox.cs.pdq.fol.Formula;
import uk.ac.ox.cs.pdq.fol.Predicate;

public class SanityCheck {

	/**
	 * Sanity checks a schema.
	 */
	public static void sanityCheck(Schema schema) throws Exception {
		Relation[] relations = schema.getRelations();
		Dependency[] dependencies = schema.getAllDependencies();
		for(Dependency dependency : dependencies)
		{
			for(Atom atom : dependency.getBodyAtoms())
			{
				String name = atom.getPredicate().getName();
				Relation relation = schema.getRelation(name);
				if(relation == null)
				{
					throw new Exception("Dependency: " + name + " does not exist as a relation");
				}
				if(atom.getNumberOfTerms() != relation.getAttributes().length)
				{		
					throw new Exception("Dependency: " + name + " arity not consistent with relation");
				}
			}
		}
	}
	
	/**
	 * Sanity checks a query against a schema.
	 */
	public static void sanityCheck(Formula query, Schema schema) throws Exception
	{
		Atom[] atoms = query.getAtoms();
		for(Atom atom : atoms)
		{
			Predicate predicate = atom.getPredicate();
			Relation relation = schema.getRelation(predicate.getName());
			if(relation == null)
			{
				throw new Exception("Relation: " + predicate.getName() + " not found in schema");
			}
			if(atom.getNumberOfTerms() > relation.getAttributes().length) 
			{
				throw new Exception("Relation: " + predicate.getName() + " too many attributes in atom");
			}
		}
	}
	
}
