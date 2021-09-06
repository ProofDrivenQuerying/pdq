package uk.ac.ox.cs.pdq.ui;

import javafx.scene.control.TreeItem;
import uk.ac.ox.cs.pdq.algebra.*;
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
            TreeItem access = new TreeItem(String.format("Access[%s]", PlanPrinter.chop(p.toString())));
            for (int i = 0; i < p.getChildren().length; i++) {
                access.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
            access.setExpanded(true);
            return access;
        }
        if (p instanceof CartesianProductTerm) {
            TreeItem join = new TreeItem(String.format("Join %s", PlanPrinter.chop(p.toString())));
            for (int i = 0; i < p.getChildren().length; i++) {
                join.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
            join.setExpanded(true);
            return join;
        }
        if(p instanceof SelectionTerm){
            Condition c = ((SelectionTerm)p).getSelectionCondition();
            if(c instanceof ConjunctiveCondition){
                SimpleCondition[] simpleConditions = ((ConjunctiveCondition)c).getSimpleConditions();
                for (SimpleCondition sc : simpleConditions){
                    Integer position = sc.getPosition();
                    for(Relation r : s.getRelations()){
                        String mappedName = r.getAttribute(position).getName();
                        sc.setMappedNamed(mappedName);
                    }
                }
            }
            TreeItem select = new TreeItem(String.format("Select[%s]", PlanPrinter.chop(p.toString())));
            for (int i = 0; i < p.getChildren().length; i++) {
                select.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
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
            for (int i = 0; i < p.getChildren().length; i++) {
                project.getChildren().addAll(printGenericPlanToTreeview(p.getChild(i), s));
            }
            project.setExpanded(true);
            return project;
        }
        if (p instanceof RenameTerm) {
            for (int i = 0; i < p.getChildren().length; i++) {
               return printGenericPlanToTreeview(p.getChild(i), s);
            }
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
            selection.setExpanded(true);
            return selection;
        }
        return null;
    }


}
