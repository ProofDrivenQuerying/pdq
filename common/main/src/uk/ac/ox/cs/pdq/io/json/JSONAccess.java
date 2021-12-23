// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
import uk.ac.ox.cs.pdq.io.PlanPrinter;

/**
 * Serializable AccessTerm class.
 *
 * @author Camilo Ortiz
 * @Contributor Brandon Moore
 */
public class JSONAccess extends JSONRelationalTerm {
    @JsonProperty
    String accessMethod;
    @JsonProperty
    String relationName;
    @JsonProperty
    String accessString;

    public JSONAccess(AccessTerm rt) {
        super(rt);
        this.accessMethod = rt.getAccessMethod().getName();
        this.relationName = rt.getRelation().getName();

        StringBuilder result = new StringBuilder();
        result.append(rt.getRelation().getName());
        result.append(",");
        result.append(rt.getAccessMethod().getName());
        result.append('[');
        for (int index = 0; index < rt.getAccessMethod().getInputs().length; ++index) {
            result.append("#");
            Attribute provenanceName = PlanPrinter.outputAttributeProvenance(rt,rt.getAccessMethod().getInputs()[index]);
            result.append(provenanceName.getName());
            result.append("=");
            if(rt.getInputConstants().isEmpty()){
                result.append("?");
            }else{
                TypedConstant tc = rt.getInputConstants().get(index);
                if(tc != null){
                    result.append("\'"+rt.getInputConstants().get(index)+"\'");
                }else{
                    result.append("?");
                }
            }

            if (index < rt.getAccessMethod().getInputs().length - 1)
                result.append(",");
        }
        result.append(']');

        accessString = result.toString();

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
