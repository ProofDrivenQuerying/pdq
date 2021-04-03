package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;

import java.util.Arrays;

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
