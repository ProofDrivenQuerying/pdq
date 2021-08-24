package uk.ac.ox.cs.pdq.ui;

import javafx.scene.control.TreeItem;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.PlanPrinter;

import java.util.ArrayList;

public class TreeViewHelper
{
    public TreeViewHelper()
    {
    }

    public static TreeItem printGenericPlanToTreeview(RelationalTerm p, Schema s) {
        if (p instanceof AccessTerm) {
            TreeItem access = new TreeItem(String.format("Access[%s]", PlanPrinter.chop(p.toString())));
            for (int i = 0; i < p.getChildren().length; i++) {
                access.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
            return access;
        }
        if (p instanceof CartesianProductTerm) {
            TreeItem join = new TreeItem(String.format("Join[%s]", PlanPrinter.chop(p.toString())));
            for (int i = 0; i < p.getChildren().length; i++) {
                join.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
            return join;
        }
        if (p instanceof ProjectionTerm) {
            ArrayList<Integer> positions = PlanPrinter.getProjectionPositionIndex((ProjectionTerm) p);
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < p.getOutputAttributes().length; i++) {
                buffer.append(PlanPrinter.projectionProvenance(p.getChildren(), positions.get(i)).getName());
                if (i < p.getOutputAttributes().length - 1) {
                    buffer.append(", ");
                }
            }
            TreeItem project = new TreeItem(String.format("Project[%s]", buffer));
            for (int i = 0; i < p.getChildren().length; i++) {
                project.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
            return project;
        }
        if (p instanceof RenameTerm) {
            TreeItem rename = new TreeItem("Rename");
            for (int i = 0; i < p.getChildren().length; i++) {
                rename.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
            return rename;
        }
        if (p instanceof SelectionTerm) {
            Condition c = ((SelectionTerm) p).getSelectionCondition();
            if (c instanceof ConjunctiveCondition) {
                SimpleCondition[] simpleConditions = ((ConjunctiveCondition) c).getSimpleConditions();
                for (SimpleCondition sc : simpleConditions) {
                    Integer position = sc.getPosition();
                    for (Relation r : s.getRelations()) {
                        String mappedName = r.getAttribute(position).getName();
                        sc.setMappedNamed(mappedName);
                    }
                }
            }
            TreeItem selection = new TreeItem(String.format("Select[%s]", PlanPrinter.chop(p.toString())));
            for (int i = 0; i < p.getChildren().length; i++) {
                selection.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
        }
        return null;
    }


}
