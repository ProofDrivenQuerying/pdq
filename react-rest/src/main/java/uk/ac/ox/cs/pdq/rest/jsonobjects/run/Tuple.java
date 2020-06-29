// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.run;

/**
 * Serializable Tuple object.
 *
 * @author Camilo Ortiz
 */
public class Tuple {
    public Object[] values;

    public Tuple(uk.ac.ox.cs.pdq.db.tuple.Tuple t){
        this.values = t.getValues();
    }
}
