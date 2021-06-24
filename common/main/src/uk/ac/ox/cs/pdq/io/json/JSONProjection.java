// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.db.Attribute;

import java.util.Arrays;

/**
 * Serializable Projection class.
 *
 * @author Camilo Ortiz
 */
public class JSONProjection extends JSONRelationalTerm {
    @JsonProperty
    Attribute[] projections;

    public JSONProjection(ProjectionTerm rt) {
        super(rt);
        this.projections = rt.getProjections();
    }
    public String toString() {
        StringBuilder toReturn = new StringBuilder("{ command: " + this.command +
                ", projections: " + Arrays.toString(this.projections) + ", " +
                "subexpression: [");

        for (JSONRelationalTerm jsonPlan : this.subexpression) {
            toReturn.append(jsonPlan.toString());
            toReturn.append(", ");
        }
        toReturn.append("]}");
        return toReturn.toString();
    }
}
