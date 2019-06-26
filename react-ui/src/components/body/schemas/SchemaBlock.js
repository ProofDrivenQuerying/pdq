import React from 'react';
import SchemaDropdown from './SchemaDropdown';
import { connect } from 'react-redux';



/**
 * This compoennt maps a SchemaDropdown component for each schema in schemaList.
 *
 * @author Camilo Ortiz
 */


const SchemaBlock = ({ schemaList}) => {

  if(!schemaList.isFetching){
    return schemaList.schemaList.map((schemaFromList, index) => {
      return(
        <SchemaDropdown
          schemaFromList={schemaFromList}
          key={"schema"+index}/>
      )
    })
  }
  else{
    return null;
  }
}

//map states to props
const mapStatesToProps = (state) =>({
  ...state
});


//connect component to redux store
export default connect(mapStatesToProps, null)(SchemaBlock);
