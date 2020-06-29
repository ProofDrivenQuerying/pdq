// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.datasources.utility;

import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodAttributeSpecification;
import uk.ac.ox.cs.pdq.datasources.services.service.RESTExecutableAccessMethodSpecification;
import uk.ac.ox.cs.pdq.datasources.services.service.Service;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;

public class SanityCheck {

	/**
	 * Sanity checks services against a schema.
	 */
	public static void sanityCheck(Schema schema, Service[] services) throws Exception
	{
		for(Service service : services)
		{
			RESTExecutableAccessMethodSpecification[] reamss = service.getAccessMethod();
			for(RESTExecutableAccessMethodSpecification reams : reamss)
			{
				String name = reams.getRelationName();
				Relation relation = schema.getRelation(name);
				if(relation == null)
				{
					throw new Exception("Service: " + service.getName() + " not found in relations");
				}
				RESTExecutableAccessMethodAttributeSpecification[] reamas = reams.getAttributes();
				for(RESTExecutableAccessMethodAttributeSpecification reama : reamas)
				{
					String name2 = reama.getRelationAttribute();
					if(relation.getAttribute(name2) == null)
					{
						throw new Exception("Service: " + service.getName() + " Attribute: " + name2 + " not found in Relation: " + name);
					}
				}
			}
		}
	}

}
