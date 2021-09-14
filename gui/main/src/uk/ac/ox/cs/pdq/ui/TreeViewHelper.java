package uk.ac.ox.cs.pdq.ui;

import javafx.scene.control.TreeItem;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.io.PlanPrinter;

/**
 * TreeViewHelper used to create TreeItems for a tree view FXML
 * @author Brandon
 */
public class TreeViewHelper
{

    /**
     * Private method used to get the provenance attribute from simpleConditions to
     * when called from the outputAttributeProvenance
     * @param rt
     * @param simpleConditions
     */
    private static StringBuffer getProvenanceCondition(RelationalTerm rt, SimpleCondition[] simpleConditions){
        StringBuffer buffer = new StringBuffer();
        for (SimpleCondition sc : simpleConditions) {
            Attribute provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), sc.getPosition());
            //check position to see which child element to retrieve attribute from CartesianProductTerm
            if(rt instanceof CartesianProductTerm){
                //get position attribute
                if(sc.getPosition() < rt.getChild(0).getNumberOfOutputAttributes()){
                    provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), sc.getPosition() );
                }else{
                    provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(1), sc.getPosition() - rt.getChild(0).getNumberOfOutputAttributes());
                }
            }

            //get Other Attribute Provenance from AttributeEqualityCondition
            if (sc instanceof AttributeEqualityCondition) {
                Attribute otherProvenanceAttribute;
                if(rt instanceof CartesianProductTerm){
                    //check position to see which child element to retrieve attribute from CartesianProductTerm

                    //get Other position attribute
                    if(((AttributeEqualityCondition) sc).getOther() < rt.getChild(0).getNumberOfOutputAttributes()){
                        otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), ((AttributeEqualityCondition) sc).getOther());
                    }else{
                        otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0),( ((AttributeEqualityCondition) sc).getOther() - rt.getChild(0).getNumberOfOutputAttributes()));
                    }
                }else{
                    otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), ((AttributeEqualityCondition) sc).getOther());
                }

                buffer.append(String.format(" #%s=#%s", provenanceAttribute.getName(), otherProvenanceAttribute.getName()));
            } else {
                if (sc instanceof ConstantEqualityCondition) {
                    buffer.append(String.format(" #%s=%s", provenanceAttribute.getName(), ((ConstantEqualityCondition) sc).getConstant()));
                } else if (sc instanceof ConstantComparisonCondition) {
                    buffer.append(String.format(" #%s=%s", provenanceAttribute.getName(), ((ConstantComparisonCondition) sc).getConstant()));
                }

            }
        }
        return buffer;
    }

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
            ConjunctiveCondition cc;
            StringBuffer buffer = null;
            if(p instanceof JoinTerm){
                JoinTerm joinTerm = (JoinTerm) p;
                cc = (ConjunctiveCondition) joinTerm.getJoinConditions();
                buffer = getProvenanceCondition(p,cc.getSimpleConditions());
            }else if(p instanceof DependentJoinTerm){
                DependentJoinTerm dependentJoinTerm = (DependentJoinTerm) p;
                cc = (ConjunctiveCondition) dependentJoinTerm.getJoinConditions();
                buffer = getProvenanceCondition(p,cc.getSimpleConditions());
            }


            TreeItem join = new TreeItem(String.format("Join [%s]", buffer));

                join.getChildren().addAll(printGenericPlanToTreeview(p.getChild(0)));
                join.getChildren().addAll(printGenericPlanToTreeview(p.getChild(1)));

            join.setExpanded(true);
            return join;
        }
        if(p instanceof SelectionTerm){
            Condition c = ((SelectionTerm)p).getSelectionCondition();
            StringBuffer buffer = new StringBuffer();
            if(c instanceof ConjunctiveCondition){
                SimpleCondition[] simpleConditions = ((ConjunctiveCondition)c).getSimpleConditions();
                buffer = getProvenanceCondition(p, simpleConditions);
            }
            TreeItem select = new TreeItem(String.format("Select[%s]", buffer));

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
