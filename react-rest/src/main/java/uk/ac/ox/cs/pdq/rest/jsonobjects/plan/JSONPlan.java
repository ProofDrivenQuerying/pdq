package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import org.apache.commons.lang3.ArrayUtils;
import uk.ac.ox.cs.pdq.algebra.AccessTerm;
import uk.ac.ox.cs.pdq.algebra.RelationalTerm;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.algebra.*;

import java.util.Arrays;


public class JSONPlan {

    public JSONPlan[] subexpression;
    public Attribute[] inputAttributes;
    public Attribute[] outputAttributes;
    public String command;


    public JSONPlan(RelationalTerm rt) {
        this.command = getType(rt);
        inputAttributes = rt.getInputAttributes();
        outputAttributes = rt.getOutputAttributes();
        subexpression = new JSONPlan[rt.getChildren().length];

        System.out.println(Arrays.toString(rt.getChildren()));
        RelationalTerm[] rtChildren = rt.getChildren();
        for (int i = 0; i < subexpression.length; i ++) {
            // Initialize child
            this.subexpression[i] = new JSONPlan(rtChildren[i]);
        }
        System.out.println(Arrays.toString(this.subexpression));
    }

    public String getType(RelationalTerm rt) {
        String toReturn;
        if (rt instanceof JoinTerm) {
            toReturn = "Join";
        } else if (rt instanceof SelectionTerm) {
            toReturn = "Select";
        } else if (rt instanceof ProjectionTerm) {
            toReturn = "Project";
        } else if (rt instanceof AccessTerm) {
            toReturn = "Access";
        } else {
           toReturn = "RelationalTerm"; // Todo: come up with a better default
        }
        return toReturn;
    }

    public String toString() {
        // Ask Michael whether both input attributes and output attributes are arguments
        StringBuilder toReturn = new StringBuilder("{ command: " + this.command + ", arguments: " + Arrays.toString(ArrayUtils.addAll(this.inputAttributes, this.outputAttributes)) + ", " +
                "subexpression: [");

        for (JSONPlan jsonPlan : this.subexpression) {
            System.out.println(jsonPlan);
            toReturn.append(jsonPlan.toString());
            toReturn.append(", ");
        }
        toReturn.append("]}");
        return toReturn.toString();
    }
}
