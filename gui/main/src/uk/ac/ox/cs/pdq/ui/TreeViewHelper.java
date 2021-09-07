package uk.ac.ox.cs.pdq.ui;

import javafx.scene.control.TreeItem;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.io.PlanPrinter;

/**
 * TreeViewHelper used to create TreeItems for a tree view FXML
 * @author Brandon
 */
public class TreeViewHelper
{

    /**
     * used to pass threw an plan(RelationalTerm) to a recursive call
     * to create nested nodes to display a plan with it provenance attributes
     * @param p
     * @return TreeItem
     */
    public static TreeItem printGenericPlanToTreeview(RelationalTerm p) {
        if (p instanceof AccessTerm) {
            return new TreeItem(String.format("Access[%s]", PlanPrinter.chop(p.toString())));
        }
        if (p instanceof CartesianProductTerm) {
            TreeItem join = new TreeItem(String.format("Join %s", PlanPrinter.chop(p.toString())));

                join.getChildren().addAll(printGenericPlanToTreeview(p.getChild(0)));
                join.getChildren().addAll(printGenericPlanToTreeview(p.getChild(1)));

            join.setExpanded(true);
            return join;
        }
        if(p instanceof SelectionTerm){
            TreeItem select = new TreeItem(String.format("Select[%s]", PlanPrinter.chop(p.toString())));

            select.getChildren().addAll(printGenericPlanToTreeview(p.getChild(0)));

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
            project.getChildren().addAll(printGenericPlanToTreeview(p.getChild(0)));
            project.setExpanded(true);
            return project;
        }
        if (p instanceof RenameTerm) {
               return printGenericPlanToTreeview(p.getChild(0));
        }
        return null;
    }


}
