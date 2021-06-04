import React from 'react';

const PlanTreeNode = ({ relationalTerm }) => {
    switch (relationalTerm.command) {
        case 'Join':
            return <div>{relationalTerm.command}</div>
        case 'Select':
            return <div>{relationalTerm.command}</div>
        case 'Project':
            return <div>{relationalTerm.command}</div>
        case 'Access':
            return <div>{relationalTerm.command}</div>
        default: 
            return relationalTerm.command
    }
}



export default PlanTreeNode;