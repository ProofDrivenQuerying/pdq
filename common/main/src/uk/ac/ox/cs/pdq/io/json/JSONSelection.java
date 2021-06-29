// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;

/**
 * Serializable SelectionTerm class.
 *
 * @author Camilo Ortiz
 */
public class JSONSelection extends JSONPlan{
    @JsonProperty
    String condition;

    public JSONSelection(SelectionTerm rt) {
        super(rt);
        this.condition = rt.getSelectionCondition().toString();
    }
    public String toString() {
        StringBuilder toReturn = new StringBuilder("{ command: " + this.command +
                ", condition: " + this.condition + ", " +
                "subexpression: [");

        for (JSONRelationalTerm jsonPlan : this.subexpression) {
            toReturn.append(jsonPlan.toString());
            toReturn.append(", ");
        }
        toReturn.append("]}");
        return toReturn.toString();
    }
}
