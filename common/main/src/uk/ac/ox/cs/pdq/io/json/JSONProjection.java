// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.io.PlanPrinter;

import java.util.Arrays;

/**
 * Serializable Projection class.
 *
 * @author Camilo Ortiz
 * @Contributor Brandon Moore
 */
public class JSONProjection extends JSONRelationalTerm {
    @JsonProperty
    Attribute[] projections;

    @JsonProperty
    String ProvenanceProjections;

    public JSONProjection(ProjectionTerm rt) {
        super(rt);
        this.projections = rt.getProjections();

        //added provenance attribute for projections
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < rt.getProjections().length; i++) {
            String a = PlanPrinter.outputAttributeProvenance(rt, i).getName();
            buffer.append(a);
            if (i < rt.getProjections().length - 1) {
                buffer.append(", ");
            }
        }
        if(buffer.length() == 0){
            ProvenanceProjections = Arrays.toString(this.projections);
        }else {
            ProvenanceProjections = buffer.toString();
        }
    }
    public String toString() {
        StringBuilder toReturn = new StringBuilder("{ command: " + this.command +
                ", projections: " + this.ProvenanceProjections + ", " +
                "subexpression: [");

        for (JSONRelationalTerm jsonPlan : this.subexpression) {
            toReturn.append(jsonPlan.toString());
            toReturn.append(", ");
        }
        toReturn.append("]}");
        return toReturn.toString();
    }
}
