// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import uk.ac.ox.cs.pdq.algebra.RelationalTerm;

/**
 * Default instantiable RelationalTerm class.
 *
 * @author Camilo Ortiz
 * @Contributor Brandon Moore
 */
public class JSONPlan extends JSONRelationalTerm{

    public JSONPlan(RelationalTerm rt) {
        super(rt);
    }

    public String toString() {
        StringBuilder toReturn = new StringBuilder("{ command: " + this.command +
                 ", " +
                "subexpression: [");

        for (JSONRelationalTerm jsonPlan : this.subexpression) {
            toReturn.append(jsonPlan.toString());
            toReturn.append(", ");
        }
        toReturn.append("]}");
        return toReturn.toString();
    }
}
