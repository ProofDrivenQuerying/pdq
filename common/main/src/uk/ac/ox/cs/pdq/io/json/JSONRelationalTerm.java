// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.io.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Attribute;

/**
 * Abstract class for all serializable Relational Terms.
 *
 * @author Camilo Ortiz
 */
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
            if (rtChildren[0] instanceof JoinTerm) {
                this.subexpression[0] = new JSONJoin((JoinTerm) rtChildren[0]);
            } else if (rtChildren[0] instanceof SelectionTerm) {
                this.subexpression[0] = new JSONSelection((SelectionTerm) rtChildren[0]);
            } else if (rtChildren[0] instanceof ProjectionTerm) {
                this.subexpression[0] = new JSONProjection((ProjectionTerm) rtChildren[0]);
            } else if (rtChildren[0] instanceof AccessTerm) {
                this.subexpression[0] = new JSONAccess((AccessTerm) rtChildren[0]);
            } else {
                this.subexpression[0] = new JSONPlan(rtChildren[0]);
            }
            if(rtChildren.length>1) {
                if (rtChildren[1] instanceof JoinTerm) {
                    this.subexpression[1] = new JSONJoin((JoinTerm) rtChildren[1]);

                } else if (rtChildren[1] instanceof SelectionTerm) {
                    this.subexpression[1] = new JSONSelection((SelectionTerm) rtChildren[1]);
                } else if (rtChildren[1] instanceof ProjectionTerm) {
                    this.subexpression[1] = new JSONProjection((ProjectionTerm) rtChildren[1]);
                } else if (rtChildren[1] instanceof AccessTerm) {
                    this.subexpression[1] = new JSONAccess((AccessTerm) rtChildren[1]);
                } else {
                    this.subexpression[1] = new JSONPlan(rtChildren[1]);
                }
            }
        }
    }


    /**
     * Returns a String version of RelationalTerm type
     * @param rt
     * @return
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

    /**
     * Returns a serializable JSON version of the RelationalTerm object
     * @param rt
     * @return
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
