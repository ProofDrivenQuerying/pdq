// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;

/**
 * Serializable AccessTerm class.
 *
 * @author Camilo Ortiz
 */
public class JSONAccess extends JSONRelationalTerm {
    @JsonProperty
    String accessMethod;
    @JsonProperty
    String relationName;

    public JSONAccess(AccessTerm rt) {
        super(rt);
        this.accessMethod = rt.getAccessMethod().getName();
        this.relationName = rt.getRelation().getName();
    }
    public String toString() {
        StringBuilder toReturn = new StringBuilder("{ command: " + this.command +
                ", accessMethod: " + this.accessMethod + ", relationName: " + this.relationName + ", " +
                "subexpression: [");

        for (JSONRelationalTerm jsonPlan : this.subexpression) {
            toReturn.append(jsonPlan.toString());
            toReturn.append(", ");
        }
        toReturn.append("]}");
        return toReturn.toString();
    }
}
