import React from 'react';

const PlanTreeNode = ({ relationalTerm }) => {
    console.log("[relationalTerm] " , relationalTerm)
    switch (relationalTerm.command) {
        case 'Join':
            return <div>{relationalTerm.command + `[${relationalTerm.condition}]`}</div>
        case 'Select':
            return <div>{relationalTerm.command + `[${relationalTerm.condition}]`}</div>
        case 'Project':
            return <div>{relationalTerm.command + `[${relationalTerm.ProvenanceProjections}]`}</div>
        case 'Access':
            return <div>{relationalTerm.command + `[${relationalTerm.accessString}]`}</div>
        default: 
            return relationalTerm.command
    }
}



export default PlanTreeNode;