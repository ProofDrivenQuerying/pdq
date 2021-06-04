package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.ac.ox.cs.pdq.algebra.JoinTerm;

import java.util.Arrays;

public class JSONJoin extends JSONRelationalTerm {
    @JsonProperty
    String condition;

    public JSONJoin(JoinTerm rt) {
        super(rt);
        this.condition = rt.getJoinConditions().asPredicate().toString();
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
