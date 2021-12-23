// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.io.PlanPrinter;

/**
 * Serializable SelectionTerm class.
 *
 * @author Camilo Ortiz
 * @Contributor Brandon Moore
 */
public class JSONSelection extends JSONPlan{
    @JsonProperty
    String condition;

    public JSONSelection(SelectionTerm rt) {
        super(rt);

        Condition c = rt.getSelectionCondition();
        StringBuffer buffer = new StringBuffer();
        if(c instanceof ConjunctiveCondition){
            Condition[] conditions = ((ConjunctiveCondition)c).getSimpleConditions();
            buffer = getProvenanceCondition(rt, conditions);
        }
        this.condition = buffer.toString();
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

    /**
     * Private method used to get the provenance attribute from simpleConditions to
     * when called from the outputAttributeProvenance
     * @param rt
     * @param conditions
     */
    private StringBuffer getProvenanceCondition(RelationalTerm rt, Condition[] conditions){
        StringBuffer buffer = new StringBuffer();
        for(int i =0; i < conditions.length; i++ ){
            Attribute provenanceAttribute;
            Attribute otherProvenanceAttribute;
            //check position to see which child element to retrieve attribute from CartesianProductTerm
            if (conditions[i] instanceof AttributeEqualityCondition) {
                int position = ((AttributeEqualityCondition) conditions[i]).getPosition();
                int other = ((AttributeEqualityCondition) conditions[i]).getOther();

                provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                //get Other Attribute Provenance from AttributeEqualityCondition
                otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), other);
                buffer.append(String.format("#%s=#%s", provenanceAttribute.getName(), otherProvenanceAttribute.getName()));
            } else {
                if (conditions[i] instanceof ConstantEqualityCondition) {
                    int position = ((ConstantEqualityCondition) conditions[i]).getPosition();

                    provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                    buffer.append(String.format("#%s=\'%s\'", provenanceAttribute.getName(), ((ConstantEqualityCondition) conditions[i]).getConstant()));
                } else if (conditions[i] instanceof ConstantComparisonCondition) {
                    int position = ((ConstantComparisonCondition) conditions[i]).getPosition();

                    provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position);
                    buffer.append(String.format("#%s=\'%s\'", provenanceAttribute.getName(), ((ConstantComparisonCondition) conditions[i]).getConstant()));
                }
            }

            // concat & when more then one Condition is set
            if (i < conditions.length - 1) {
                buffer.append(" & ");
            }
        }
        return buffer;
    }
}
