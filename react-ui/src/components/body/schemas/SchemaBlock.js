//react
import React from 'react';
//components
import SchemaDropdown from './SchemaDropdown';
//redux
import { connect } from 'react-redux';



/**
 * SchemaBlock returns a button for each schema name in schemaList by using
 * the `.map()` function.
 *
 * @author Camilo Ortiz
 */


const SchemaBlock = ({ schemaList}) => {

  if(!schemaList.isFetching){
    return schemaList.schemaList.map((schemaFromList, index) => {
      return(
        <SchemaDropdown schemaFromList={schemaFromList} key={"schema"+index}/>
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
