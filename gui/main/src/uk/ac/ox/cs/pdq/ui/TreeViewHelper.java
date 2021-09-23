package uk.ac.ox.cs.pdq.ui;

import javafx.scene.control.TreeItem;
import uk.ac.ox.cs.pdq.algebra.*;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.fol.TypedConstant;
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
     * @param conditions
     */
    private static StringBuffer getProvenanceCondition(RelationalTerm rt, Condition[] conditions){
        StringBuffer buffer = new StringBuffer();
        for(int i =0; i < conditions.length; i++ ){
            Attribute provenanceAttribute;
            Attribute otherProvenanceAttribute;
            //check position to see which child element to retrieve attribute from CartesianProductTerm
            if(rt instanceof CartesianProductTerm){
                if (conditions[i] instanceof ConstantEqualityCondition) {
                    int position = ((ConstantEqualityCondition) conditions[i]).getPosition();
                    //get position attribute
                    if(position < rt.getChild(0).getNumberOfOutputAttributes()){
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                    }else{
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(1), position - rt.getChild(0).getNumberOfOutputAttributes());
                    }

                    buffer.append(String.format("#%s=%s", provenanceAttribute.getName(), ((ConstantEqualityCondition) conditions[i]).getConstant()));
                }else if (conditions[i] instanceof ConstantComparisonCondition) {
                    int position = ((ConstantComparisonCondition) conditions[i]).getPosition();
                    //get position attribute
                    if(position < rt.getChild(0).getNumberOfOutputAttributes()){
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                    }else{
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(1), position - rt.getChild(0).getNumberOfOutputAttributes());
                    }
                    buffer.append(String.format("#%s=%s", provenanceAttribute.getName(), ((ConstantEqualityCondition) conditions[i]).getConstant()));
                }else if (conditions[i] instanceof AttributeEqualityCondition) {
                    int position = ((AttributeEqualityCondition) conditions[i]).getPosition();
                    int other = ((AttributeEqualityCondition) conditions[i]).getOther();
                    //get position attribute
                    if(position < rt.getChild(0).getNumberOfOutputAttributes()){
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                    }else{
                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(1), position - rt.getChild(0).getNumberOfOutputAttributes());
                    }

                    //get Other position attribute
                    if(other < rt.getChild(0).getNumberOfOutputAttributes()){
                        otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), other);
                    }else{
                        otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0),( other - rt.getChild(0).getNumberOfOutputAttributes()));
                    }
                    buffer.append(String.format("#%s=#%s", provenanceAttribute.getName(), otherProvenanceAttribute.getName()));
                }
            }else{

                if (conditions[i] instanceof AttributeEqualityCondition) {
                    int position = ((AttributeEqualityCondition) conditions[i]).getPosition();
                    int other = ((AttributeEqualityCondition) conditions[i]).getOther();

                    provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                    //get Other Attribute Provenance from AttributeEqualityCondition
                    otherProvenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), other);
                    buffer.append(String.format("#%s=#%s", provenanceAttribute.getName(), otherProvenanceAttribute.getName()));
                } else {
                    if (conditions[i] instanceof ConstantEqualityCondition) {
                        int position = ((ConstantEqualityCondition) conditions[i]).getPosition();

                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position );
                        buffer.append(String.format("#%s=%s", provenanceAttribute.getName(), ((ConstantEqualityCondition) conditions[i]).getConstant()));
                    } else if (conditions[i] instanceof ConstantComparisonCondition) {
                        int position = ((ConstantComparisonCondition) conditions[i]).getPosition();

                        provenanceAttribute = PlanPrinter.outputAttributeProvenance(rt.getChild(0), position);
                        buffer.append(String.format("#%s=%s", provenanceAttribute.getName(), ((ConstantComparisonCondition) conditions[i]).getConstant()));
                    }
                }
            }
            // concat & when more then one Condition is set
            if (i < conditions.length - 1) {
                buffer.append(" & ");
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
            AccessTerm at = (AccessTerm) p;

            StringBuilder result = new StringBuilder();
            result.append(at.getRelation().getName());
            result.append(",");
            result.append(at.getAccessMethod().getName());
            result.append('[');
            for (int index = 0; index < at.getAccessMethod().getInputs().length; ++index) {
                result.append("#");
                Attribute provenanceName = PlanPrinter.outputAttributeProvenance(p,at.getAccessMethod().getInputs()[index]);
                result.append(provenanceName.getName());
                result.append("=");
                result.append("?");
                if (index < at.getAccessMethod().getInputs().length - 1)
                    result.append(",");
            }
            result.append(']');

            return new TreeItem(String.format("Access[%s]", result));
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
                Condition[] conditions = ((ConjunctiveCondition)c).getSimpleConditions();
                buffer = getProvenanceCondition(p, conditions);
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
