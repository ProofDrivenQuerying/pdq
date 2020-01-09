import React from 'react';
import { connect } from 'react-redux';
import SchemaItem from './SchemaItem';
import ListGroup from 'react-bootstrap/ListGroup';
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
          <SchemaList schemaList={schemaList}/>
        </div>
      </div>
    )
}

const SchemaList = ({ schemaList}) => {
  if(!schemaList.isFetching){
    const schemas = schemaList.schemas.map((schemaFromList, index) =>
        <SchemaItem
          schemaFromList={schemaFromList}
          key={"schema"+index}/>
      );
      return (
        <ListGroup>
          {schemas}
        </ListGroup>
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

export default connect(mapStatesToProps, null)(SchemaBody);
