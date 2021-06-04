package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import uk.ac.ox.cs.pdq.algebra.*;


public class JSONPlan extends JSONRelationalTerm{

    public JSONPlan(RelationalTerm rt) {
        super(rt);
    }

    public String toString() {
        // Ask Michael whether both input attributes and output attributes are arguments
        StringBuilder toReturn = new StringBuilder("{ command: " + this.command +
                 ", " +
                "subexpression: [");

        for (JSONRelationalTerm jsonPlan : this.subexpression) {
            toReturn.append(jsonPlan.toString());
            toReturn.append(", ");
        }
        toReturn.append("]}");
        return toReturn.toString();
    }
}
