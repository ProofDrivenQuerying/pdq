package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.SelectionTerm;


public class JSONSelection extends JSONPlan{
    @JsonProperty
    String condition;

    public JSONSelection(SelectionTerm rt) {
        super(rt);
        this.condition = rt.getSelectionCondition().toString();
    }
    public String toString() {
        // Ask Michael whether both input attributes and output attributes are arguments
        StringBuilder toReturn = new StringBuilder("{ command: " + this.command +
                //Arrays.toString(ArrayUtils.addAll(this.inputAttributes, this.outputAttributes)) +
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
