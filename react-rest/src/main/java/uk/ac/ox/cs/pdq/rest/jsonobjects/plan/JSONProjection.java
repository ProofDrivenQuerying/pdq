package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.ac.ox.cs.pdq.algebra.ProjectionTerm;
import uk.ac.ox.cs.pdq.db.Attribute;

import java.util.Arrays;

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
