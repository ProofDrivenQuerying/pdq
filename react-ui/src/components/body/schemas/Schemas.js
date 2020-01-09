import React from 'react';
import { connect } from 'react-redux';
import SchemaItem from './SchemaItem';
import ListGroup from 'react-bootstrap/ListGroup';
import './schemas.css';


/**
 * Renders the schema information component.
 *
 * @author Camilo Ortiz
 */

const Schemas = ({ schemaList}) => {
  /*
    Contains a Header and SchemaList (a list of selectable schemas)
  */
    return(
      <div>
        <header className='body-name-schema'>
          Schemas
        </header>

        <SchemaList schemaList={schemaList}/>
      </div>
    )
}

const SchemaList = ({ schemaList}) => {
  /*
    SchemaList maps the schemas stored in the state (schemaList) to SchemaItem
    components to create a list of schemas
  */
  if(!schemaList.isFetching){
    const schemas = schemaList.schemas.map((schemaFromList, index) =>
        <SchemaItem
          schemaFromList={schemaFromList}
          key={"schema"+index}/>
      );
      return (
        <div className='schemas'>
          {schemas}
        </div>
    );
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

export default connect(mapStatesToProps, null)(Schemas);
