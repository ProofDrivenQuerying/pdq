package uk.ac.ox.cs.pdq.rest.jsonobjects.plan;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Attribute;

import java.util.Arrays;


public abstract class JSONRelationalTerm {
    @JsonProperty
    public String command;
    @JsonProperty
    public JSONRelationalTerm[] subexpression;
    @JsonProperty
    public Attribute[] inputAttributes;
    @JsonProperty
    public Attribute[] outputAttributes;

    public JSONRelationalTerm(RelationalTerm rt) {
        this.command = this.getType(rt);
        this.subexpression = new JSONRelationalTerm[rt.getChildren().length];
        this.inputAttributes = rt.getInputAttributes();
        this.outputAttributes = rt.getOutputAttributes();

        RelationalTerm[] rtChildren = rt.getChildren();

        if (rtChildren.length == 0) {
            this.subexpression = new JSONRelationalTerm[]{};
        } else {
            RelationalTerm rtChild = rtChildren[0];
            for (int i = 0; i < this.subexpression.length; i ++) {
                if (rtChild instanceof JoinTerm) {
                    this.subexpression[i] = new JSONJoin((JoinTerm) rtChildren[i]);
                } else if (rtChild instanceof SelectionTerm) {
                    this.subexpression[i] = new JSONSelection((SelectionTerm) rtChildren[i]);
                } else if (rtChild instanceof ProjectionTerm) {
                    this.subexpression[i] = new JSONProjection((ProjectionTerm) rtChildren[i]);
                } else if (rtChild instanceof AccessTerm) {
                    this.subexpression[i] = new JSONAccess((AccessTerm) rtChildren[i]);
                } else {
                    this.subexpression[i] = new JSONPlan(rtChildren[i]);
                }
            }

        }
    }

    /*
    Returns a String version of RelationalTerm type
     */
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
        } else if (rt instanceof RenameTerm) {
            toReturn = "Rename";
        } else {
            toReturn = "RelationalTerm";
        }
        return toReturn;
    }

    /*
    Returns a serializable JSON version of the RelationalTerm object
     */
    public static JSONRelationalTerm fromRelationalTerm(RelationalTerm rt) {
        JSONRelationalTerm toReturn;
        if (rt instanceof JoinTerm) {
            toReturn = new JSONJoin((JoinTerm) rt);
        } else if (rt instanceof SelectionTerm) {
            toReturn = new JSONSelection((SelectionTerm) rt);
        } else if (rt instanceof ProjectionTerm) {
            toReturn = new JSONProjection((ProjectionTerm) rt);
        } else if (rt instanceof AccessTerm) {
            toReturn = new JSONAccess((AccessTerm) rt);
        } else {
            toReturn = new JSONPlan(rt); // Todo: come up with a better default
        }
        return toReturn;
    }

}
