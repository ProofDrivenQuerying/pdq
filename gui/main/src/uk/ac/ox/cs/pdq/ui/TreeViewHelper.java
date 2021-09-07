package uk.ac.ox.cs.pdq.ui;

import javafx.scene.control.TreeItem;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.io.PlanPrinter;


public class TreeViewHelper
{
    public TreeViewHelper()
    {
    }

    public static TreeItem printGenericPlanToTreeview(RelationalTerm p, Schema s) {
        if (p instanceof AccessTerm) {
            return new TreeItem(String.format("Access[%s]", PlanPrinter.chop(p.toString())));
        }
        if (p instanceof CartesianProductTerm) {
            TreeItem join = new TreeItem(String.format("Join %s", PlanPrinter.chop(p.toString())));

                join.getChildren().addAll(printGenericPlanToTreeview(p.getChild(0), s));
                join.getChildren().addAll(printGenericPlanToTreeview(p.getChild(1), s));

            join.setExpanded(true);
            return join;
        }
        if(p instanceof SelectionTerm){
            TreeItem select = new TreeItem(String.format("Select[%s]", PlanPrinter.chop(p.toString())));

            select.getChildren().addAll(printGenericPlanToTreeview(p.getChild(0), s));

            select.setExpanded(true);
            return select;
        }
        if (p instanceof ProjectionTerm) {
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < p.getOutputAttributes().length; i++) {
                String a = PlanPrinter.outputAttributeProvenance(p, i).getName();
                buffer.append(a);
                if (i < p.getOutputAttributes().length - 1) {
                    buffer.append(", ");
                }
            }
            TreeItem project = new TreeItem(String.format("Project[%s]", buffer));
            project.getChildren().addAll(printGenericPlanToTreeview(p.getChild(0), s));
            project.setExpanded(true);
            return project;
        }
        if (p instanceof RenameTerm) {
               return printGenericPlanToTreeview(p.getChild(0), s);
        }
        return null;
    }


}
