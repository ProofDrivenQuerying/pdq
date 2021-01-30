// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.fol.Dependency;

/**
 * Serializable class that contains dependency arrays. Initialized with a Schema.
 *
 * @author Camilo Ortiz
 */
public class DependencyList {
    public EGD[] EGDDependencies;
    public TGD[] TGDDependencies;
    public int id;

    public DependencyList(Schema s, int id){
        this.id = id;

        uk.ac.ox.cs.pdq.fol.EGD[] egd = s.getKeyDependencies();
        Dependency[] tgd = s.getNonEgdDependencies();

        this.EGDDependencies = new EGD[egd.length];
        for (int i = 0; i < egd.length; i++){
            String n = egd[i].getName();
            String d = egd[i].toString();

            this.EGDDependencies[i] = new EGD(n, d);
        }

        this.TGDDependencies = new TGD[tgd.length];
        for (int j = 0; j< tgd.length; j++){
            String n = tgd[j].getName();
            String d = tgd[j].toString();

            this.TGDDependencies[j] = new TGD(n, d);
        }
    }

}
