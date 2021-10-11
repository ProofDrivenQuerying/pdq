// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.io.PlanPrinter;

/**
 * Serializable JoinTerm class.
 *
 * @author Camilo Ortiz
 */
public class JSONJoin extends JSONRelationalTerm {
    @JsonProperty
    String condition;

    public JSONJoin(JoinTerm rt) {
        super(rt);
        ConjunctiveCondition cc = (ConjunctiveCondition) rt.getJoinConditions();
        StringBuffer buffer = getProvenanceCondition(rt,cc.getSimpleConditions());

        this.condition = buffer.toString();
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
            if(rt instanceof CartesianProductTerm){
                if (conditions[i] instanceof ConstantEqualityCondition) {
                    int position = ((ConstantEqualityCondition) conditions[i]).getPosition();
                    //get position attribute
                    if(position < rt.getChild(0).getNumberOfOutputAttributes()){
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                    }else{
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(1), position - rt.getChild(0).getNumberOfOutputAttributes());
                    }

                    buffer.append(String.format("#%s=%s", provenanceAttribute.getName(), ((ConstantEqualityCondition) conditions[i]).getConstant()));
                }else if (conditions[i] instanceof ConstantComparisonCondition) {
                    int position = ((ConstantComparisonCondition) conditions[i]).getPosition();
                    //get position attribute
                    if(position < rt.getChild(0).getNumberOfOutputAttributes()){
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                    }else{
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(1), position - rt.getChild(0).getNumberOfOutputAttributes());
                    }
                    buffer.append(String.format("#%s=%s", provenanceAttribute.getName(), ((ConstantEqualityCondition) conditions[i]).getConstant()));
                }else if (conditions[i] instanceof AttributeEqualityCondition) {
                    int position = ((AttributeEqualityCondition) conditions[i]).getPosition();
                    int other = ((AttributeEqualityCondition) conditions[i]).getOther();
                    //get position attribute
                    if(position < rt.getChild(0).getNumberOfOutputAttributes()){
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                    }else{
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(1), position - rt.getChild(0).getNumberOfOutputAttributes());
                    }

                    //get Other position attribute
                    if(other < rt.getChild(0).getNumberOfOutputAttributes()){
                        otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), other);
                    }else{
                        otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0),( other - rt.getChild(0).getNumberOfOutputAttributes()));
                    }
                    buffer.append(String.format("#%s=#%s", provenanceAttribute.getName(), otherProvenanceAttribute.getName()));
                }
            }
            // concat & when more then one Condition is set
            if (i < conditions.length - 1) {
                buffer.append(" & ");
            }
        }
        return buffer;
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
