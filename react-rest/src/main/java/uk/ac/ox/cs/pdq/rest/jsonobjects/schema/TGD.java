// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 * Serializable TGD class
 *
 * @author Camilo Ortiz
 */
public class TGD {
    public String name;
    public String definition;

    public TGD(String n, String d){
        this.name = n;
        this.definition = d;
    }
}
