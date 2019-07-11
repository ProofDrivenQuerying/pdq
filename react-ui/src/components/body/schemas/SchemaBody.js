import React from 'react';
import { connect } from 'react-redux';
import SchemaName from './SchemaName';
import './schemabody.css';


/**
 * Renders the schema information component.
 *
 * @author Camilo Ortiz
 */

const SchemaBody = ({ schemaList}) => {
    return(
      <div>
        <header className='body-name-schema'>
          Schemas
        </header>

        <div className='schemas'>
          <SchemaBlock schemaList={schemaList}/>
        </div>
      </div>
    )
}

const SchemaBlock = ({ schemaList}) => {
  if(!schemaList.isFetching){
    return schemaList.schemas.map((schemaFromList, index) => {
      return(
        <SchemaName
          schemaFromList={schemaFromList}
          key={"schema"+index}/>
      )
    })
  }
  else{
    return null;
  }
}

//redux
const mapStatesToProps = (state) =>{
  return({
    schemaList: state.schemaList
  })
};

export default connect(mapStatesToProps, null)(SchemaBody);
