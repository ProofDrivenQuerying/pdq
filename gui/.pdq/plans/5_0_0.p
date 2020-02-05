<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<RelationalTermWithCost type="ProjectionTerm">
    <projections name="fv_activity_comment160032912" type="java.lang.String"/>
    <RelationalTerm type="SelectionTerm">
        <predicate type="ConjunctiveCondition">
            <predicates position="28" type="ConstantEqualityCondition">
                <constant value="29"/>
            </predicates>
            <predicates position="29" type="ConstantEqualityCondition">
                <constant value="30"/>
            </predicates>
        </predicate>
        <RelationalTerm type="RenameTerm">
            <renamings name="fv_activity_comment160032912" type="java.lang.String"/>
            <renamings name="v_activity_id160032912" type="java.lang.String"/>
            <renamings name="v_assay_chembl_id160032912" type="java.lang.String"/>
            <renamings name="v_assay_description160032912" type="java.lang.String"/>
            <renamings name="v_assay_type160032912" type="java.lang.String"/>
            <renamings name="v_bao_endpoint160032912" type="java.lang.String"/>
            <renamings name="v_bao_format160032912" type="java.lang.String"/>
            <renamings name="v_canonical_smiles160032912" type="java.lang.String"/>
            <renamings name="v_data_validity_comment160032912" type="java.lang.String"/>
            <renamings name="v_document_chembl_id160032912" type="java.lang.String"/>
            <renamings name="v_document_journal160032912" type="java.lang.String"/>
            <renamings name="v_document_year160032912" type="java.lang.String"/>
            <renamings name="v_molecule_chembl_id160032912" type="java.lang.String"/>
            <renamings name="v_pchembl_value160032912" type="java.lang.Double"/>
            <renamings name="v_potential_duplicate160032912" type="java.lang.String"/>
            <renamings name="v_published_relation160032912" type="java.lang.String"/>
            <renamings name="v_published_type160032912" type="java.lang.String"/>
            <renamings name="v_published_units160032912" type="java.lang.String"/>
            <renamings name="v_published_value160032912" type="java.lang.String"/>
            <renamings name="v_qudt_units160032912" type="java.lang.String"/>
            <renamings name="v_record_id160032912" type="java.lang.String"/>
            <renamings name="v_standard_flag160032912" type="java.lang.Boolean"/>
            <renamings name="v_standard_relation160032912" type="java.lang.String"/>
            <renamings name="v_standard_type160032912" type="java.lang.String"/>
            <renamings name="v_standard_units160032912" type="java.lang.String"/>
            <renamings name="v_standard_value160032912" type="java.lang.Double"/>
            <renamings name="v_target_chembl_id160032912" type="java.lang.String"/>
            <renamings name="v_target_organism160032912" type="java.lang.String"/>
            <renamings name="29" type="java.lang.String"/>
            <renamings name="30" type="java.lang.String"/>
            <RelationalTerm type="AccessTerm">
                <accessMethod name="chembl_activity_free"/>
                <inputConstants/>
                <relation name="activityFree">
                    <attribute name="activity_comment" type="java.lang.String"/>
                    <attribute name="activity_id" type="java.lang.String"/>
                    <attribute name="assay_chembl_id" type="java.lang.String"/>
                    <attribute name="assay_description" type="java.lang.String"/>
                    <attribute name="assay_type" type="java.lang.String"/>
                    <attribute name="bao_endpoint" type="java.lang.String"/>
                    <attribute name="bao_format" type="java.lang.String"/>
                    <attribute name="canonical_smiles" type="java.lang.String"/>
                    <attribute name="data_validity_comment" type="java.lang.String"/>
                    <attribute name="document_chembl_id" type="java.lang.String"/>
                    <attribute name="document_journal" type="java.lang.String"/>
                    <attribute name="document_year" type="java.lang.String"/>
                    <attribute name="molecule_chembl_id" type="java.lang.String"/>
                    <attribute name="pchembl_value" type="java.lang.Double"/>
                    <attribute name="potential_duplicate" type="java.lang.String"/>
                    <attribute name="published_relation" type="java.lang.String"/>
                    <attribute name="published_type" type="java.lang.String"/>
                    <attribute name="published_units" type="java.lang.String"/>
                    <attribute name="published_value" type="java.lang.String"/>
                    <attribute name="qudt_units" type="java.lang.String"/>
                    <attribute name="record_id" type="java.lang.String"/>
                    <attribute name="standard_flag" type="java.lang.Boolean"/>
                    <attribute name="standard_relation" type="java.lang.String"/>
                    <attribute name="standard_type" type="java.lang.String"/>
                    <attribute name="standard_units" type="java.lang.String"/>
                    <attribute name="standard_value" type="java.lang.Double"/>
                    <attribute name="target_chembl_id" type="java.lang.String"/>
                    <attribute name="target_organism" type="java.lang.String"/>
                    <attribute name="target_pref_name" type="java.lang.String"/>
                    <attribute name="uo_units" type="java.lang.String"/>
                    <access-method name="chembl_activity_free"/>
                </relation>
            </RelationalTerm>
        </RelationalTerm>
    </RelationalTerm>
    <cost value="4.154853167389282E8" type="DoubleCost"/>
</RelationalTermWithCost>